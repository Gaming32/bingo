package io.github.gaming32.bingo.game.mode;

import io.github.gaming32.bingo.game.BingoBoard;
import org.jetbrains.annotations.NotNull;

public class StandardGameMode implements BingoGameMode {
    @NotNull
    @Override
    public BingoBoard.Teams getWinners(BingoBoard board, int teamCount, BingoBoard.Teams nerfedTeams, boolean tryHarder) {
        BingoBoard.Teams result = BingoBoard.Teams.NONE;
        for (int i = 0; i < teamCount; i++) {
            final BingoBoard.Teams team = BingoBoard.Teams.fromOne(i);
            if (didWin(board, team, nerfedTeams.and(team))) {
                result = result.or(team);
            }
        }
        return result;
    }

    private boolean didWin(BingoBoard board, BingoBoard.Teams team, boolean isNerfed) {
        for (int[] line : board.getShape().getLines(board.getSize())) {
            boolean hasLine = true;
            for (int goalIndex : line) {
                if (!BingoGameMode.hasGoal(board, goalIndex, team, isNerfed)) {
                    hasLine = false;
                    break;
                }
            }

            if (hasLine) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canGetGoal(BingoBoard board, int index, BingoBoard.Teams team, boolean isNever) {
        return !board.getStates()[index].and(team) ^ isNever;
    }
}
