package io.github.gaming32.bingo.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.data.BingoDifficulty;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.util.BingoCodecs;
import io.github.gaming32.bingo.util.BingoUtil;
import io.github.gaming32.bingo.util.ResourceLocations;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.HolderGetter;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class BingoBoard {
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 7;
    public static final int DEFAULT_SIZE = 5;

    public static final Codec<BingoBoard> PERSISTENCE_CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Codec.INT.fieldOf("size").forGetter(BingoBoard::getSize),
            BingoCodecs.array(Teams.CODEC, Teams.class).fieldOf("states").forGetter(BingoBoard::getStates),
            BingoCodecs.array(ActiveGoal.PERSISTENCE_CODEC, ActiveGoal.class).fieldOf("goals").forGetter(BingoBoard::getGoals)
        ).apply(instance, BingoBoard::create)
    );

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

    private static BingoBoard create(int size, Teams[] states, ActiveGoal[] goals) {
        final BingoBoard board = new BingoBoard(size);
        System.arraycopy(states, 0, board.states, 0, size * size);
        for (int i = 0; i < size * size; i++) {
            final ActiveGoal goal = board.goals[i] = goals[i]; // TODO: Fix AIOOBE in exceptional cases
            board.byVanillaId.put(generateVanillaId(i), goal);
            board.toGoalIndex.put(goal, i);
        }
        return board;
    }

    public static BingoBoard generate(
        int size,
        BingoDifficulty difficulty,
        int teamCount,
        RandomSource rand,
        Predicate<BingoGoal.Holder> isAllowedGoal,
        List<BingoGoal.Holder> requiredGoals,
        Set<BingoTag.Holder> excludedTags,
        boolean allowsClientRequired,
        @Nullable HolderGetter.Provider registries
    ) {
        final BingoBoard board = new BingoBoard(size);
        final BingoGoal.Holder[] generatedSheet = generateGoals(
            size, difficulty, rand, isAllowedGoal, requiredGoals, excludedTags, allowsClientRequired
        );
        for (int i = 0; i < size * size; i++) {
            final ActiveGoal goal;
            try {
                goal = board.goals[i] = generatedSheet[i].build(rand);
            } catch (IllegalArgumentException e) {
                throw new InvalidGoalException(generatedSheet[i].id(), e);
            }
            if (registries != null) {
                goal.validateAndLog(registries);
            }
            if (generatedSheet[i].goal().getSpecialType() == BingoTag.SpecialType.NEVER) {
                board.states[i] = Teams.fromAll(teamCount);
            }
            board.byVanillaId.put(generateVanillaId(i), goal);
            board.toGoalIndex.put(goal, i);
        }
        return board;
    }

    public static BingoGoal.Holder[] generateGoals(
        int size,
        BingoDifficulty difficulty,
        RandomSource rand,
        Predicate<BingoGoal.Holder> isAllowedGoal,
        List<BingoGoal.Holder> requiredGoals,
        Set<BingoTag.Holder> excludedTags,
        boolean allowsClientRequired
    ) {
        final Queue<BingoGoal.Holder> requiredGoalQueue = new ArrayDeque<>(requiredGoals);

        final BingoGoal.Holder[] generatedSheet = new BingoGoal.Holder[size * size];

        final int[] difficultyLayout = generateDifficulty(size, difficulty, rand);
        final int[] indices = BingoUtil.shuffle(BingoUtil.generateIntArray(size * size), rand);

        final Set<ResourceLocation> usedGoals = new HashSet<>();
        final Object2IntOpenHashMap<ResourceLocation> tagCount = new Object2IntOpenHashMap<>();
        final Set<String> antisynergys = new HashSet<>();
        final Set<String> reactants = new HashSet<>();
        final Set<String> catalysts = new HashSet<>();

        for (final BingoTag.Holder tag : excludedTags) {
            tagCount.put(tag.id(), tag.tag().getMaxForDifficulty(difficulty.number(), size));
        }

        for (int i = 0; i < size * size; i++) {
            final Iterator<Integer> difficultiesToTry = BingoDifficulty.getNumbers()
                .headSet(difficultyLayout[i], true)
                .descendingIterator();
            if (!difficultiesToTry.hasNext()) {
                throw new IllegalArgumentException("No goals with difficulty " + difficultyLayout[i] + " or easier");
            }

            List<BingoGoal.Holder> possibleGoals = BingoGoal.getGoalsByDifficulty(difficultyLayout[i] = difficultiesToTry.next());

            int failSafe = 0;
            BingoGoal.Holder goal;

            goalGen:
            while (true) {
                if (requiredGoalQueue.peek() != null) {
                    goal = requiredGoalQueue.remove();
                    break;
                }

                failSafe++;

                while (failSafe >= 500 || possibleGoals.isEmpty()) {
                    if (!difficultiesToTry.hasNext()) {
                        throw new IllegalArgumentException("No valid board layout was found for the specified size and difficulty");
                    }

                    possibleGoals = BingoGoal.getGoalsByDifficulty(difficultyLayout[i] = difficultiesToTry.next());
                    failSafe = 1;
                }

                final BingoGoal.Holder goalCandidate = possibleGoals.get(rand.nextInt(possibleGoals.size()));

                if (!isAllowedGoal.test(goalCandidate)) {
                    continue;
                }

                if (!allowsClientRequired && goalCandidate.goal().isRequiredOnClient()) {
                    continue;
                }

                if (goalCandidate.goal().getInfrequency().isPresent()) {
                    final int infrequency = goalCandidate.goal().getInfrequency().getAsInt();
                    if (rand.nextInt(infrequency) + 1 < infrequency) {
                        continue;
                    }
                }

                if (usedGoals.contains(goalCandidate.id())) {
                    continue;
                }

                if (!goalCandidate.goal().getTags().isEmpty()) {
                    for (final BingoTag.Holder tag : goalCandidate.goal().getTags()) {
                        if (tagCount.getInt(tag.id()) >= tag.tag().getMaxForDifficulty(difficulty.number(), size)) {
                            continue goalGen;
                        }
                    }

                    if (goalCandidate.goal().getTags().stream().anyMatch(t -> !t.tag().allowedOnSameLine())) {
                        for (int z = 0; z < i; z++) {
                            final Set<BingoTag.Holder> tags = generatedSheet[indices[z]].goal().getTags();
                            if (!tags.isEmpty() && isOnSameLine(size, indices[i], indices[z])) {
                                if (tags.stream().anyMatch(t ->
                                    !t.tag().allowedOnSameLine() &&
                                        goalCandidate.goal().getTags().stream().anyMatch(t2 -> t.id().equals(t2.id()))
                                )) {
                                    continue goalGen;
                                }
                            }
                        }
                    }
                }

                if (!goalCandidate.goal().getAntisynergy().isEmpty()) {
                    if (goalCandidate.goal().getAntisynergy().stream().anyMatch(antisynergys::contains)) {
                        continue;
                    }
                }
                if (!goalCandidate.goal().getCatalyst().isEmpty()) {
                    if (goalCandidate.goal().getCatalyst().stream().anyMatch(reactants::contains)) {
                        continue;
                    }
                }
                if (!goalCandidate.goal().getReactant().isEmpty()) {
                    if (goalCandidate.goal().getReactant().stream().anyMatch(catalysts::contains)) {
                        continue;
                    }
                }

                goal = goalCandidate;
                break;
            }

            for (final BingoTag.Holder tag : goal.goal().getTags()) {
                tagCount.addTo(tag.id(), 1);
            }
            antisynergys.addAll(goal.goal().getAntisynergy());
            catalysts.addAll(goal.goal().getCatalyst());
            reactants.addAll(goal.goal().getReactant());

            usedGoals.add(goal.id());
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

    private static int[] generateDifficulty(int size, BingoDifficulty difficulty, RandomSource rand) {
        final int[] layout = new int[size * size];

        if (difficulty.distribution() != null) {
            List<Integer> scaledDistribution = difficulty.distribution().stream().map(f -> Math.round(f * size * size)).toList();
            int p = 0;
            for (int difficultyLevel = 0; difficultyLevel < scaledDistribution.size(); ++difficultyLevel) {
                for (int i = 0; i < scaledDistribution.get(difficultyLevel) && p < layout.length; ++i)
                    layout[p++] = difficultyLevel;
            }
            BingoUtil.shuffle(layout, rand);
        } else {
            final Iterator<Integer> available = BingoDifficulty.getNumbers()
                    .headSet(difficulty.number(), true)
                    .descendingIterator();
            if (!available.hasNext()) {
                throw new IllegalArgumentException("No difficulty exists with number " + difficulty);
            }
            final int difficulty1 = available.next();
            if (!available.hasNext()) {
                Arrays.fill(layout, difficulty1);
            } else {
                final int difficulty2 = available.next();
                final int amountOf1 = rand.nextInt(size * size * 3 / 5, size * size * 3 / 5 + size);
                final int[] indices = BingoUtil.shuffle(BingoUtil.generateIntArray(size * size), rand);
                Arrays.fill(layout, difficulty2);
                for (int i = 0; i < amountOf1; i++) {
                    layout[indices[i]] = difficulty1;
                }
            }
        }
        return layout;
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
                texts[x] = WordUtils.wrap(getGoal(x, y).name().getString(), boxWidth - 3, "\n", true).split("\n");
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
        return ResourceLocations.bingo("generated/goal/" + index);
    }

    public static final class Teams {
        public static final Teams NONE = new Teams(0);
        public static final Teams TEAM1 = new Teams(0b01);
        public static final Teams TEAM2 = new Teams(0b10);
        private static final Teams[] CACHE = {NONE, TEAM1, TEAM2, new Teams(0b11)};

        public static final Codec<Teams> CODEC = Codec.INT.xmap(Teams::fromBits, Teams::toBits);
        public static final StreamCodec<ByteBuf, Teams> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(Teams::fromBits, Teams::toBits);

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
            return count() >= count;
        }

        public boolean one() {
            return count() == 1;
        }

        public int count() {
            return Integer.bitCount(bits);
        }

        public int getFirstIndex() {
            return Integer.numberOfTrailingZeros(bits);
        }

        public IntStream stream() {
            return IntStream.iterate(bits, i -> i != 0, i -> i & ~(1 << Integer.numberOfTrailingZeros(i)))
                .map(Integer::numberOfTrailingZeros);
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
