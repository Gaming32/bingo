package io.github.gaming32.bingo.client;

import io.github.gaming32.bingo.game.ActiveGoal;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.game.GoalProgress;
import io.github.gaming32.bingo.game.mode.BingoGameMode;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;

public record ClientGame(
    int size,
    BingoBoard.Teams[] states,
    ActiveGoal[] goals,
    PlayerTeam[] teams,
    BingoGameMode.RenderMode renderMode,
    @Nullable GoalProgress[] progress
) {
    public BingoBoard.Teams getState(int x, int y) {
        return states[getIndex(x, y)];
    }

    public ActiveGoal getGoal(int x, int y) {
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
