package io.github.gaming32.bingo.game;

import io.github.gaming32.bingo.Bingo;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

    /**
     * @apiNote {@code player} does <i>not</i> need to be in a team. In fact, they should be added even if they aren't!
     */
    public void addPlayer(ServerPlayer player) {
        registerListeners(player);
    }

    public void endGame(PlayerList playerList, BingoBoard.Teams winner) {
        clearListeners(playerList);
        final Component message;
        if (winner.any()) {
            if (winner.all()) {
                message = Component.translatable("bingo.ended.tie");
            } else {
                final PlayerTeam playerTeam = getTeam(winner);
                Component teamName = playerTeam.getDisplayName();
                if (playerTeam.getColor() != ChatFormatting.RESET) {
                    teamName = teamName.copy().withStyle(playerTeam.getColor());
                }
                message = Component.translatable("bingo.ended", teamName);
            }
            for (final ServerPlayer player : playerList.getPlayers()) {
                player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 1f, 1f);
            }
        } else {
            message = Component.translatable("bingo.ended.draw");
        }
        playerList.broadcastSystemMessage(message, false);

        Bingo.activeGame = null;
        Bingo.updateCommandTree(playerList);
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
                unregisterListeners(player, goal, true);
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

    private void unregisterListeners(ServerPlayer player, ActiveGoal goal, boolean force) {
        final AdvancementProgress progress = getOrStartProgress(player, goal);
        for (final var entry : goal.getCriteria().entrySet()) {
            final CriterionProgress criterionProgress = progress.getCriterion(entry.getKey());
            if (criterionProgress != null && (force || criterionProgress.isDone() || progress.isDone())) {
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
            unregisterListeners(player, goal, false);
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
            notifyTeam(team, goal, player.server.getPlayerList());
            checkForWin(player.server.getPlayerList());
        }
    }

    private void notifyTeam(BingoBoard.Teams team, ActiveGoal goal, PlayerList playerList) {
        final PlayerTeam playerTeam = getTeam(team);
        final Component message = Component.translatable(
            "bingo.goal_obtained", goal.getName().copy().withStyle(ChatFormatting.GREEN)
        );
        for (final String member : playerTeam.getPlayers()) {
            final ServerPlayer player = playerList.getPlayerByName(member);
            if (player == null) continue;
            player.playNotifySound(SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.MASTER, 0.5f, 1f);
            player.sendSystemMessage(message);
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

    public PlayerTeam getTeam(BingoBoard.Teams team) {
        if (!team.one()) {
            throw new IllegalArgumentException("BingoGame.getTeam() called with multiple teams!");
        }
        assert team.hasTeam1 != team.hasTeam2;
        return team.hasTeam1 ? team1 : team2;
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
