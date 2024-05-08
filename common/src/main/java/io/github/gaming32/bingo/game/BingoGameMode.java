package io.github.gaming32.bingo.game;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.util.BingoStreamCodecs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

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
        public String getName() {
            return "standard";
        }
    };

    BingoGameMode LOCKOUT = new BingoGameMode() {
        private static final SimpleCommandExceptionType TOO_FEW_TEAMS =
            new SimpleCommandExceptionType(Bingo.translatable("bingo.lockout.too_few_teams"));
        private static final SimpleCommandExceptionType TEAM_MISSING_COLOR =
            new SimpleCommandExceptionType(Bingo.translatable("bingo.team_missing_color"));
        private static final SimpleCommandExceptionType DUPLICATE_TEAM_COLOR =
            new SimpleCommandExceptionType(Bingo.translatable("bingo.duplicate_team_color"));

        @Override
        public CommandSyntaxException checkAllowedConfig(GameConfig config) {
            if (config.teams.size() < 2) {
                return TOO_FEW_TEAMS.create();
            }

            final Set<ChatFormatting> uniqueColors = EnumSet.noneOf(ChatFormatting.class);
            for (final PlayerTeam team : config.teams) {
                if (team.getColor().getColor() == null) {
                    return TEAM_MISSING_COLOR.create();
                }
                uniqueColors.add(team.getColor());
            }
            if (uniqueColors.size() < config.teams.size()) {
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
            for (final BingoBoard.Teams state : board.getStates()) {
                if (state.count() == 1) {
                    totalHeld++;
                    teams[state.getFirstIndex()].goalsHeld++;
                }
            }

            Arrays.sort(teams, Comparator.comparing(v -> -v.goalsHeld)); // Sort in reverse

            final int totalGoals = board.getSize() * board.getSize();
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
            if (!isNever)
                return !board.getStates()[index].any();
            else
                return board.getStates()[index].count() > 1 && board.getStates()[index].and(team);
        }

        @Override
        public boolean isGoalAllowed(BingoGoal.Holder goal, boolean allowNeverGoalsInLockout) {
            return goal.goal().getTags().stream().allMatch((g) -> {
                if (g.tag().specialType() == BingoTag.SpecialType.NONE)
                    return true;
                if (g.tag().specialType() == BingoTag.SpecialType.NEVER)
                    return allowNeverGoalsInLockout;
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

        @Override
        public String getName() {
            return "lockout";
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
        public boolean isGoalAllowed(BingoGoal.Holder goal, boolean allowNeverGoalsInLockout) {
            return goal.goal().getTags().stream().allMatch(g -> g.tag().specialType() == BingoTag.SpecialType.NONE);
        }

        @Override
        public String getName() {
            return "blackout";
        }
    };

    Map<String, BingoGameMode> GAME_MODES = new HashMap<>(Map.of(
        "standard", STANDARD,
        "lockout", LOCKOUT,
        "blackout", BLACKOUT
    ));

    Codec<BingoGameMode> PERSISTENCE_CODEC = Codec.stringResolver(BingoGameMode::getName, GAME_MODES::get);

    @Nullable
    default CommandSyntaxException checkAllowedConfig(GameConfig config) {
        return null;
    }

    @NotNull
    BingoBoard.Teams getWinners(BingoBoard board, int teamCount, boolean tryHarder);

    boolean canGetGoal(BingoBoard board, int index, BingoBoard.Teams team, boolean isNever);

    default boolean isGoalAllowed(BingoGoal.Holder goal, boolean allowNeverGoalsInLockout) {
        return true;
    }

    default RenderMode getRenderMode() {
        return RenderMode.FANCY;
    }

    default boolean isLockout() {
        return false;
    }

    default boolean canFinishedTeamsGetMoreGoals() {
        return true;
    }

    String getName();

    record GameConfig(BingoGameMode gameMode, int size, Collection<PlayerTeam> teams) {
    }

    enum RenderMode {
        FANCY, ALL_TEAMS;

        public static final StreamCodec<FriendlyByteBuf, RenderMode> STREAM_CODEC = BingoStreamCodecs.enum_(RenderMode.class);
    }
}
