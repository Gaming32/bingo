package io.github.gaming32.bingo.client;

import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.game.BingoGameMode;
import io.github.gaming32.bingo.game.GoalProgress;
import io.github.gaming32.bingo.network.ClientGoal;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;

public final class ClientGame {
    private final int size;
    private final BingoBoard.Teams[] states;
    private final ClientGoal[] goals;
    private final PlayerTeam[] teams;
    private final BingoGameMode.RenderMode renderMode;
    private final GoalProgress @Nullable [] progress;
    private long scheduledEndTime = -1;

    public ClientGame(
            int size,
            BingoBoard.Teams[] states,
            ClientGoal[] goals,
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

    public ClientGoal getGoal(int x, int y) {
        return goals[getIndex(x, y)];
    }

    @Nullable
    public GoalProgress getProgress(int x, int y) {
        return progress[getIndex(x, y)];
    }

    public PlayerTeam[] getTeams() {
        return teams;
    }

    public BingoBoard.Teams[] getStates() {
        return states;
    }

    public int getSize() {
        return size;
    }

    public GoalProgress[] getProgress() {
        return progress;
    }

    public ClientGoal[] getGoals() {
        return goals;
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
