package io.github.gaming32.bingo.game.mode;

import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.data.goal.GoalHolder;
import io.github.gaming32.bingo.game.BingoBoard;
import org.jetbrains.annotations.NotNull;

public class BlackoutGameMode implements BingoGameMode {
    @NotNull
    @Override
    public BingoBoard.Teams getWinners(BingoBoard board, int teamCount, BingoBoard.Teams nerfedTeams, boolean tryHarder) {
        BingoBoard.Teams result = BingoBoard.Teams.NONE;
        for (int i = 0; i < teamCount; i++) {
            final BingoBoard.Teams team = BingoBoard.Teams.fromOne(i);
            if (hasWon(board, team, nerfedTeams.and(team))) {
                result = result.or(team);
            }
        }
        return result;
    }

    private boolean hasWon(BingoBoard board, BingoBoard.Teams team, boolean isNerfed) {
        final int goalCount = board.getShape().getGoalCount(board.getSize());
        for (int goalIndex = 0; goalIndex < goalCount; goalIndex++) {
            if (!BingoGameMode.hasGoal(board, goalIndex, team, isNerfed)) {
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
    public boolean isGoalAllowed(GoalHolder goal) {
        return goal.goal().getTags().stream().allMatch(g -> g.value().specialType() == BingoTag.SpecialType.NONE);
    }
}
