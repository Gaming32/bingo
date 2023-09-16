package io.github.gaming32.bingo.game;

import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.data.BingoTags;
import io.github.gaming32.bingo.util.Util;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootDataManager;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class BingoBoard {
    public static final int SIZE = 5;
    public static final int SIZE_SQ = SIZE * SIZE;

    private static final int[] SECONDARY_DIAGONAL = {4, 8, 12, 16, 20};

    private final Teams[] states = new Teams[SIZE_SQ];
    private final ActiveGoal[] goals = new ActiveGoal[SIZE_SQ];

    private BingoBoard() {
        Arrays.fill(states, Teams.NONE);
    }

    public static BingoBoard generate(
        int difficulty,
        int teamCount,
        RandomSource rand,
        LootDataManager lootData,
        Predicate<BingoGoal> isAllowedGoal,
        @Nullable BingoGoal requiredGoal
    ) {
        final BingoBoard board = new BingoBoard();
        final BingoGoal[] generatedSheet = generateGoals(difficulty, rand, isAllowedGoal, requiredGoal);
        for (int i = 0; i < SIZE_SQ; i++) {
            board.goals[i] = generatedSheet[i].build(rand, lootData);
            if (BingoTags.isNever(generatedSheet[i].getTags())) {
                board.states[i] = Teams.fromAll(teamCount);
            }
        }
        return board;
    }

    public static BingoGoal[] generateGoals(
        int difficulty,
        RandomSource rand,
        Predicate<BingoGoal> isAllowedGoal,
        @Nullable BingoGoal requiredGoal
    ) {
        final BingoGoal[] generatedSheet = new BingoGoal[SIZE_SQ];

        final int[] difficultyLayout = generateDifficulty(difficulty, rand);
        final int[] indices = Util.shuffle(Util.generateIntArray(SIZE_SQ), rand);

        final Set<ResourceLocation> usedGoals = new HashSet<>();
        final Object2IntOpenHashMap<ResourceLocation> tagCount = new Object2IntOpenHashMap<>();
        final Set<String> antisynergys = new HashSet<>();
        final Set<String> reactants = new HashSet<>();
        final Set<String> catalysts = new HashSet<>();

        for (int i = 0; i < SIZE_SQ; i++) {
            List<BingoGoal> possibleGoals = BingoGoal.getGoalsByDifficulty(difficultyLayout[i]);

            int failSafe = 0;
            BingoGoal goal;

            goalGen:
            while (true) {
                if (requiredGoal != null) {
                    goal = requiredGoal;
                    requiredGoal = null;
                    break;
                }

                failSafe++;

                if (failSafe >= 500) {
                    if (difficultyLayout[i] == 0) {
                        throw new IllegalArgumentException("Invalid goal list");
                    }

                    possibleGoals = BingoGoal.getGoalsByDifficulty(--difficultyLayout[i]);
                    failSafe = 1;
                }

                final BingoGoal goalCandidate = possibleGoals.get(rand.nextInt(possibleGoals.size()));

                if (!isAllowedGoal.test(goalCandidate)) {
                    continue;
                }

                if (goalCandidate.getInfrequency() != null) {
                    if (rand.nextInt(goalCandidate.getInfrequency()) + 1 < goalCandidate.getInfrequency()) {
                        continue;
                    }
                }

                if (usedGoals.contains(goalCandidate.getId())) {
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
                tagCount.addTo(tag.id(), 1);
            }
            antisynergys.addAll(goal.getAntisynergy());
            catalysts.addAll(goal.getCatalyst());
            reactants.addAll(goal.getReactant());

            usedGoals.add(goal.getId());
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

    public Teams getState(int x, int y) {
        return states[getIndex(x, y)];
    }

    public ActiveGoal getGoal(int x, int y) {
        return goals[getIndex(x, y)];
    }

    private int getIndex(int x, int y) {
        return y * 5 + x;
    }

    public Teams[] getStates() {
        return states;
    }

    public ActiveGoal[] getGoals() {
        return goals;
    }

    @Override
    @SuppressWarnings("deprecation")
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
                texts[x] = WordUtils.wrap(getGoal(x, y).getName().getString(), boxWidth - 3, "\n", true).split("\n");
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

    public static final class Teams {
        public static final Teams NONE = new Teams(0);
        public static final Teams TEAM1 = new Teams(0b01);
        public static final Teams TEAM2 = new Teams(0b10);
        private static final Teams[] CACHE = {NONE, TEAM1, TEAM2, new Teams(0b11)};

        private final int bits;

        private Teams(int bits) {
            this.bits = bits;
        }

        public static Teams fromBits(int bits) {
            if (bits >= 0 && bits < CACHE.length) {
                return CACHE[bits];
            }
            return new Teams(bits);
        }

        public static Teams fromOne(int index) {
            return fromBits(1 << index);
        }

        public static Teams fromAll(int count) {
            return fromBits((1 << count) - 1);
        }

        public int toBits() {
            return bits;
        }

        public boolean any() {
            return bits != 0;
        }

        public boolean all(int count) {
            return Integer.bitCount(bits) >= count;
        }

        public boolean one() {
            return Integer.bitCount(bits) == 1;
        }

        public int getFirstIndex() {
            return Integer.numberOfTrailingZeros(bits);
        }

        public Teams or(Teams other) {
            return fromBits(bits | other.bits);
        }

        public Teams andNot(Teams other) {
            return fromBits(bits & ~other.bits);
        }

        public boolean and(Teams other) {
            return (bits & other.bits) != 0;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Teams other && bits == other.bits;
        }

        @Override
        public int hashCode() {
            return bits;
        }

        @Override
        public String toString() {
            return "Teams[" + Integer.toBinaryString(bits) + "]";
        }
    }
}
