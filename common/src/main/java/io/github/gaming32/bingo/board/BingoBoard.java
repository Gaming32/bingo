package io.github.gaming32.bingo.board;

import io.github.gaming32.bingo.goal.BingoGoal;
import io.github.gaming32.bingo.goal.GoalType;
import io.github.gaming32.bingo.goal.GoalTypes;
import net.minecraft.util.RandomSource;

import java.util.function.Predicate;

public class BingoBoard {
    public static final int SIZE = 5;

    private final BoardState[][] board = new BoardState[SIZE][SIZE];
    private final BingoGoal[][] goals = new BingoGoal[SIZE][SIZE];

    public BingoBoard() {
        for (final BoardState[] row : board) {
            for (int i = 0; i < SIZE; i++) {
                row[i] = BoardState.OFF;
            }
        }
    }

    public void randomize(RandomSource random) {
        for (final BingoGoal[] row : goals) {
            for (int i = 0; i < SIZE; i++) {
                row[i] = GoalTypes.randomGoal(random);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <G extends BingoGoal> void trigger(BoardState team, GoalType<G> type, Predicate<G> predicate) {
        for (int row = 0; row < SIZE; row++) {
            for (int column = 0; column < SIZE; column++) {
                if (!board[row][column].and(team)) {
                    final var goal = goals[row][column];
                    if (goal.getType() == type && predicate.test((G)goal)) {
                        board[row][column] = board[row][column].or(team);
                    }
                }
            }
        }
    }

    public enum BoardState {
        OFF(false, false),
        TEAM1(true, false),
        TEAM2(false, true),
        BOTH_TEAMS(true, true);

        public final boolean hasTeam1, hasTeam2;

        BoardState(boolean hasTeam1, boolean hasTeam2) {
            this.hasTeam1 = hasTeam1;
            this.hasTeam2 = hasTeam2;
        }

        public BoardState or(BoardState other) {
            return values()[ordinal() | other.ordinal()];
        }

        public boolean and(BoardState other) {
            return (ordinal() & other.ordinal()) != 0;
        }
    }
}
