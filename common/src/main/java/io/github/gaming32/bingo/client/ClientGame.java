package io.github.gaming32.bingo.client;

import io.github.gaming32.bingo.game.ActiveGoal;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.game.GoalProgress;
import io.github.gaming32.bingo.game.mode.BingoGameMode;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;

public final class ClientGame {
    private final int size;
    private final BingoBoard.Teams[] states;
    private final ActiveGoal[] goals;
    private final PlayerTeam[] teams;
    private final BingoGameMode.RenderMode renderMode;
    private final GoalProgress @Nullable [] progress;
    private long scheduledEndTime = -1;

    public ClientGame(
            int size,
            BingoBoard.Teams[] states,
            ActiveGoal[] goals,
            PlayerTeam[] teams,
            BingoGameMode.RenderMode renderMode,
            @Nullable GoalProgress[] progress
    ) {
        this.size = size;
        this.states = states;
        this.goals = goals;
        this.teams = teams;
        this.renderMode = renderMode;
        this.progress = progress;
    }

    public BingoBoard.Teams getState(int x, int y) {
        return states[getIndex(x, y)];
    }

    public ActiveGoal getGoal(int x, int y) {
        return goals[getIndex(x, y)];
    }

    public GoalProgress[] getProgress() {
        return progress;
    }

    @Nullable
    public GoalProgress getProgress(int x, int y) {
        return progress[getIndex(x, y)];
    }

    public int getSize() {
        return size;
    }

    public BingoBoard.Teams[] getStates() {
        return states;
    }

    public ActiveGoal[] getGoals() {
        return goals;
    }

    public PlayerTeam[] getTeams() {
        return teams;
    }

    public BingoGameMode.RenderMode getRenderMode() {
        return renderMode;
    }

    public void setScheduledEndTime(long endTime) {
        this.scheduledEndTime = endTime;
    }

    public long getScheduledEndTime() {
        return scheduledEndTime;
    }

    private int getIndex(int x, int y) {
        return y * size + x;
    }
}
