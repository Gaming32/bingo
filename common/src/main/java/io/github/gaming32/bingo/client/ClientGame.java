package io.github.gaming32.bingo.client;

import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.game.BingoGameMode;
import io.github.gaming32.bingo.game.GoalProgress;
import io.github.gaming32.bingo.network.ClientGoal;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;

public record ClientGame(
    int size,
    BingoBoard.Teams[] states,
    ClientGoal[] goals,
    PlayerTeam[] teams,
    BingoGameMode.RenderMode renderMode,
    @Nullable GoalProgress[] progress
) {
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

    private int getIndex(int x, int y) {
        return y * size + x;
    }
}
