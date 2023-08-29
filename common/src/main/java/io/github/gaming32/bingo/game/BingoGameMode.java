package io.github.gaming32.bingo.game;

import org.jetbrains.annotations.NotNull;

public interface BingoGameMode {
    BingoGameMode STANDARD = new BingoGameMode() {
        @NotNull
        @Override
        public BingoBoard.Teams getWinner(BingoBoard board, boolean tryHarder) {
            BingoBoard.Teams result = BingoBoard.Teams.NONE;
            if (didWin(board, BingoBoard.Teams.TEAM1)) {
                result = result.or(BingoBoard.Teams.TEAM1);
            }
            if (didWin(board, BingoBoard.Teams.TEAM2)) {
                result = result.or(BingoBoard.Teams.TEAM2);
            }
            return result;
        }

        private boolean didWin(BingoBoard board, BingoBoard.Teams team) {
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

        @Override
        public boolean canGetGoal(BingoBoard board, int index, BingoBoard.Teams team) {
            return !board.getStates()[index].and(team);
        }
    };

    BingoGameMode LOCKOUT = new BingoGameMode() {
        @NotNull
        @Override
        public BingoBoard.Teams getWinner(BingoBoard board, boolean tryHarder) {
            int count1 = 0;
            int count2 = 0;
            for (final BingoBoard.Teams state : board.getStates()) {
                if (state.hasTeam1) {
                    count1++;
                    if (count1 >= BingoBoard.SIZE_SQ / 2 + 1) {
                        return BingoBoard.Teams.TEAM1;
                    }
                }
                if (state.hasTeam2) {
                    count2++;
                    if (count2 >= BingoBoard.SIZE_SQ / 2 + 1) {
                        return BingoBoard.Teams.TEAM1;
                    }
                }
            }
            if (tryHarder) {
                return count1 == count2 ? BingoBoard.Teams.BOTH
                    : count1 > count2 ? BingoBoard.Teams.TEAM1 : BingoBoard.Teams.TEAM2;
            }
            return BingoBoard.Teams.NONE;
        }

        @Override
        public boolean canGetGoal(BingoBoard board, int index, BingoBoard.Teams team) {
            return !board.getStates()[index].any();
        }
    };

    @NotNull
    BingoBoard.Teams getWinner(BingoBoard board, boolean tryHarder);

    boolean canGetGoal(BingoBoard board, int index, BingoBoard.Teams team);
}
