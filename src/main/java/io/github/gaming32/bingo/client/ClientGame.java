package io.github.gaming32.bingo.client;

import io.github.gaming32.bingo.game.ActiveGoal;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.game.BoardShape;
import io.github.gaming32.bingo.game.GoalProgress;
import io.github.gaming32.bingo.game.mode.BingoGameMode;
import net.minecraft.world.scores.PlayerTeam;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

public record ClientGame(
    BoardShape shape,
    int size,
    BingoBoard.Teams[] states,
    ActiveGoal[] goals,
    PlayerTeam[] teams,
    BingoGameMode.RenderMode renderMode,
    @Nullable GoalProgress[] progress,
    @Nullable Integer[] manualHighlights,
    MutableInt manualHighlightModCount
) {
}
