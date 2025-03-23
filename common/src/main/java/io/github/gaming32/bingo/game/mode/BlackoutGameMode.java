package io.github.gaming32.bingo.game.mode;

import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.data.goal.GoalHolder;
import io.github.gaming32.bingo.game.BingoBoard;
import org.jetbrains.annotations.NotNull;

public class BlackoutGameMode implements BingoGameMode {
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
    public boolean isGoalAllowed(GoalHolder goal, boolean allowNeverGoalsInLockout) {
        return goal.goal().getTags().stream().allMatch(g -> g.value().specialType() == BingoTag.SpecialType.NONE);
    }
}
