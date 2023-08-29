package io.github.gaming32.bingo.game;

import net.minecraft.advancements.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.scores.PlayerTeam;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BingoGame {
    private final BingoBoard board;
    private final BingoGameMode gameMode;
    private final PlayerTeam team1, team2;
    private final Map<UUID, Map<ActiveGoal, AdvancementProgress>> goalProgress = new HashMap<>();

    public BingoGame(BingoBoard board, BingoGameMode gameMode, PlayerTeam team1, PlayerTeam team2) {
        this.board = board;
        this.gameMode = gameMode;
        this.team1 = team1;
        this.team2 = team2;
    }

    public BingoBoard getBoard() {
        return board;
    }

    public BingoGameMode getGameMode() {
        return gameMode;
    }

    public void addPlayer(ServerPlayer player) {
        registerListeners(player);
    }

    public void endGame(PlayerList playerList, BingoBoard.Teams winner) {
        clearListeners(playerList);
        playerList.broadcastSystemMessage(Component.translatable(
            "bingo.ended", winner
        ), false);
    }

    private void registerListeners(ServerPlayer player) {
        for (final ActiveGoal goal : board.getGoals()) {
            registerListeners(player, goal);
        }
    }

    private void clearListeners(PlayerList playerList) {
        for (final var playerEntry : goalProgress.entrySet()) {
            final ServerPlayer player = playerList.getPlayer(playerEntry.getKey());
            if (player == null) continue;
            for (final ActiveGoal goal : playerEntry.getValue().keySet()) {
                unregisterListeners(player, goal);
            }
        }
    }

    private void registerListeners(ServerPlayer player, ActiveGoal goal) {
        final AdvancementProgress progress = getOrStartProgress(player, goal);
        if (!progress.isDone()) {
            for (final var entry : goal.getCriteria().entrySet()) {
                final CriterionProgress criterionProgress = progress.getCriterion(entry.getKey());
                if (criterionProgress != null && !criterionProgress.isDone()) {
                    final CriterionTriggerInstance triggerInstance = entry.getValue().getTrigger();
                    if (triggerInstance != null) {
                        final CriterionTrigger<CriterionTriggerInstance> trigger = CriteriaTriggers.getCriterion(triggerInstance.getCriterion());
                        if (trigger != null) {
                            trigger.addPlayerListener(
                                player.getAdvancements(), new GoalListener<>(triggerInstance, goal, entry.getKey())
                            );
                        }
                    }
                }
            }
        }
    }

    private void unregisterListeners(ServerPlayer player, ActiveGoal goal) {
        final AdvancementProgress progress = getOrStartProgress(player, goal);
        for (final var entry : goal.getCriteria().entrySet()) {
            final CriterionProgress criterionProgress = progress.getCriterion(entry.getKey());
            if (criterionProgress != null && (criterionProgress.isDone() || progress.isDone())) {
                final CriterionTriggerInstance triggerInstance = entry.getValue().getTrigger();
                if (triggerInstance != null) {
                    final CriterionTrigger<CriterionTriggerInstance> trigger = CriteriaTriggers.getCriterion(triggerInstance.getCriterion());
                    if (trigger != null) {
                        trigger.removePlayerListener(
                            player.getAdvancements(), new GoalListener<>(triggerInstance, goal, entry.getKey())
                        );
                    }
                }
            }
        }
    }

    public AdvancementProgress getOrStartProgress(ServerPlayer player, ActiveGoal goal) {
        final Map<ActiveGoal, AdvancementProgress> map = goalProgress.computeIfAbsent(player.getUUID(), k -> new HashMap<>());
        AdvancementProgress progress = map.get(goal);
        if (progress == null) {
            progress = new AdvancementProgress();
            progress.update(goal.getCriteria(), goal.getGoal().getRequirements());
            map.put(goal, progress);
        }
        return progress;
    }

    public boolean award(ServerPlayer player, ActiveGoal goal, String criterion) {
        boolean awarded = false;
        final AdvancementProgress progress = getOrStartProgress(player, goal);
        final boolean wasDone = progress.isDone();
        if (progress.grantProgress(criterion)) {
            unregisterListeners(player, goal);
            awarded = true;
        }
        if (!wasDone && progress.isDone()) {
            updateTeamBoard(player, goal);
        }
        return awarded;
    }

    private void updateTeamBoard(ServerPlayer player, ActiveGoal goal) {
        final BingoBoard.Teams team = getTeam(player);
        if (!team.any()) return;
        final BingoBoard.Teams[] board = this.board.getStates();
        final int index = ArrayUtils.indexOf(this.board.getGoals(), goal);
        if (gameMode.canGetGoal(this.board, index, team)) {
            board[index] = board[index].or(team);
            checkForWin(player.server.getPlayerList());
        }
    }

    @NotNull
    public BingoBoard.Teams getTeam(ServerPlayer player) {
        if (player.isAlliedTo(team1)) {
            return BingoBoard.Teams.TEAM1;
        }
        if (player.isAlliedTo(team2)) {
            return BingoBoard.Teams.TEAM2;
        }
        return BingoBoard.Teams.NONE;
    }

    public void checkForWin(PlayerList playerList) {
        final BingoBoard.Teams winner = getWinner(false);
        if (!winner.any()) return;
        endGame(playerList, winner);
    }

    public BingoBoard.Teams getWinner(boolean tryHarder) {
        return gameMode.getWinner(board, tryHarder);
    }
}
