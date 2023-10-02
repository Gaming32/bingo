package io.github.gaming32.bingo.game;

import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.util.BingoUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.LootDataManager;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class BingoBoard {
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 7;
    public static final int DEFAULT_SIZE = 5;

    private final int size;
    private final Teams[] states;
    private final ActiveGoal[] goals;
    private final Map<ResourceLocation, ActiveGoal> byVanillaId;
    private final Object2IntMap<ActiveGoal> toGoalIndex;

    private BingoBoard(int size) {
        this.size = size;
        this.states = new Teams[size * size];
        this.goals = new ActiveGoal[size * size];
        this.byVanillaId = new HashMap<>(size * size);
        this.toGoalIndex = new Object2IntOpenHashMap<>(size * size);

        Arrays.fill(states, Teams.NONE);
        toGoalIndex.defaultReturnValue(-1);
    }

    public static BingoBoard generate(
        int size,
        int difficulty,
        int teamCount,
        RandomSource rand,
        LootDataManager lootData,
        Predicate<BingoGoal> isAllowedGoal,
        @Nullable BingoGoal requiredGoal,
        boolean allowsClientRequired
    ) {
        final BingoBoard board = new BingoBoard(size);
        final BingoGoal[] generatedSheet = generateGoals(size, difficulty, rand, isAllowedGoal, requiredGoal, allowsClientRequired);
        for (int i = 0; i < size * size; i++) {
            final ActiveGoal goal = board.goals[i] = generatedSheet[i].build(rand, lootData);
            if (generatedSheet[i].getSpecialType() == BingoTag.SpecialType.NEVER) {
                board.states[i] = Teams.fromAll(teamCount);
            }
            board.byVanillaId.put(generateVanillaId(i), goal);
            board.toGoalIndex.put(goal, i);
        }
        return board;
    }

    public static BingoGoal[] generateGoals(
        int size,
        int difficulty,
        RandomSource rand,
        Predicate<BingoGoal> isAllowedGoal,
        @Nullable BingoGoal requiredGoal,
        boolean allowsClientRequired
    ) {
        final BingoGoal[] generatedSheet = new BingoGoal[size * size];

        final int[] difficultyLayout = generateDifficulty(size, difficulty, rand);
        final int[] indices = BingoUtil.shuffle(BingoUtil.generateIntArray(size * size), rand);

        final Set<ResourceLocation> usedGoals = new HashSet<>();
        final Object2IntOpenHashMap<ResourceLocation> tagCount = new Object2IntOpenHashMap<>();
        final Set<String> antisynergys = new HashSet<>();
        final Set<String> reactants = new HashSet<>();
        final Set<String> catalysts = new HashSet<>();

        for (int i = 0; i < size * size; i++) {
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

                if (!allowsClientRequired && goalCandidate.isRequiredOnClient()) {
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
                            if (!tags.isEmpty() && isOnSameLine(size, indices[i], indices[z])) {
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

    private static boolean isOnSameLine(int size, int a, int b) {
        // check primary diagonal
        if (a % (size + 1) == 0 && b % (size + 1) == 0) {
            return true;
        }

        // check secondary diagonal
        if (size > 1
            && a % (size - 1) == 0 && b % (size - 1) == 0 // this checks the secondary diagonal plus the other two corners
            && a != 0 && b != 0 // exclude the top left corner
            && a != size * size - 1 && b != size * size - 1 // exclude the bottom right corner
        ) {
            return true;
        }

        // check row and column
        if (a / size == b / size) {
            return true;
        }
        if (a % size == b % size) {
            return true;
        }
        return false;
    }

    private static int[] generateDifficulty(int size, int difficulty, RandomSource rand) {
        final int[] layout = new int[size * size];

        final int amountOfVeryHard, amountOfHard, amountOfMedium, amountOfEasy;
        switch (difficulty) {
            case 1 -> {
                amountOfVeryHard = 0;
                amountOfHard = 0;
                amountOfMedium = 0;
                amountOfEasy = rand.nextInt(size * size * 3 / 5, size * size * 3 / 5 + size);
            }
            case 2 -> {
                amountOfVeryHard = 0;
                amountOfHard = 0;
                amountOfMedium = rand.nextInt(size * size * 3 / 5, size * size * 3 / 5 + size);
                amountOfEasy = size * size - amountOfMedium;
            }
            case 3 -> {
                amountOfVeryHard = 0;
                amountOfHard = rand.nextInt(size * size * 3 / 5, size * size * 3 / 5 + size);
                amountOfMedium = size * size - amountOfHard;
                amountOfEasy = size * size - amountOfHard - amountOfMedium;
            }
            case 4 -> {
                amountOfVeryHard = rand.nextInt(size * size * 3 / 5, size * size * 3 / 5 + size);
                amountOfHard = size * size - amountOfVeryHard;
                amountOfMedium = size * size - amountOfHard - amountOfVeryHard;
                amountOfEasy = size * size - amountOfHard - amountOfMedium - amountOfVeryHard;
            }
            default -> {
                amountOfVeryHard = 0;
                amountOfHard = 0;
                amountOfMedium = 0;
                amountOfEasy = 0;
            }
        }

        distributeDifficulty(size, layout, amountOfVeryHard, 4, rand);
        distributeDifficulty(size, layout, amountOfHard, 3, rand);
        distributeDifficulty(size, layout, amountOfMedium, 2, rand);
        distributeDifficulty(size, layout, amountOfEasy, 1, rand);

        return layout;
    }

    private static void distributeDifficulty(int size, int[] layout, int amount, int difficulty, RandomSource rand) {
        for (int i = 0; i < amount; i++) {
            boolean cont;
            int failSafe = 0;

            do {
                cont = true;
                failSafe++;

                final int rng = rand.nextInt(size * size);
                if (layout[rng] == 0) {
                    layout[rng] = difficulty;
                } else {
                    cont = false;
                    if (failSafe >= 500) break;
                }
            } while (!cont);
        }
    }

    public int getSize() {
        return size;
    }

    public Teams getState(int x, int y) {
        return states[getIndex(x, y)];
    }

    public ActiveGoal getGoal(int x, int y) {
        return goals[getIndex(x, y)];
    }

    private int getIndex(int x, int y) {
        return y * size + x;
    }

    public Teams[] getStates() {
        return states;
    }

    public ActiveGoal[] getGoals() {
        return goals;
    }

    @Nullable
    public ActiveGoal byVanillaId(ResourceLocation id) {
        return byVanillaId.get(id);
    }

    public int getIndex(ActiveGoal goal) {
        return toGoalIndex.getInt(goal);
    }

    @Override
    @SuppressWarnings("deprecation")
    public String toString() {
        final int boxWidth = 20;
        final int boxHeight = 7;

        final StringBuilder result = new StringBuilder((boxWidth * size + 1) * (boxHeight + 1));

        for (int y = 0; y < size; y++) {
            for (int column = 0; column < size; column++) {
                result.append('+');
                result.append("-".repeat(boxWidth - 1));
            }
            result.append("+\n");

            final String[][] texts = new String[size][];
            for (int x = 0; x < size; x++) {
                texts[x] = WordUtils.wrap(getGoal(x, y).getName().getString(), boxWidth - 3, "\n", true).split("\n");
            }

            for (int line = 0; line < boxHeight - 1; line++) {
                for (int x = 0; x < size; x++) {
                    final String box = line < texts[x].length ? texts[x][line] : "";
                    result.append("| ").append(box).append(" ".repeat(boxWidth - box.length() - 2));
                }
                result.append("|\n");
            }
        }

        for (int column = 0; column < size; column++) {
            result.append('+');
            result.append("-".repeat(boxWidth - 1));
        }
        result.append('+');

        return result.toString();
    }

    public static ResourceLocation generateVanillaId(int index) {
        return new ResourceLocation("bingo:generated/goal/" + index);
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
