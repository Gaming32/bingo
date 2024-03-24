package io.github.gaming32.bingo.game;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.mixin.common.StatsCounterAccessor;
import io.github.gaming32.bingo.network.VanillaNetworking;
import io.github.gaming32.bingo.network.messages.s2c.InitBoardPacket;
import io.github.gaming32.bingo.network.messages.s2c.RemoveBoardPacket;
import io.github.gaming32.bingo.network.messages.s2c.ResyncStatesPacket;
import io.github.gaming32.bingo.network.messages.s2c.SyncTeamPacket;
import io.github.gaming32.bingo.network.messages.s2c.UpdateProgressPacket;
import io.github.gaming32.bingo.network.messages.s2c.UpdateStatePacket;
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
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BingoGame {
    public static final Component REQUIRED_CLIENT_KICK = Component.literal(
        "This bingo game requires the Bingo mod to be installed on the client. Please install it before joining."
    );

    private final BingoBoard board;
    private final BingoGameMode gameMode;
    private final boolean requireClient;
    private final boolean persistent;
    private final PlayerTeam[] teams;

    private final Map<UUID, Map<ActiveGoal, AdvancementProgress>> advancementProgress = new HashMap<>();
    private final Map<UUID, Map<ActiveGoal, GoalProgress>> goalProgress = new HashMap<>();
    private final Map<UUID, Object2IntOpenHashMap<ActiveGoal>> goalAchievedCount = new HashMap<>();
    private final Map<UUID, List<ActiveGoal>> queuedGoals = new HashMap<>();
    private final Map<UUID, Object2IntMap<Stat<?>>> baseStats = new HashMap<>();

    public BingoGame(BingoBoard board, BingoGameMode gameMode, boolean requireClient, boolean persistent, PlayerTeam... teams) {
        this.board = board;
        this.gameMode = gameMode;
        this.requireClient = requireClient;
        this.persistent = persistent;
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

    /**
     * @apiNote {@code player} does <i>not</i> need to be in a team. In fact, they should be added even if they aren't!
     */
    public void addPlayer(ServerPlayer player) {
        if (requireClient && player.tickCount > 60 && !Bingo.isInstalledOnClient(player)) {
            player.connection.disconnect(REQUIRED_CLIENT_KICK);
            return;
        }

        RemoveBoardPacket.INSTANCE.sendTo(player);
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
        new SyncTeamPacket(team).sendTo(player);

        new InitBoardPacket(this, obfuscateTeam(team)).sendTo(player);
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
                    new UpdateProgressPacket(goalIndex, progress.progress(), progress.maxProgress()).sendTo(player);
                }
            });
        }
    }

    public BingoBoard.Teams[] obfuscateTeam(BingoBoard.Teams playerTeam) {
        final BingoBoard.Teams[] states = board.getStates();
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

    public void endGame(PlayerList playerList, BingoBoard.Teams winner) {
        clearListeners(playerList);
        final Component message;
        if (winner.any()) {
            if (!winner.one()) {
                message = Bingo.translatable("bingo.ended.tie");
            } else {
                final PlayerTeam playerTeam = getTeam(winner);
                Component teamName = BingoUtil.getDisplayName(playerTeam, playerList);
                if (playerTeam.getColor() != ChatFormatting.RESET) {
                    teamName = teamName.copy().withStyle(playerTeam.getColor());
                }
                message = Bingo.translatable("bingo.ended", teamName);
            }
            for (final ServerPlayer player : playerList.getPlayers()) {
                player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 1f, 1f);
            }
        } else {
            message = Bingo.translatable("bingo.ended.draw");
        }
        playerList.broadcastSystemMessage(message, false);

        Bingo.activeGame = null;
        new ResyncStatesPacket(board.getStates()).sendTo(playerList.getPlayers());
        Bingo.updateCommandTree(playerList);
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
                player,
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
                player,
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

        new UpdateProgressPacket(goalIndex, progress, maxProgress).sendTo(player);
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
        final Component message = Bingo.translatable(
            isLoss ? "bingo.goal_lost" : "bingo.goal_obtained",
            obtainer.getDisplayName(),
            goal.name().copy().withStyle(isLoss ? ChatFormatting.GOLD : ChatFormatting.GREEN)
        );
        final BingoBoard.Teams boardState = board.getStates()[boardIndex];
        final boolean showOtherTeam = Bingo.showOtherTeam || gameMode.getRenderMode() == BingoGameMode.RenderMode.ALL_TEAMS;
        final UpdateStatePacket stateMessage = !showOtherTeam
            ? new UpdateStatePacket(boardIndex, obfuscateTeam(team, boardState)) : null;
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
            if (!showOtherTeam) {
                stateMessage.sendTo(player);
            }
            player.connection.send(vanillaPacket);
            player.sendSystemMessage(message);
        }
        if (showOtherTeam) {
            new UpdateStatePacket(boardIndex, boardState).sendTo(playerList.getPlayers());
            if (gameMode.isLockout()) {
                Component teamComponent = BingoUtil.getDisplayName(playerTeam, playerList);
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
        final BingoBoard.Teams winner = getWinner(false);
        if (!winner.any()) return;
        endGame(playerList, winner);
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
        List<String> teamNames,
        Map<UUID, Int2ObjectMap<AdvancementProgress>> advancementProgress,
        Map<UUID, Int2ObjectMap<GoalProgress>> goalProgress,
        Map<UUID, Int2IntMap> goalAchievedCount,
        Map<UUID, IntList> queuedGoals,
        Map<UUID, Object2IntMap<Stat<?>>> baseStats
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
                Codec.STRING.listOf().fieldOf("team_names").forGetter(PersistenceData::teamNames),
                ADVANCEMENT_PROGRESS_CODEC.fieldOf("advancement_progress").forGetter(PersistenceData::advancementProgress),
                GOAL_PROGRESS_CODEC.fieldOf("goal_progress").forGetter(PersistenceData::goalProgress),
                GOAL_ACHIEVED_COUNT_CODEC.fieldOf("goal_achieved_count").forGetter(PersistenceData::goalAchievedCount),
                QUEUED_GOALS_CODEC.fieldOf("queued_goals").forGetter(PersistenceData::queuedGoals),
                BASE_STATS_CODEC.fieldOf("base_stats").forGetter(PersistenceData::baseStats)
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
            final BingoGame game = new BingoGame(board, gameMode, requireClient, true, teams);

            merge(game.advancementProgress, advancementProgress);
            merge(game.goalProgress, goalProgress);

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

            return game;
        }

        private <V> void merge(Map<UUID, Map<ActiveGoal, V>> target, Map<UUID, Int2ObjectMap<V>> source) {
            for (final var entry : source.entrySet()) {
                final Map<ActiveGoal, V> subTarget = Maps.newHashMapWithExpectedSize(entry.getValue().size());
                for (final var subEntry : entry.getValue().int2ObjectEntrySet()) {
                    subTarget.put(getGoal(subEntry.getIntKey()), subEntry.getValue());
                }
                target.put(entry.getKey(), subTarget);
            }
        }

        private ActiveGoal getGoal(int goal) {
            return board.getGoals()[goal];
        }

        private static PersistenceData create(BingoGame game) {
            final Map<UUID, Int2IntMap> goalAchievedCount = Maps.newHashMapWithExpectedSize(game.goalAchievedCount.size());
            for (final var entry : game.goalAchievedCount.entrySet()) {
                final Int2IntMap subTarget = new Int2IntOpenHashMap(entry.getValue().size());
                for (final var subEntry : entry.getValue().object2IntEntrySet()) {
                    subTarget.put(getGoal(game, subEntry.getKey()), subEntry.getIntValue());
                }
                goalAchievedCount.put(entry.getKey(), subTarget);
            }

            final Map<UUID, IntList> queuedGoals = Maps.newHashMapWithExpectedSize(game.queuedGoals.size());
            for (final var entry : game.queuedGoals.entrySet()) {
                final IntList subTarget = new IntArrayList(entry.getValue().size());
                for (final ActiveGoal value : entry.getValue()) {
                    subTarget.add(getGoal(game, value));
                }
                queuedGoals.put(entry.getKey(), subTarget);
            }

            return new PersistenceData(
                game.board, game.gameMode, game.requireClient,
                Arrays.stream(game.teams).map(PlayerTeam::getName).toList(),
                createMap(game, game.advancementProgress),
                createMap(game, game.goalProgress),
                goalAchievedCount, queuedGoals, game.baseStats
            );
        }

        private static <V> Map<UUID, Int2ObjectMap<V>> createMap(BingoGame game, Map<UUID, Map<ActiveGoal, V>> source) {
            final Map<UUID, Int2ObjectMap<V>> target = Maps.newHashMapWithExpectedSize(source.size());
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
