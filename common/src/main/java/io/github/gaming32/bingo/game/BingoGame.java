package io.github.gaming32.bingo.game;

import net.minecraft.advancements.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BingoGame {
    private final BingoBoard board;
    private final BingoGameMode gameMode;
    private final Map<UUID, Map<ActiveGoal, AdvancementProgress>> goalProgress = new HashMap<>();

    public BingoGame(BingoBoard board, BingoGameMode gameMode) {
        this.board = board;
        this.gameMode = gameMode;
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

    public void endGame(PlayerList playerList, BingoBoard.BoardState winner) {
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
        final BingoBoard.BoardState team = getTeam(player);
        if (team == null) return;
        final BingoBoard.BoardState[] board = this.board.getBoard();
        final int index = ArrayUtils.indexOf(this.board.getGoals(), goal);
        board[index] = board[index].or(team);
        checkForWin(player.server.getPlayerList());
    }

    public BingoBoard.BoardState getTeam(ServerPlayer player) {
        // TODO: Figure out how we want to handle this
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void checkForWin(PlayerList playerList) {
        final BingoBoard.BoardState winner = getWinner(false);
        if (!winner.any()) return;
        endGame(playerList, winner);
    }

    public BingoBoard.BoardState getWinner(boolean tryHarder) {
        return gameMode.getWinner(board, tryHarder);
    }
}
