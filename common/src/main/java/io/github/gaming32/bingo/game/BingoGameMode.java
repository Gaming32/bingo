package io.github.gaming32.bingo.game;

import org.jetbrains.annotations.NotNull;

public interface BingoGameMode {
    BingoGameMode STANDARD = new BingoGameMode() {
        @NotNull
        @Override
        public BingoBoard.BoardState getWinner(BingoBoard board, boolean tryHarder) {
            BingoBoard.BoardState result = BingoBoard.BoardState.OFF;
            if (didWin(board, BingoBoard.BoardState.TEAM1)) {
                result = result.or(BingoBoard.BoardState.TEAM1);
            }
            if (didWin(board, BingoBoard.BoardState.TEAM2)) {
                result = result.or(BingoBoard.BoardState.TEAM2);
            }
            return result;
        }

        private boolean didWin(BingoBoard board, BingoBoard.BoardState team) {
            columnsCheck:
            for (int column = 0; column < BingoBoard.SIZE; column++) {
                for (int y = 0; y < BingoBoard.SIZE; y++) {
                    if (!board.getState(column, y).and(team)) {
                        continue columnsCheck;
                    }
                }
                return true;
            }

            rowsCheck:
            for (int row = 0; row < BingoBoard.SIZE; row++) {
                for (int x = 0; x < BingoBoard.SIZE; x++) {
                    if (!board.getState(x, row).and(team)) {
                        continue rowsCheck;
                    }
                }
                return true;
            }

            boolean win = true;
            for (int i = 0; i < BingoBoard.SIZE; i++) {
                if (!board.getState(i, i).and(team)) {
                    win = false;
                    break;
                }
            }
            if (win) {
                return true;
            }

            for (int i = 0; i < BingoBoard.SIZE; i++) {
                if (!board.getState(i, BingoBoard.SIZE - i - 1).and(team)) {
                    return false;
                }
            }
            return true;
        }
    };

    BingoGameMode LOCKOUT = (board, tryHarder) -> {
        int count1 = 0;
        int count2 = 0;
        for (final BingoBoard.BoardState state : board.getBoard()) {
            if (state.hasTeam1) {
                count1++;
                if (count1 >= BingoBoard.SIZE_SQ / 2 + 1) {
                    return BingoBoard.BoardState.TEAM1;
                }
            }
            if (state.hasTeam2) {
                count2++;
                if (count2 >= BingoBoard.SIZE_SQ / 2 + 1) {
                    return BingoBoard.BoardState.TEAM1;
                }
            }
        }
        if (tryHarder) {
            return count1 == count2 ? BingoBoard.BoardState.BOTH_TEAMS
                : count1 > count2 ? BingoBoard.BoardState.TEAM1 : BingoBoard.BoardState.TEAM2;
        }
        return BingoBoard.BoardState.OFF;
    };

    @NotNull
    BingoBoard.BoardState getWinner(BingoBoard board, boolean tryHarder);
}
