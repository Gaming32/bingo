package io.github.gaming32.bingo.game;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.mixin.common.StatsCounterAccessor;
import io.github.gaming32.bingo.network.VanillaNetworking;
import io.github.gaming32.bingo.network.messages.s2c.InitBoardPayload;
import io.github.gaming32.bingo.network.messages.s2c.RemoveBoardPayload;
import io.github.gaming32.bingo.network.messages.s2c.ResyncStatesPayload;
import io.github.gaming32.bingo.network.messages.s2c.SyncTeamPayload;
import io.github.gaming32.bingo.network.messages.s2c.UpdateEndTimePayload;
import io.github.gaming32.bingo.network.messages.s2c.UpdateProgressPayload;
import io.github.gaming32.bingo.network.messages.s2c.UpdateStatePayload;
import io.github.gaming32.bingo.triggers.progress.ProgressibleTrigger;
import io.github.gaming32.bingo.util.BingoCodecs;
import io.github.gaming32.bingo.util.BingoUtil;
import io.github.gaming32.bingo.util.StatCodecs;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class BingoGame {
    public static final Component REQUIRED_CLIENT_KICK = Component.literal(
        "This bingo game requires the Bingo mod to be installed on the client. Please install it before joining."
    );

    private final BingoBoard board;
    private final BingoGameMode gameMode;
    private final boolean requireClient;
    private final boolean persistent;
    private final boolean continueAfterWin;
    private final PlayerTeam[] teams;

    private final Map<UUID, Map<ActiveGoal, AdvancementProgress>> advancementProgress = new HashMap<>();
    private final Map<UUID, Map<ActiveGoal, GoalProgress>> goalProgress = new HashMap<>();
    private final Map<UUID, Object2IntOpenHashMap<ActiveGoal>> goalAchievedCount = new HashMap<>();
    private final Map<UUID, List<ActiveGoal>> queuedGoals = new HashMap<>();
    private final Map<UUID, Object2IntMap<Stat<?>>> baseStats = new HashMap<>();
    private final ServerBossEvent vanillaRemainingTime = new ServerBossEvent(Bingo.translatable("bingo.remaining_time"), BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.PROGRESS);
    private BingoBoard.Teams winningTeams = BingoBoard.Teams.NONE;
    private BingoBoard.Teams finishedTeams = BingoBoard.Teams.NONE;
    private long scheduledEndTime = -1;

    public BingoGame(BingoBoard board, BingoGameMode gameMode, boolean requireClient, boolean persistent, boolean continueAfterWin, PlayerTeam... teams) {
        this.board = board;
        this.gameMode = gameMode;
        this.requireClient = requireClient;
        this.persistent = persistent;
        this.continueAfterWin = continueAfterWin;
        this.teams = teams;
    }

    public BingoBoard getBoard() {
        return board;
    }

    public BingoGameMode getGameMode() {
        return gameMode;
    }

    public boolean isRequireClient() {
        return requireClient;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public boolean shouldContinueAfterWin() {
        return continueAfterWin;
    }

    public void setScheduledEndTime(long endTime) {
        this.scheduledEndTime = endTime;
    }

    public long getScheduledEndTime() {
        return scheduledEndTime;
    }

    /**
     * @apiNote {@code player} does <i>not</i> need to be in a team. In fact, they should be added even if they aren't!
     */
    public void addPlayer(ServerPlayer player) {
        if (requireClient && player.tickCount > 60 && !Bingo.isInstalledOnClient(player)) {
            player.connection.disconnect(REQUIRED_CLIENT_KICK);
            return;
        }

        RemoveBoardPayload.INSTANCE.sendTo(player);
        if (Bingo.needAdvancementsClear.remove(player.getUUID())) {
            player.connection.send(new ClientboundUpdateAdvancementsPacket(
                false, List.of(), Set.of(VanillaNetworking.ROOT_ADVANCEMENT.id()), Map.of()
            ));
        }

        registerListeners(player);
        baseStats.computeIfAbsent(player.getUUID(), k -> new Object2IntOpenHashMap<>(
            ((StatsCounterAccessor)player.getStats()).getStats()
        ));

        final BingoBoard.Teams team = getTeam(player);
        new SyncTeamPayload(team).sendTo(player);

        InitBoardPayload.create(this, obfuscateTeam(team, player)).sendTo(player);
        player.connection.send(new ClientboundUpdateAdvancementsPacket(
            false,
            VanillaNetworking.generateAdvancements(board.getSize(), board.getGoals()),
            Set.of(),
            VanillaNetworking.generateProgressMap(board.getStates(), team)
        ));
        Bingo.needAdvancementsClear.add(player.getUUID());

        Map<ActiveGoal, GoalProgress> goalProgress = this.goalProgress.get(player.getUUID());
        if (goalProgress != null) {
            goalProgress.forEach((goal, progress) -> {
                int goalIndex = getBoardIndex(player, goal);
                if (goalIndex != -1) {
                    new UpdateProgressPayload(goalIndex, progress.progress(), progress.maxProgress()).sendTo(player);
                }
            });
        }

        if (scheduledEndTime > 0) {
            if (Bingo.isInstalledOnClient(player)) {
                new UpdateEndTimePayload(scheduledEndTime).sendTo(player);
            } else {
                vanillaRemainingTime.addPlayer(player);
            }
        }
    }

    public void removePlayer(ServerPlayer player) {
        unregisterListeners(player, true);
        vanillaRemainingTime.removePlayer(player);
    }

    public void updateRemainingTime(PlayerList playerList) {
        for (ServerPlayer player : playerList.getPlayers()) {
            if (Bingo.isInstalledOnClient(player)) {
                new UpdateEndTimePayload(scheduledEndTime).sendTo(player);
            }
        }
        updateVanillaRemainingTime();
    }

    public void updateVanillaRemainingTime() {
        if (vanillaRemainingTime.getPlayers().isEmpty())
            return;
        long remainingTime = getScheduledEndTime() - System.currentTimeMillis();
        String formatedRemainingTime = BingoUtil.formatRemainingTime(remainingTime);
        BossEvent.BossBarColor color = BossEvent.BossBarColor.WHITE;
        if (remainingTime < 30 * 60 * 1000)
            color = BossEvent.BossBarColor.PURPLE;
        if (remainingTime < 5 * 60 * 1000)
            color = BossEvent.BossBarColor.RED;
        vanillaRemainingTime.setName(Bingo.translatable("bingo.remaining_time_with_value", formatedRemainingTime));
        vanillaRemainingTime.setColor(color);
    }

    public BingoBoard.Teams[] obfuscateTeam(BingoBoard.Teams playerTeam, Player player) {
        final BingoBoard.Teams[] states = board.getStates();
        if (player != null && player.isSpectator()) {
            return states;
        }
        if (Bingo.showOtherTeam || gameMode.getRenderMode() == BingoGameMode.RenderMode.ALL_TEAMS) {
            return states;
        }
        if (!playerTeam.any()) {
            BingoBoard.Teams[] ret = new BingoBoard.Teams[states.length];
            Arrays.fill(ret, BingoBoard.Teams.NONE);
            return ret;
        }
        final BingoBoard.Teams[] result = new BingoBoard.Teams[states.length];
        for (int i = 0; i < states.length; i++) {
            result[i] = obfuscateTeam(playerTeam, states[i]);
        }
        return result;
    }

    public static BingoBoard.Teams obfuscateTeam(BingoBoard.Teams playerTeam, BingoBoard.Teams state) {
        if (Bingo.showOtherTeam) {
            return state;
        }
        return state.and(playerTeam) ? playerTeam : BingoBoard.Teams.NONE;
    }

    public Object2IntMap<Stat<?>> getBaseStats(ServerPlayer player) {
        return baseStats.get(player.getUUID());
    }

    public void endGame(PlayerList playerList) {
        clearListeners(playerList);
        final Component message;
        if (!winningTeams.any()) {
            winningTeams = getWinner(true);
        }
        if (winningTeams.any()) {
            if (!winningTeams.one()) {
                message = Bingo.translatable("bingo.ended.tie");
            } else {
                final PlayerTeam playerTeam = getTeam(winningTeams);
                message = BingoUtil.mapEither(
                    BingoUtil.getDisplayName(playerTeam, playerList),
                    name -> {
                        if (playerTeam.getColor() != ChatFormatting.RESET) {
                            return name.copy().withStyle(playerTeam.getColor());
                        }
                        return name;
                    }
                ).map(
                    playerName -> Bingo.translatable("bingo.ended.single", playerName),
                    teamName -> Bingo.translatable("bingo.ended", teamName)
                );
            }
            for (final ServerPlayer player : playerList.getPlayers()) {
                player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 1f, 1f);
            }
        } else {
            message = Bingo.translatable("bingo.ended.draw");
        }
        playerList.broadcastSystemMessage(message, false);

        Bingo.activeGame = null;
        new ResyncStatesPayload(board.getStates()).sendTo(playerList.getPlayers());
        Bingo.updateCommandTree(playerList);
        vanillaRemainingTime.removeAllPlayers();
    }

    private void registerListeners(ServerPlayer player) {
        for (final ActiveGoal goal : board.getGoals()) {
            registerListeners(player, goal);
        }
    }

    private void clearListeners(PlayerList playerList) {
        for (final var playerEntry : advancementProgress.entrySet()) {
            final ServerPlayer player = playerList.getPlayer(playerEntry.getKey());
            if (player == null) continue;
            for (final ActiveGoal goal : playerEntry.getValue().keySet()) {
                unregisterListeners(player, goal, true);
            }
        }
    }

    private <T extends CriterionTriggerInstance> CriterionTrigger.Listener<T> createListener(
        Criterion<T> criterion, String criterionId, ActiveGoal goal
    ) {
        return new CriterionTrigger.Listener<>(
            criterion.triggerInstance(),
            new AdvancementHolder(BingoBoard.generateVanillaId(board.getIndex(goal)), null),
            criterionId
        );
    }

    private <T extends CriterionTriggerInstance> void addListener(
        Criterion<T> criterion, String criterionId, ServerPlayer player, ActiveGoal goal
    ) {
        criterion.trigger().addPlayerListener(player.getAdvancements(), createListener(criterion, criterionId, goal));
        if (criterion.trigger() instanceof ProgressibleTrigger<T> progressibleTrigger) {
            progressibleTrigger.addProgressListener(
                player.getAdvancements(),
                new BingoGameProgressListener<>(this, goal, player, criterionId, criterion.triggerInstance())
            );
        }
    }

    private <T extends CriterionTriggerInstance> void removeListener(
        Criterion<T> criterion, String criterionId, ServerPlayer player, ActiveGoal goal
    ) {
        criterion.trigger().removePlayerListener(player.getAdvancements(), createListener(criterion, criterionId, goal));
        if (criterion.trigger() instanceof ProgressibleTrigger<T> progressibleTrigger) {
            progressibleTrigger.removeProgressListener(
                player.getAdvancements(),
                new BingoGameProgressListener<>(this, goal, player, criterionId, criterion.triggerInstance())
            );
        }
    }

    private void registerListeners(ServerPlayer player, ActiveGoal goal) {
        final AdvancementProgress progress = getOrStartProgress(player, goal);
        if (!progress.isDone()) {
            for (final var entry : goal.criteria().entrySet()) {
                final CriterionProgress criterionProgress = progress.getCriterion(entry.getKey());
                if (criterionProgress != null && !criterionProgress.isDone()) {
                    addListener(entry.getValue(), entry.getKey(), player, goal);
                }
            }
        }
    }

    private void unregisterListeners(ServerPlayer player, boolean force) {
        for (final ActiveGoal goal : board.getGoals()) {
            unregisterListeners(player, goal, force);
        }
    }

    private void unregisterListeners(ServerPlayer player, ActiveGoal goal, boolean force) {
        final AdvancementProgress progress = getOrStartProgress(player, goal);
        for (final var entry : goal.criteria().entrySet()) {
            final CriterionProgress criterionProgress = progress.getCriterion(entry.getKey());
            if (criterionProgress != null && (force || criterionProgress.isDone() || progress.isDone())) {
                removeListener(entry.getValue(), entry.getKey(), player, goal);
            }
        }
    }

    public AdvancementProgress getOrStartProgress(ServerPlayer player, ActiveGoal goal) {
        final Map<ActiveGoal, AdvancementProgress> map = advancementProgress.computeIfAbsent(player.getUUID(), k -> new HashMap<>());
        AdvancementProgress progress = map.get(goal);
        if (progress == null) {
            progress = new AdvancementProgress();
            progress.update(goal.goal().goal().getRequirements());
            map.put(goal, progress);
        }
        return progress;
    }

    @Nullable
    public GoalProgress getGoalProgress(ServerPlayer player, ActiveGoal goal) {
        var progress = goalProgress.get(player.getUUID());
        return progress == null ? null : progress.get(goal);
    }

    public void updateProgress(ServerPlayer player, ActiveGoal goal, int progress, int maxProgress) {
        int goalIndex = getBoardIndex(player, goal);
        if (goalIndex == -1) {
            return;
        }

        Map<ActiveGoal, GoalProgress> goalProgress = this.goalProgress.computeIfAbsent(player.getUUID(), k -> new HashMap<>());
        GoalProgress existingProgress = goalProgress.get(goal);
        if (existingProgress != null && existingProgress.progress() == progress && existingProgress.maxProgress() == maxProgress) {
            return;
        }

        new UpdateProgressPayload(goalIndex, progress, maxProgress).sendTo(player);
        goalProgress.put(goal, new GoalProgress(progress, maxProgress));
    }

    public boolean award(ServerPlayer player, ActiveGoal goal, String criterion) {
        return award(player, goal, criterion, 1);
    }

    public boolean award(ServerPlayer player, ActiveGoal goal, String criterion, int count) {
        if (goal.goal().goal().getSpecialType() == BingoTag.SpecialType.FINISH) {
            final BingoBoard.Teams team = getTeam(player);
            final BingoBoard.Teams[] board = this.board.getStates();
            final int index = getBoardIndex(player, goal);
            if (index == -1) {
                return false;
            }
            final BingoBoard.Teams oldTeams = board[index];
            board[index] = board[index].or(team);
            final boolean winner = getWinner(false).and(team);
            board[index] = oldTeams;
            if (!winner) {
                return false;
            }
        }
        boolean awarded = false;
        final AdvancementProgress progress = getOrStartProgress(player, goal);
        final boolean wasDone = progress.isDone();
        if (progress.grantProgress(criterion)) {
            unregisterListeners(player, goal, false);
            awarded = true;
        }
        if (!wasDone && progress.isDone()) {
            int completedCount = goalAchievedCount.computeIfAbsent(player.getUUID(), k -> new Object2IntOpenHashMap<>()).addTo(goal, count) + count;
            if (completedCount > goal.requiredCount()) {
                completedCount = goal.requiredCount();
            }
            goal.goal().goal().getProgress().onGoalCompleted(this, player, goal, completedCount);
            if (completedCount == goal.requiredCount()) {
                updateTeamBoard(player, goal, false);
            } else {
                for (String completedCriterion : progress.getCompletedCriteria()) {
                    progress.revokeProgress(completedCriterion);
                }
                unregisterListeners(player, goal, true);
                registerListeners(player, goal);
            }
        }
        goal.goal().goal().getProgress().criterionChanged(this, player, goal, criterion, true);
        return awarded;
    }

    public boolean award(ServerPlayer player, ActiveGoal goal) {
        final AdvancementProgress progress = getOrStartProgress(player, goal);
        if (progress.isDone()) {
            return false;
        }
        boolean success = false;
        for (final String criterion : progress.getRemainingCriteria()) {
            success |= award(player, goal, criterion, goal.requiredCount());
        }
        return success;
    }

    public boolean revoke(ServerPlayer player, ActiveGoal goal, String criterion) {
        boolean revoked = false;
        final AdvancementProgress progress = getOrStartProgress(player, goal);
        final boolean wasDone = progress.isDone();
        if (progress.revokeProgress(criterion)) {
            registerListeners(player, goal);
            revoked = true;
        }
        if (wasDone && !progress.isDone()) {
            updateTeamBoard(player, goal, true);
        }
        goal.goal().goal().getProgress().criterionChanged(this, player, goal, criterion, false);
        return revoked;
    }

    public boolean revoke(ServerPlayer player, ActiveGoal goal) {
        final AdvancementProgress progress = getOrStartProgress(player, goal);
        if (!progress.hasProgress()) {
            return false;
        }
        boolean success = false;
        for (final String criterion : progress.getCompletedCriteria()) {
            success |= revoke(player, goal, criterion);
        }
        var achievedCount = goalAchievedCount.get(player.getUUID());
        if (achievedCount != null) {
            achievedCount.removeInt(goal);
        }
        return success;
    }

    public void flushQueuedGoals(ServerPlayer player) {
        final List<ActiveGoal> goals = queuedGoals.remove(player.getUUID());
        if (goals == null) return;
        for (final ActiveGoal goal : goals) {
            updateTeamBoard(player, goal, false);
        }
    }

    private void updateTeamBoard(ServerPlayer player, ActiveGoal goal, boolean revoke) {
        final BingoBoard.Teams team = getTeam(player);
        if (!team.any()) {
            if (!revoke) {
                queuedGoals.computeIfAbsent(player.getUUID(), k -> new ArrayList<>()).add(goal);
            }
            return;
        }
        if (!gameMode.canFinishedTeamsGetMoreGoals() && finishedTeams.and(team)) {
            return;
        }

        final BingoBoard.Teams[] board = this.board.getStates();
        final int index = getBoardIndex(player, goal);
        if (index == -1) return;
        final boolean isNever = goal.goal().goal().getSpecialType() == BingoTag.SpecialType.NEVER;
        if (revoke || gameMode.canGetGoal(this.board, index, team, isNever)) {
            final boolean isLoss = isNever ^ revoke;
            board[index] = isLoss ? board[index].andNot(team) : board[index].or(team);
            notifyTeam(player, team, goal, player.server.getPlayerList(), index, isLoss);
            if (!isLoss) {
                checkForWin(player.server.getPlayerList());
            }
        }
    }

    private int getBoardIndex(ServerPlayer player, ActiveGoal goal) {
        final int index = ArrayUtils.indexOf(this.board.getGoals(), goal);
        if (index == -1) {
            Bingo.LOGGER.warn(
                "Player {} got a goal ({}) from a previous game! This should not happen.",
                player.getScoreboardName(), goal.goal().id()
            );
        }
        return index;
    }

    private void notifyTeam(
        ServerPlayer obtainer,
        BingoBoard.Teams team,
        ActiveGoal goal,
        PlayerList playerList,
        int boardIndex,
        boolean isLoss
    ) {
        final PlayerTeam playerTeam = getTeam(team);
        final Component goalName = goal.name().copy().withStyle(isLoss ? ChatFormatting.GOLD : ChatFormatting.GREEN);
        final Component message;
        if (playerTeam.getPlayers().size() == 1) {
            message = Bingo.translatable(
                isLoss ? "bingo.goal_lost.single" : "bingo.goal_obtained.single",
                goalName
            );
        } else {
            message = Bingo.translatable(
                isLoss ? "bingo.goal_lost" : "bingo.goal_obtained",
                obtainer.getDisplayName(),
                goalName
            );
        }
        final BingoBoard.Teams boardState = board.getStates()[boardIndex];
        final boolean showOtherTeam = Bingo.showOtherTeam || gameMode.getRenderMode() == BingoGameMode.RenderMode.ALL_TEAMS;
        final UpdateStatePayload statePayload = new UpdateStatePayload(boardIndex, boardState);
        final UpdateStatePayload obfuscatedStatePayload = new UpdateStatePayload(boardIndex, obfuscateTeam(team, boardState));
        final ClientboundUpdateAdvancementsPacket vanillaPacket = new ClientboundUpdateAdvancementsPacket(
            false,
            List.of(),
            Set.of(),
            Map.of(
                BingoBoard.generateVanillaId(boardIndex),
                VanillaNetworking.generateProgress(boardState.and(team))
            )
        );
        for (final String member : playerTeam.getPlayers()) {
            final ServerPlayer player = playerList.getPlayerByName(member);
            if (player == null) continue;
            player.playNotifySound(
                isLoss ? SoundEvents.RESPAWN_ANCHOR_DEPLETE.value() : SoundEvents.NOTE_BLOCK_CHIME.value(),
                SoundSource.MASTER,
                isLoss ? 1f : 0.5f, 1f
            );
            if (!showOtherTeam && !player.isSpectator()) {
                obfuscatedStatePayload.sendTo(player);
            }
            player.connection.send(vanillaPacket);
            player.sendSystemMessage(message);
        }
        if (showOtherTeam) {
            statePayload.sendTo(playerList.getPlayers());
            if (gameMode.isLockout()) {
                Component teamComponent = BingoUtil.getDisplayName(playerTeam, playerList)
                    .map(Function.identity(), Function.identity());
                if (playerTeam.getColor() != ChatFormatting.RESET) {
                    teamComponent = teamComponent.copy().withStyle(playerTeam.getColor());
                }
                final Component lockoutMessage = Bingo.translatable(
                    "bingo.goal_lost.lockout",
                    teamComponent, goal.name().copy().withStyle(ChatFormatting.GOLD)
                );
                for (final ServerPlayer player : playerList.getPlayers()) {
                    if (player.isAlliedTo(playerTeam)) continue;
                    player.playNotifySound(SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.MASTER, 0.5f, 1f);
                    player.sendSystemMessage(lockoutMessage);
                }
            }
        } else {
            for (final ServerPlayer player : playerList.getPlayers()) {
                if (player.isSpectator()) {
                    statePayload.sendTo(player);
                }
            }
        }
    }

    @NotNull
    public BingoBoard.Teams getTeam(ServerPlayer player) {
        for (int i = 0; i < teams.length; i++) {
            if (player.isAlliedTo(teams[i])) {
                return BingoBoard.Teams.fromOne(i);
            }
        }
        return BingoBoard.Teams.NONE;
    }

    public PlayerTeam getTeam(BingoBoard.Teams team) {
        if (!team.one()) {
            throw new IllegalArgumentException("BingoGame.getTeam() called with multiple teams!");
        }
        final int index = team.getFirstIndex();
        if (index >= teams.length) {
            throw new IllegalArgumentException("BingoGame.getTeam() called with a team it doesn't have");
        }
        return teams[index];
    }

    public PlayerTeam[] getTeams() {
        return teams;
    }

    public void checkForWin(PlayerList playerList) {
        final BingoBoard.Teams finishers = getWinner(false);
        final BingoBoard.Teams newFinishers = finishers.andNot(finishedTeams);
        if (!newFinishers.any()) {
            return;
        }

        final int place = finishedTeams.count() + 1;
        finishedTeams = finishedTeams.or(finishers);
        if (place == 1) {
            winningTeams = newFinishers;
        }

        int remainingTeams = 0;
        for (int i = 0; i < teams.length; i++) {
            boolean isTeamActive = teams[i].getPlayers().stream().anyMatch(playerName -> playerList.getPlayerByName(playerName) != null);
            if (isTeamActive && !finishedTeams.and(BingoBoard.Teams.fromOne(i))) {
                remainingTeams++;
            }
        }

        if (continueAfterWin) {
            notifyFinishedTeam(playerList, newFinishers, place, remainingTeams);
        }

        if (!continueAfterWin || remainingTeams < 2) {
            endGame(playerList);
        }
    }

    private void notifyFinishedTeam(PlayerList playerList, BingoBoard.Teams newFinishers, int place, int remainingTeams) {
        Component message;
        if (newFinishers.one()) {
            final PlayerTeam playerTeam = getTeam(newFinishers);
            message = BingoUtil.mapEither(
                BingoUtil.getDisplayName(playerTeam, playerList),
                name -> {
                    if (playerTeam.getColor() != ChatFormatting.RESET) {
                        return name.copy().withStyle(playerTeam.getColor());
                    }
                    return name;
                }
            ).map(
                playerName -> Bingo.translatable("bingo.finished.single", playerName, BingoUtil.ordinal(place)),
                teamName -> Bingo.translatable("bingo.finished", teamName, BingoUtil.ordinal(place))
            );
        } else {
            Component teamList = ComponentUtils.wrapInSquareBrackets(ComponentUtils.formatList(newFinishers.stream().mapToObj(teamIndex -> {
                final PlayerTeam team = getTeam(BingoBoard.Teams.fromOne(teamIndex));
                final Component name = Either.unwrap(BingoUtil.getDisplayName(team, playerList));
                if (team.getColor() != ChatFormatting.RESET) {
                    return name.copy().withStyle(team.getColor());
                }
                return name;
            }).toList(), Function.identity()));
            message = Bingo.translatable("bingo.finished.tie", teamList, BingoUtil.ordinal(place));
        }

        if (remainingTeams > 1) {
            for (final ServerPlayer player : playerList.getPlayers()) {
                player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 1f, 1f);
            }
        }

        playerList.broadcastSystemMessage(message, false);
    }

    public BingoBoard.Teams getWinner(boolean tryHarder) {
        return gameMode.getWinners(board, teams.length, tryHarder);
    }

    public PersistenceData createPersistenceData() {
        return PersistenceData.create(this);
    }

    private record BingoGameProgressListener<T extends CriterionTriggerInstance>(
        BingoGame game, ActiveGoal goal, ServerPlayer player, String criterionId, T triggerInstance
    ) implements ProgressibleTrigger.ProgressListener<T> {
        @Override
        public void update(T triggerInstance, int progress, int maxProgress) {
            if (triggerInstance == this.triggerInstance) {
                goal.goal().goal().getProgress().goalProgressChanged(game, player, goal, criterionId, progress, maxProgress);
            }
        }
    }

    public record PersistenceData(
        BingoBoard board,
        BingoGameMode gameMode,
        boolean requireClient,
        boolean continueAfterWin,
        List<String> teamNames,
        Map<UUID, Int2ObjectMap<AdvancementProgress>> advancementProgress,
        Map<UUID, Int2ObjectMap<GoalProgress>> goalProgress,
        Map<UUID, Int2IntMap> goalAchievedCount,
        Map<UUID, IntList> queuedGoals,
        Map<UUID, Object2IntMap<Stat<?>>> baseStats,
        BingoBoard.Teams winningTeams,
        BingoBoard.Teams finsihedTeams
    ) {
        private static final Codec<Map<UUID, Int2ObjectMap<AdvancementProgress>>> ADVANCEMENT_PROGRESS_CODEC =
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, BingoCodecs.int2ObjectMap(AdvancementProgress.CODEC));
        private static final Codec<Map<UUID, Int2ObjectMap<GoalProgress>>> GOAL_PROGRESS_CODEC =
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, BingoCodecs.int2ObjectMap(GoalProgress.PERSISTENCE_CODEC));
        private static final Codec<Map<UUID, Int2IntMap>> GOAL_ACHIEVED_COUNT_CODEC =
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, BingoCodecs.INT_2_INT_MAP);
        private static final Codec<Map<UUID, IntList>> QUEUED_GOALS_CODEC =
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, BingoCodecs.INT_LIST);
        private static final Codec<Map<UUID, Object2IntMap<Stat<?>>>> BASE_STATS_CODEC =
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, BingoCodecs.object2IntMap(StatCodecs.STRING_CODEC));

        public static final Codec<PersistenceData> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                BingoBoard.PERSISTENCE_CODEC.fieldOf("board").forGetter(PersistenceData::board),
                BingoGameMode.PERSISTENCE_CODEC.fieldOf("game_mode").forGetter(PersistenceData::gameMode),
                Codec.BOOL.fieldOf("require_client").forGetter(PersistenceData::requireClient),
                Codec.BOOL.optionalFieldOf("continue_after_win", false).forGetter(PersistenceData::continueAfterWin),
                Codec.STRING.listOf().fieldOf("team_names").forGetter(PersistenceData::teamNames),
                ADVANCEMENT_PROGRESS_CODEC.fieldOf("advancement_progress").forGetter(PersistenceData::advancementProgress),
                GOAL_PROGRESS_CODEC.fieldOf("goal_progress").forGetter(PersistenceData::goalProgress),
                GOAL_ACHIEVED_COUNT_CODEC.fieldOf("goal_achieved_count").forGetter(PersistenceData::goalAchievedCount),
                QUEUED_GOALS_CODEC.fieldOf("queued_goals").forGetter(PersistenceData::queuedGoals),
                BASE_STATS_CODEC.fieldOf("base_stats").forGetter(PersistenceData::baseStats),
                BingoBoard.Teams.CODEC.optionalFieldOf("winning_teams", BingoBoard.Teams.NONE).forGetter(PersistenceData::winningTeams),
                BingoBoard.Teams.CODEC.optionalFieldOf("finished_teams", BingoBoard.Teams.NONE).forGetter(PersistenceData::finsihedTeams)
            ).apply(instance, PersistenceData::new)
        );

        public BingoGame createGame(Scoreboard scoreboard) throws IllegalStateException {
            final PlayerTeam[] teams = new PlayerTeam[teamNames.size()];
            for (int i = 0; i < teams.length; i++) {
                teams[i] = scoreboard.getPlayerTeam(teamNames.get(i));
                if (teams[i] == null) {
                    throw new IllegalStateException("Team '" + teamNames.get(i) + "' no longer exists");
                }
            }
            final BingoGame game = new BingoGame(board, gameMode, requireClient, true, continueAfterWin, teams);

            for (final var entry : advancementProgress.entrySet()) {
                final Map<ActiveGoal, AdvancementProgress> subTarget = HashMap.newHashMap(entry.getValue().size());
                for (final var subEntry : entry.getValue().int2ObjectEntrySet()) {
                    final ActiveGoal goal = getGoal(subEntry.getIntKey());
                    final AdvancementProgress progress = subEntry.getValue();
                    progress.update(goal.goal().goal().getRequirements());
                    subTarget.put(goal, progress);
                }
                game.advancementProgress.put(entry.getKey(), subTarget);
            }

            for (final var entry : goalProgress.entrySet()) {
                final Map<ActiveGoal, GoalProgress> subTarget = HashMap.newHashMap(entry.getValue().size());
                for (final var subEntry : entry.getValue().int2ObjectEntrySet()) {
                    subTarget.put(getGoal(subEntry.getIntKey()), subEntry.getValue());
                }
                game.goalProgress.put(entry.getKey(), subTarget);
            }

            for (final var entry : goalAchievedCount.entrySet()) {
                final Object2IntOpenHashMap<ActiveGoal> subTarget = new Object2IntOpenHashMap<>(entry.getValue().size());
                for (final var subEntry : entry.getValue().int2IntEntrySet()) {
                    subTarget.put(getGoal(subEntry.getIntKey()), subEntry.getIntValue());
                }
                game.goalAchievedCount.put(entry.getKey(), subTarget);
            }

            for (final var entry : queuedGoals.entrySet()) {
                final List<ActiveGoal> subTarget = new ArrayList<>(entry.getValue().size());
                for (final int goal : entry.getValue()) {
                    subTarget.add(getGoal(goal));
                }
                game.queuedGoals.put(entry.getKey(), subTarget);
            }

            game.baseStats.putAll(baseStats);

            game.winningTeams = winningTeams;
            game.finishedTeams = finsihedTeams;

            return game;
        }

        private ActiveGoal getGoal(int goal) {
            return board.getGoals()[goal];
        }

        private static PersistenceData create(BingoGame game) {
            final Map<UUID, Int2IntMap> goalAchievedCount = HashMap.newHashMap(game.goalAchievedCount.size());
            for (final var entry : game.goalAchievedCount.entrySet()) {
                final Int2IntMap subTarget = new Int2IntOpenHashMap(entry.getValue().size());
                for (final var subEntry : entry.getValue().object2IntEntrySet()) {
                    subTarget.put(getGoal(game, subEntry.getKey()), subEntry.getIntValue());
                }
                goalAchievedCount.put(entry.getKey(), subTarget);
            }

            final Map<UUID, IntList> queuedGoals = HashMap.newHashMap(game.queuedGoals.size());
            for (final var entry : game.queuedGoals.entrySet()) {
                final IntList subTarget = new IntArrayList(entry.getValue().size());
                for (final ActiveGoal value : entry.getValue()) {
                    subTarget.add(getGoal(game, value));
                }
                queuedGoals.put(entry.getKey(), subTarget);
            }

            return new PersistenceData(
                game.board, game.gameMode, game.requireClient, game.continueAfterWin,
                Arrays.stream(game.teams).map(PlayerTeam::getName).toList(),
                createMap(game, game.advancementProgress),
                createMap(game, game.goalProgress),
                goalAchievedCount, queuedGoals, game.baseStats,
                game.winningTeams, game.finishedTeams
            );
        }

        private static <V> Map<UUID, Int2ObjectMap<V>> createMap(BingoGame game, Map<UUID, Map<ActiveGoal, V>> source) {
            final Map<UUID, Int2ObjectMap<V>> target = HashMap.newHashMap(source.size());
            for (final var entry : source.entrySet()) {
                final Int2ObjectMap<V> subTarget = new Int2ObjectOpenHashMap<>(entry.getValue().size());
                for (final var subEntry : entry.getValue().entrySet()) {
                    subTarget.put(getGoal(game, subEntry.getKey()), subEntry.getValue());
                }
                target.put(entry.getKey(), subTarget);
            }
            return target;
        }

        private static int getGoal(BingoGame game, ActiveGoal goal) {
            return game.board.getIndex(goal);
        }
    }
}
