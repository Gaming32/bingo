package io.github.gaming32.bingo.game;

import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoTag;
import org.jetbrains.annotations.NotNull;

public interface BingoGameMode {
    BingoGameMode STANDARD = new BingoGameMode() {
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

        @Override
        public boolean isGoalAllowed(BingoGoal goal) {
            return true;
        }
    };

    BingoGameMode LOCKOUT = new BingoGameMode() {
        @NotNull
        @Override
        public BingoBoard.Teams getWinners(BingoBoard board, int teamCount, boolean tryHarder) {
            if (teamCount != 2) {
                // TODO
                throw new UnsupportedOperationException("Lockout not supported on teamCount != 2 yet");
            }
            final int size = board.getSize();
            int count1 = 0;
            int count2 = 0;
            for (final BingoBoard.Teams state : board.getStates()) {
                if (state.and(BingoBoard.Teams.TEAM1)) {
                    count1++;
                    if (count1 >= size * size / 2 + 1) {
                        return BingoBoard.Teams.TEAM1;
                    }
                }
                if (state.and(BingoBoard.Teams.TEAM2)) {
                    count2++;
                    if (count2 >= size * size / 2 + 1) {
                        return BingoBoard.Teams.TEAM1;
                    }
                }
            }
            if (tryHarder) {
                return count1 == count2 ? BingoBoard.Teams.fromAll(2)
                    : count1 > count2 ? BingoBoard.Teams.TEAM1 : BingoBoard.Teams.TEAM2;
            }
            return BingoBoard.Teams.NONE;
        }

        @Override
        public boolean canGetGoal(BingoBoard board, int index, BingoBoard.Teams team, boolean isNever) {
            return !board.getStates()[index].any();
        }

        @Override
        public boolean isGoalAllowed(BingoGoal goal) {
            return goal.getTags().stream().allMatch(g -> g.specialType() == BingoTag.SpecialType.NONE);
        }
    };

    BingoGameMode BLACKOUT = new BingoGameMode() {
        @NotNull
        @Override
        public BingoBoard.Teams getWinners(BingoBoard board, int teamCount, boolean tryHarder) {
            BingoBoard.Teams result = BingoBoard.Teams.NONE;
            for (int i = 0; i < teamCount; i++) {
                final BingoBoard.Teams team = BingoBoard.Teams.fromOne(i);
                if (hasWon(board.getStates(), team)) {
                    result = result.or(team);
                }
            }
            return result;
        }

        private boolean hasWon(BingoBoard.Teams[] states, BingoBoard.Teams team) {
            for (final BingoBoard.Teams state : states) {
                if (!state.and(team)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean canGetGoal(BingoBoard board, int index, BingoBoard.Teams team, boolean isNever) {
            return !board.getStates()[index].and(team);
        }

        @Override
        public boolean isGoalAllowed(BingoGoal goal) {
            return goal.getTags().stream().allMatch(g -> g.specialType() == BingoTag.SpecialType.NONE);
        }
    };

    @NotNull
    BingoBoard.Teams getWinners(BingoBoard board, int teamCount, boolean tryHarder);

    boolean canGetGoal(BingoBoard board, int index, BingoBoard.Teams team, boolean isNever);

    boolean isGoalAllowed(BingoGoal goal);
}
