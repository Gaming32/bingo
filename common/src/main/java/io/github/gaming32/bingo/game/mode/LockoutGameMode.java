package io.github.gaming32.bingo.game.mode;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.data.goal.GoalHolder;
import io.github.gaming32.bingo.game.ActiveGoal;
import io.github.gaming32.bingo.game.BingoBoard;
import net.minecraft.ChatFormatting;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;

public class LockoutGameMode implements BingoGameMode {
    private static final SimpleCommandExceptionType TOO_FEW_TEAMS =
        new SimpleCommandExceptionType(Bingo.translatable("bingo.lockout.too_few_teams"));
    private static final SimpleCommandExceptionType TEAM_MISSING_COLOR =
        new SimpleCommandExceptionType(Bingo.translatable("bingo.team_missing_color"));
    private static final SimpleCommandExceptionType DUPLICATE_TEAM_COLOR =
        new SimpleCommandExceptionType(Bingo.translatable("bingo.duplicate_team_color"));

    @Override
    public CommandSyntaxException checkAllowedConfig(GameConfig config) {
        if (config.teams().size() < 2) {
            return TOO_FEW_TEAMS.create();
        }

        final Set<ChatFormatting> uniqueColors = EnumSet.noneOf(ChatFormatting.class);
        for (final PlayerTeam team : config.teams()) {
            if (team.getColor().getColor() == null) {
                return TEAM_MISSING_COLOR.create();
            }
            uniqueColors.add(team.getColor());
        }
        if (uniqueColors.size() < config.teams().size()) {
            return DUPLICATE_TEAM_COLOR.create();
        }

        return null;
    }

    @NotNull
    @Override
    public BingoBoard.Teams getWinners(BingoBoard board, int teamCount, boolean tryHarder) {
        class TeamValue {
            final BingoBoard.Teams team;
            int goalsHeld;

            TeamValue(BingoBoard.Teams team) {
                this.team = team;
            }
        }

        final TeamValue[] teams = new TeamValue[teamCount];
        for (int i = 0; i < teamCount; i++) {
            teams[i] = new TeamValue(BingoBoard.Teams.fromOne(i));
        }

        int totalHeld = 0;
        boolean nonStalemateGoalsLeft = false;
        for (int x = 0; x < board.getSize(); x++) {
            for (int y = 0; y < board.getSize(); y++) {
                BingoBoard.Teams state = board.getState(x, y);
                if (state.count() == 1) {
                    totalHeld++;
                    teams[state.getFirstIndex()].goalsHeld++;
                } else if (!nonStalemateGoalsLeft) {
                    ActiveGoal activeGoal = board.getGoal(x, y);
                    nonStalemateGoalsLeft = activeGoal.specialType() != BingoTag.SpecialType.NEVER || activeGoal.isLockoutInflictable();
                }
            }
        }

        Arrays.sort(teams, Comparator.comparing(v -> -v.goalsHeld)); // Sort in reverse

        final int totalGoals = nonStalemateGoalsLeft ? board.getSize() * board.getSize() : totalHeld;
        if (totalGoals - totalHeld < teams[0].goalsHeld - teams[1].goalsHeld) {
            return teams[0].team;
        }
        if (totalHeld == totalGoals || tryHarder) {
            if (totalHeld == 0) {
                return BingoBoard.Teams.NONE;
            }
            BingoBoard.Teams tied = BingoBoard.Teams.NONE;
            int held = teams[0].goalsHeld;
            for (final TeamValue team : teams) {
                if (team.goalsHeld != held) break;
                tied = tied.or(team.team);
            }
            return tied;
        }
        return BingoBoard.Teams.NONE;
    }

    @Override
    public boolean canGetGoal(BingoBoard board, int index, BingoBoard.Teams team, boolean isNever) {
        if (!isNever) {
            return !board.getStates()[index].any();
        } else {
            return board.getStates()[index].count() > 1 && board.getStates()[index].and(team);
        }
    }

    @Override
    public boolean isGoalAllowed(GoalHolder goal, boolean allowNeverGoalsInLockout) {
        return goal.goal().getTags().stream().allMatch((g) -> {
            if (g.value().specialType() == BingoTag.SpecialType.NONE) {
                return true;
            }
            if (g.value().specialType() == BingoTag.SpecialType.NEVER) {
                return allowNeverGoalsInLockout;
            }
            return false;
        });
    }

    @Override
    public RenderMode getRenderMode() {
        return RenderMode.ALL_TEAMS;
    }

    @Override
    public boolean isLockout() {
        return true;
    }

    @Override
    public boolean canFinishedTeamsGetMoreGoals() {
        return false;
    }
}
