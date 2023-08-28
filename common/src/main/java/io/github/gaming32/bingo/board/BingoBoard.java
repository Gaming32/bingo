package io.github.gaming32.bingo.board;

import io.github.gaming32.bingo.ActiveGoal;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.util.Util;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootDataManager;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BingoBoard {
    public static final int SIZE = 5;
    public static final int SIZE_SQ = SIZE * SIZE;

    private static final int[] SECONDARY_DIAGONAL = {4, 8, 12, 16, 20};

    private final BoardState[] board = new BoardState[SIZE_SQ];
    private final ActiveGoal[] goals = new ActiveGoal[SIZE_SQ];

    private BingoBoard() {
        Arrays.fill(board, BoardState.OFF);
    }

    public static BingoBoard generate(int difficulty, RandomSource rand, LootDataManager lootData) {
        final BingoBoard board = new BingoBoard();
        final BingoGoal[] generatedSheet = generateGoals(difficulty, rand);
        for (int i = 0; i < SIZE_SQ; i++) {
            board.goals[i] = generatedSheet[i].build(rand, lootData);
        }
        return board;
    }

    public static BingoGoal[] generateGoals(int difficulty, RandomSource rand) {
        final BingoGoal[] generatedSheet = new BingoGoal[SIZE_SQ];

        final int[] difficultyLayout = generateDifficulty(difficulty, rand);
        final int[] indices = Util.shuffle(Util.generateIntArray(SIZE_SQ), rand);

        final Set<ResourceLocation> usedGoals = new HashSet<>();
        final Object2IntMap<ResourceLocation> tagCount = new Object2IntOpenHashMap<>();
        final Set<String> antisynergys = new HashSet<>();
        final Set<String> reactants = new HashSet<>();
        final Set<String> catalysts = new HashSet<>();

        for (int i = 0; i < SIZE_SQ; i++) {
            List<BingoGoal> possibleGoals = BingoGoal.getGoalsByDifficulty(difficultyLayout[i]);

            int failSafe = 0;
            BingoGoal goal;

            goalGen:
            while (true) {
                failSafe++;

                if (failSafe >= 500) {
                    if (difficultyLayout[i] == 0) {
                        throw new IllegalArgumentException("Invalid goal list");
                    }

                    possibleGoals = BingoGoal.getGoalsByDifficulty(--difficultyLayout[i]);
                    failSafe = 1;
                }

                final BingoGoal goalCandidate = possibleGoals.get(rand.nextInt(possibleGoals.size()));

                if (goalCandidate.getInfrequency() != null) {
                    if (rand.nextInt(goalCandidate.getInfrequency()) + 1 < goalCandidate.getInfrequency()) {
                        continue;
                    }
                }

                if (!usedGoals.add(goalCandidate.getId())) {
                    continue;
                }

                if (!goalCandidate.getTags().isEmpty()) {
                    for (final BingoTag tag : goalCandidate.getTags()) {
                        if (tagCount.getInt(tag.id()) >= tag.difficultyMax().getInt(difficulty)) {
                            continue goalGen;
                        }
                    }

                    if (goalCandidate.getTags().stream().anyMatch(t -> !t.allowedOnSameLine())) {
                        for (int z = 0; z < i; z++) {
                            final List<BingoTag> tags = generatedSheet[indices[z]].getTags();
                            if (!tags.isEmpty() && isOnSameLine(indices[i], indices[z])) {
                                if (tags.stream().anyMatch(t ->
                                    !t.allowedOnSameLine() &&
                                        goalCandidate.getTags().stream().anyMatch(t2 -> t.id().equals(t2.id()))
                                )) {
                                    continue goalGen;
                                }
                            }
                        }
                    }
                }

                if (!goalCandidate.getAntisynergy().isEmpty()) {
                    if (goalCandidate.getAntisynergy().stream().anyMatch(antisynergys::contains)) {
                        continue;
                    }
                }
                if (!goalCandidate.getCatalyst().isEmpty()) {
                    if (goalCandidate.getCatalyst().stream().anyMatch(reactants::contains)) {
                        continue;
                    }
                }
                if (!goalCandidate.getReactant().isEmpty()) {
                    if (goalCandidate.getReactant().stream().anyMatch(catalysts::contains)) {
                        continue;
                    }
                }

                goal = goalCandidate;
                break;
            }

            for (final BingoTag tag : goal.getTags()) {
                tagCount.mergeInt(tag.id(), 1, Integer::sum);
            }
            antisynergys.addAll(goal.getAntisynergy());
            catalysts.addAll(goal.getCatalyst());
            reactants.addAll(goal.getReactant());

            generatedSheet[indices[i]] = goal;
        }

        return generatedSheet;
    }

    private static boolean isOnSameLine(int a, int b) {
        if (a % 6 == 0 && b % 6 == 0) {
            return true;
        }
        if (Arrays.binarySearch(SECONDARY_DIAGONAL, a) >= 0 && Arrays.binarySearch(SECONDARY_DIAGONAL, b) >= 0) {
            return true;
        }
        if (a / 5 == b / 5) {
            return true;
        }
        if (a % 5 == b % 5) {
            return true;
        }
        return false;
    }

    private static int[] generateDifficulty(int difficulty, RandomSource rand) {
        final int[] layout = new int[SIZE_SQ];

        final int amountOfVeryHard, amountOfHard, amountOfMedium, amountOfEasy;
        switch (difficulty) {
            case 1 -> {
                amountOfVeryHard = 0;
                amountOfHard = 0;
                amountOfMedium = 0;
                amountOfEasy = rand.nextIntBetweenInclusive(15, 19);
            }
            case 2 -> {
                amountOfVeryHard = 0;
                amountOfHard = 0;
                amountOfMedium = rand.nextIntBetweenInclusive(15, 19);
                amountOfEasy = 25 - amountOfMedium;
            }
            case 3 -> {
                amountOfVeryHard = 0;
                amountOfHard = rand.nextIntBetweenInclusive(15, 19);
                amountOfMedium = 25 - amountOfHard;
                amountOfEasy = 25 - amountOfHard - amountOfMedium;
            }
            case 4 -> {
                amountOfVeryHard = rand.nextIntBetweenInclusive(15, 19);
                amountOfHard = 25 - amountOfVeryHard;
                amountOfMedium = 25 - amountOfHard - amountOfVeryHard;
                amountOfEasy = 25 - amountOfHard - amountOfMedium - amountOfVeryHard;
            }
            default -> {
                amountOfVeryHard = 0;
                amountOfHard = 0;
                amountOfMedium = 0;
                amountOfEasy = 0;
            }
        }

        distributeDifficulty(layout, amountOfVeryHard, 4, rand);
        distributeDifficulty(layout, amountOfHard, 3, rand);
        distributeDifficulty(layout, amountOfMedium, 2, rand);
        distributeDifficulty(layout, amountOfEasy, 1, rand);

        return layout;
    }

    private static void distributeDifficulty(int[] layout, int amount, int difficulty, RandomSource rand) {
        for (int i = 0; i < amount; i++) {
            boolean cont;
            int failSafe = 0;

            do {
                cont = true;
                failSafe++;

                final int rng = rand.nextInt(SIZE_SQ);
                if (layout[rng] == 0) {
                    layout[rng] = difficulty;
                } else {
                    cont = false;
                    if (failSafe >= 500) break;
                }
            } while (!cont);
        }
    }

    public ActiveGoal getGoal(int x, int y) {
        return goals[y * 5 + x];
    }

    @Override
    public String toString() {
        final int boxWidth = 20;
        final int boxHeight = 7;

        final StringBuilder result = new StringBuilder((boxWidth * SIZE + 1) * (boxHeight + 1));

        for (int y = 0; y < SIZE; y++) {
            for (int column = 0; column < SIZE; column++) {
                result.append('+');
                result.append("-".repeat(boxWidth - 1));
            }
            result.append("+\n");

            final String[][] texts = new String[SIZE][];
            for (int x = 0; x < SIZE; x++) {
                texts[x] = WordUtils.wrap(getGoal(x, y).name().getString(), boxWidth - 3, "\n", true).split("\n");
            }

            for (int line = 0; line < boxHeight - 1; line++) {
                for (int x = 0; x < SIZE; x++) {
                    final String box = line < texts[x].length ? texts[x][line] : "";
                    result.append("| ").append(box).append(" ".repeat(boxWidth - box.length() - 2));
                }
                result.append("|\n");
            }
        }

        for (int column = 0; column < SIZE; column++) {
            result.append('+');
            result.append("-".repeat(boxWidth - 1));
        }
        result.append('+');

        return result.toString();
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
