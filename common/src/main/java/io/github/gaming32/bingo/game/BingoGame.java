package io.github.gaming32.bingo.game;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.data.ProgressTracker;
import io.github.gaming32.bingo.mixin.common.AdvancementProgressAccessor;
import io.github.gaming32.bingo.mixin.common.StatsCounterAccessor;
import io.github.gaming32.bingo.network.VanillaNetworking;
import io.github.gaming32.bingo.network.messages.s2c.*;
import io.github.gaming32.bingo.triggers.AbstractProgressibleTriggerInstance;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stat;
import net.minecraft.world.scores.PlayerTeam;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BingoGame {
    public static final Component REQUIRED_CLIENT_KICK = Component.literal(
        "This bingo game requires the Bingo mod to be installed on the client. Please install it before joining."
    );

    private final BingoBoard board;
    private final BingoGameMode gameMode;
    private final boolean requireClient;
    private final PlayerTeam[] teams;
    private final Map<UUID, Map<ActiveGoal, AdvancementProgress>> advancementProgress = new HashMap<>();
    private final Map<UUID, Map<ActiveGoal, GoalProgress>> goalProgress = new HashMap<>();
    private final Map<UUID, List<ActiveGoal>> queuedGoals = new HashMap<>();
    private final Map<UUID, Object2IntMap<Stat<?>>> baseStats = new HashMap<>();

    public BingoGame(BingoBoard board, BingoGameMode gameMode, boolean requireClient, PlayerTeam... teams) {
        this.board = board;
        this.gameMode = gameMode;
        this.requireClient = requireClient;
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

    /**
     * @apiNote {@code player} does <i>not</i> need to be in a team. In fact, they should be added even if they aren't!
     */
    public void addPlayer(ServerPlayer player) {
        if (requireClient && player.tickCount > 60 && !Bingo.isInstalledOnClient(player)) {
            player.connection.disconnect(REQUIRED_CLIENT_KICK);
            return;
        }

        RemoveBoardMessage.INSTANCE.sendTo(player);
        if (Bingo.needAdvancementsClear.remove(player)) {
            player.connection.send(new ClientboundUpdateAdvancementsPacket(
                false,
                List.of(),
                VanillaNetworking.generateAdvancementIds(BingoBoard.MAX_SIZE),
                Map.of()
            ));
        }

        registerListeners(player);
        baseStats.computeIfAbsent(player.getUUID(), k -> new Object2IntOpenHashMap<>(
            ((StatsCounterAccessor)player.getStats()).getStats()
        ));

        final BingoBoard.Teams team = getTeam(player);
        new SyncTeamMessage(team).sendTo(player);

        new InitBoardMessage(board.getSize(), board.getGoals(), obfuscateTeam(team, board.getStates())).sendTo(player);
        player.connection.send(new ClientboundUpdateAdvancementsPacket(
            false,
            VanillaNetworking.generateAdvancements(board.getSize(), board.getGoals()),
            Set.of(),
            VanillaNetworking.generateProgressMap(board.getStates(), team)
        ));
        Bingo.needAdvancementsClear.add(player);

        Map<ActiveGoal, GoalProgress> goalProgress = this.goalProgress.get(player.getUUID());
        if (goalProgress != null) {
            goalProgress.forEach((goal, progress) -> {
                int goalIndex = getBoardIndex(player, goal);
                if (goalIndex != -1) {
                    new UpdateProgressMessage(goalIndex, progress.progress(), progress.maxProgress()).sendTo(player);
                }
            });
        }
    }

    public static BingoBoard.Teams[] obfuscateTeam(BingoBoard.Teams playerTeam, BingoBoard.Teams[] states) {
        if (Bingo.showOtherTeam) {
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
            if (!winner.one() && winner.all(teams.length)) {
                message = Bingo.translatable("bingo.ended.tie");
            } else {
                final PlayerTeam playerTeam = getTeam(winner);
                Component teamName = playerTeam.getDisplayName();
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
        new ResyncStatesMessage(board.getStates()).sendTo(playerList.getPlayers());
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
        if (criterion.triggerInstance() instanceof AbstractProgressibleTriggerInstance progressibleTrigger) {
            progressibleTrigger.addProgressListener(new BingoGameProgressListener(this, player, goal, criterionId));
        }
    }

    private <T extends CriterionTriggerInstance> void removeListener(
        Criterion<T> criterion, String criterionId, ServerPlayer player, ActiveGoal goal
    ) {
        criterion.trigger().removePlayerListener(player.getAdvancements(), createListener(criterion, criterionId, goal));
        if (criterion.triggerInstance() instanceof AbstractProgressibleTriggerInstance progressibleTrigger) {
            progressibleTrigger.removeProgressListener(new BingoGameProgressListener(this, player, goal, criterionId));
        }
    }

    private void registerListeners(ServerPlayer player, ActiveGoal goal) {
        final AdvancementProgress progress = getOrStartProgress(player, goal);
        if (!progress.isDone()) {
            for (final var entry : goal.getCriteria().entrySet()) {
                final CriterionProgress criterionProgress = progress.getCriterion(entry.getKey());
                if (criterionProgress != null && !criterionProgress.isDone()) {
                    addListener(entry.getValue(), entry.getKey(), player, goal);
                }
            }
        }
    }

    private void unregisterListeners(ServerPlayer player, ActiveGoal goal, boolean force) {
        final AdvancementProgress progress = getOrStartProgress(player, goal);
        for (final var entry : goal.getCriteria().entrySet()) {
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
            progress.update(goal.getGoal().getRequirements());
            map.put(goal, progress);
        }
        return progress;
    }

    private void onProgress(ServerPlayer player, ActiveGoal goal, String criterionId, int progress, int maxProgress) {
        if (!(goal.getGoal().getProgress() instanceof ProgressTracker.Criterion progressTracker)) {
            return;
        }

        progress = (int) (progress * progressTracker.scale);
        maxProgress = (int) (maxProgress * progressTracker.scale);

        if (criterionId.equals(progressTracker.criterion)) {
            updateProgress(player, goal, progress, maxProgress);
        }
    }

    private void updateProgress(ServerPlayer player, ActiveGoal goal, int progress, int maxProgress) {
        int goalIndex = getBoardIndex(player, goal);
        if (goalIndex == -1) {
            return;
        }

        Map<ActiveGoal, GoalProgress> goalProgress = this.goalProgress.computeIfAbsent(player.getUUID(), k -> new HashMap<>());
        GoalProgress existingProgress = goalProgress.get(goal);
        if (existingProgress != null && existingProgress.progress() == progress && existingProgress.maxProgress() == maxProgress) {
            return;
        }

        new UpdateProgressMessage(goalIndex, progress, maxProgress).sendTo(player);
        goalProgress.put(goal, new GoalProgress(progress, maxProgress));
    }

    public boolean award(ServerPlayer player, ActiveGoal goal, String criterion) {
        if (goal.getGoal().getSpecialType() == BingoTag.SpecialType.FINISH) {
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
            updateTeamBoard(player, goal, false);
        }
        onCriteriaChange(player, goal);
        return awarded;
    }

    public boolean award(ServerPlayer player, ActiveGoal goal) {
        final AdvancementProgress progress = getOrStartProgress(player, goal);
        if (progress.isDone()) {
            return false;
        }
        boolean success = false;
        for (final String criterion : progress.getRemainingCriteria()) {
            success |= award(player, goal, criterion);
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
        onCriteriaChange(player, goal);
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
        return success;
    }

    private void onCriteriaChange(ServerPlayer player, ActiveGoal goal) {
        if (goal.getGoal().getProgress() instanceof ProgressTracker.AchievedRequirements) {
            AdvancementProgress progress = getOrStartProgress(player, goal);

            int amount = ((AdvancementProgressAccessor) progress).callCountCompletedRequirements();
            int max = goal.getGoal().getRequirements().size();
            updateProgress(player, goal, amount, max);
        }
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
        final boolean isNever = goal.getGoal().getSpecialType() == BingoTag.SpecialType.NEVER;
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
                player.getScoreboardName(), goal.getGoal().getId()
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
            goal.getName().copy().withStyle(isLoss ? ChatFormatting.GOLD : ChatFormatting.GREEN)
        );
        final BingoBoard.Teams boardState = board.getStates()[boardIndex];
        final UpdateStateMessage stateMessage = !Bingo.showOtherTeam
            ? new UpdateStateMessage(boardIndex, obfuscateTeam(team, boardState)) : null;
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
            if (!Bingo.showOtherTeam) {
                assert stateMessage != null;
                stateMessage.sendTo(player);
            }
            player.connection.send(vanillaPacket);
            player.sendSystemMessage(message);
        }
        if (Bingo.showOtherTeam) {
            new UpdateStateMessage(boardIndex, boardState).sendTo(playerList.getPlayers());
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

    public void checkForWin(PlayerList playerList) {
        final BingoBoard.Teams winner = getWinner(false);
        if (!winner.any()) return;
        endGame(playerList, winner);
    }

    public BingoBoard.Teams getWinner(boolean tryHarder) {
        return gameMode.getWinners(board, teams.length, tryHarder);
    }

    private record BingoGameProgressListener(BingoGame game, ServerPlayer player, ActiveGoal goal, String criterionId) implements AbstractProgressibleTriggerInstance.ProgressListener {
        @Override
        public void update(int progress, int maxProgress) {
            game.onProgress(player, goal, criterionId, progress, maxProgress);
        }
    }
}
