package io.github.gaming32.bingo.game.mode;

import io.github.gaming32.bingo.game.BingoBoard;
import org.jetbrains.annotations.NotNull;

public class StandardGameMode implements BingoGameMode {
    @NotNull
    @Override
    public BingoBoard.Teams getWinners(BingoBoard board, int teamCount, boolean tryHarder) {
        BingoBoard.Teams result = BingoBoard.Teams.NONE;
        for (int i = 0; i < teamCount; i++) {
            final BingoBoard.Teams team = BingoBoard.Teams.fromOne(i);
            if (didWin(board, team)) {
                result = result.or(team);
            }
        }
        return result;
    }

    private boolean didWin(BingoBoard board, BingoBoard.Teams team) {
        for (int[] line : board.getShape().getLines(board.getSize())) {
            for (int goalIndex : line) {
                if (!board.getStates()[goalIndex].and(team)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean canGetGoal(BingoBoard board, int index, BingoBoard.Teams team, boolean isNever) {
        return !board.getStates()[index].and(team) ^ isNever;
    }
}
