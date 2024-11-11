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
        int size = board.getSize();

        columnsCheck:
        for (int column = 0; column < size; column++) {
            for (int y = 0; y < size; y++) {
                if (!board.getState(column, y).and(team)) {
                    continue columnsCheck;
                }
            }
            return true;
        }

        rowsCheck:
        for (int row = 0; row < size; row++) {
            for (int x = 0; x < size; x++) {
                if (!board.getState(x, row).and(team)) {
                    continue rowsCheck;
                }
            }
            return true;
        }

        // check primary diagonal
        boolean win = true;
        for (int i = 0; i < size; i++) {
            if (!board.getState(i, i).and(team)) {
                win = false;
                break;
            }
        }
        if (win) {
            return true;
        }

        // check secondary diagonal
        for (int i = 0; i < size; i++) {
            if (!board.getState(i, size - i - 1).and(team)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canGetGoal(BingoBoard board, int index, BingoBoard.Teams team, boolean isNever) {
        return !board.getStates()[index].and(team) ^ isNever;
    }
}
