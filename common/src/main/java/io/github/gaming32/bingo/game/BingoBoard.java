package io.github.gaming32.bingo.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.data.BingoDifficulty;
import io.github.gaming32.bingo.data.BingoRegistries;
import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.data.goal.GoalHolder;
import io.github.gaming32.bingo.data.goal.GoalManager;
import io.github.gaming32.bingo.util.BingoUtil;
import io.github.gaming32.bingo.util.ResourceLocations;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.IntStream;

public class BingoBoard {
    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 7;
    public static final int DEFAULT_SIZE = 5;

    public static final Codec<BingoBoard> PERSISTENCE_CODEC = PartiallyParsed.CODEC.xmap(
        BingoBoard::create, PartiallyParsed::create
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
        this.byVanillaId = HashMap.newHashMap(size * size);
        this.toGoalIndex = new Object2IntOpenHashMap<>(size * size);

        Arrays.fill(states, Teams.NONE);
        toGoalIndex.defaultReturnValue(-1);
    }

    private static BingoBoard create(PartiallyParsed parsed) {
        final var size = parsed.size;
        final BingoBoard board = new BingoBoard(size);
        parsed.states.toArray(board.states);
        parsed.goals.toArray(board.goals);
        for (int i = 0; i < size * size; i++) {
            final ActiveGoal goal = board.goals[i];
            board.byVanillaId.put(generateVanillaId(i), goal);
            board.toGoalIndex.put(goal, i);
        }
        return board;
    }

    public static BingoBoard generate(
        int size,
        int difficulty,
        int teamCount,
        RandomSource rand,
        BiPredicate<GoalHolder, Boolean> isAllowedGoal,
        Collection<GoalHolder> requiredGoals,
        HolderSet<BingoTag> excludedTags,
        boolean allowNeverGoalsInLockout,
        boolean allowsClientRequired,
        HolderLookup.Provider registries
    ) {
        final BingoBoard board = new BingoBoard(size);
        final GoalHolder[] generatedSheet = generateGoals(
            registries.lookupOrThrow(BingoRegistries.DIFFICULTY),
            size,
            difficulty,
            rand,
            isAllowedGoal,
            requiredGoals,
            excludedTags,
            allowNeverGoalsInLockout,
            allowsClientRequired
        );
        for (int i = 0; i < size * size; i++) {
            final ActiveGoal goal;
            try {
                goal = board.goals[i] = generatedSheet[i].build(rand);
            } catch (IllegalArgumentException e) {
                throw new InvalidGoalException(generatedSheet[i].id(), e);
            }
            goal.validateAndLog(registries);
            if (generatedSheet[i].goal().getSpecialType() == BingoTag.SpecialType.NEVER) {
                board.states[i] = Teams.fromAll(teamCount);
            }
            board.byVanillaId.put(generateVanillaId(i), goal);
            board.toGoalIndex.put(goal, i);
        }
        return board;
    }

    public static GoalHolder[] generateGoals(
        HolderLookup<BingoDifficulty> difficultyLookup,
        int size,
        int difficulty,
        RandomSource rand,
        BiPredicate<GoalHolder, Boolean> isAllowedGoal,
        Collection<GoalHolder> requiredGoals,
        HolderSet<BingoTag> excludedTags,
        boolean allowNeverGoalsInLockout,
        boolean allowsClientRequired
    ) {
        final Queue<GoalHolder> requiredGoalQueue = new ArrayDeque<>(requiredGoals);

        final GoalHolder[] generatedSheet = new GoalHolder[size * size];

        final var difficulties = BingoDifficulty.getNumbers(difficultyLookup);
        final int[] difficultyLayout = generateDifficulty(difficulties, size, difficulty, rand);
        final int[] indices = BingoUtil.shuffle(BingoUtil.generateIntArray(size * size), rand);

        final Set<ResourceLocation> usedGoals = HashSet.newHashSet(size * size);
        final Object2IntOpenHashMap<Holder<BingoTag>> tagCount = new Object2IntOpenHashMap<>();
        final Set<String> antisynergys = new HashSet<>();
        final Set<String> reactants = new HashSet<>();
        final Set<String> catalysts = new HashSet<>();

        for (final var tag : excludedTags) {
            tagCount.put(tag, tag.value().getMaxForDifficulty(difficulty, size));
        }

        for (int i = 0; i < size * size; i++) {
            final var difficultiesToTry = difficulties.headSet(difficultyLayout[i], true).descendingIterator();
            if (!difficultiesToTry.hasNext()) {
                throw new IllegalArgumentException("No goals with difficulty " + difficultyLayout[i] + " or easier");
            }

            List<GoalHolder> possibleGoals = GoalManager.getGoalsByDifficulty(difficultyLayout[i] = difficultiesToTry.next());

            int failSafe = 0;
            GoalHolder goal;

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

                    possibleGoals = GoalManager.getGoalsByDifficulty(difficultyLayout[i] = difficultiesToTry.next());
                    failSafe = 1;
                }

                final GoalHolder goalCandidate = possibleGoals.get(rand.nextInt(possibleGoals.size()));

                if (!isAllowedGoal.test(goalCandidate, allowNeverGoalsInLockout)) {
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

                if (goalCandidate.goal().getTags().size() != 0) {
                    for (final var tag : goalCandidate.goal().getTags()) {
                        if (tagCount.getInt(tag) >= tag.value().getMaxForDifficulty(difficulty, size)) {
                            continue goalGen;
                        }
                    }

                    if (goalCandidate.goal().getTags().stream().anyMatch(t -> !t.value().allowedOnSameLine())) {
                        for (int z = 0; z < i; z++) {
                            final var tags = generatedSheet[indices[z]].goal().getTags();
                            if (tags.size() > 0 && isOnSameLine(size, indices[i], indices[z])) {
                                if (tags.stream().anyMatch(t ->
                                    !t.value().allowedOnSameLine() && goalCandidate.goal().getTags().contains(t)
                                )) {
                                    continue goalGen;
                                }
                            }
                        }
                    }
                }

                if (!Collections.disjoint(antisynergys, goalCandidate.goal().getAntisynergy())) continue;
                if (!Collections.disjoint(reactants, goalCandidate.goal().getCatalyst())) continue;
                if (!Collections.disjoint(catalysts, goalCandidate.goal().getReactant())) continue;

                goal = goalCandidate;
                break;
            }

            for (final var tag : goal.goal().getTags()) {
                tagCount.addTo(tag, 1);
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

    private static int[] generateDifficulty(NavigableSet<Integer> difficulties, int size, int difficulty, RandomSource rand) {
        final int[] layout = new int[size * size];

        final Iterator<Integer> available = difficulties.headSet(difficulty, true).descendingIterator();
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
                result.append('+').repeat('-', boxWidth - 1);
            }
            result.append("+\n");

            final String[][] texts = new String[size][];
            for (int x = 0; x < size; x++) {
                texts[x] = WordUtils.wrap(getGoal(x, y).name().getString(), boxWidth - 3, "\n", true).split("\n");
            }

            for (int line = 0; line < boxHeight - 1; line++) {
                for (int x = 0; x < size; x++) {
                    final String box = line < texts[x].length ? texts[x][line] : "";
                    result.append("| ").append(box).repeat(' ', boxWidth - box.length() - 2);
                }
                result.append("|\n");
            }
        }

        for (int column = 0; column < size; column++) {
            result.append('+').repeat('-', boxWidth - 1);
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

    private record PartiallyParsed(int size, List<Teams> states, List<ActiveGoal> goals) {
        static final Codec<PartiallyParsed> CODEC = RecordCodecBuilder.<PartiallyParsed>create(
            instance -> instance.group(
                Codec.INT.fieldOf("size").forGetter(PartiallyParsed::size),
                Teams.CODEC.listOf().fieldOf("states").forGetter(PartiallyParsed::states),
                ActiveGoal.PERSISTENCE_CODEC.listOf().fieldOf("goals").forGetter(PartiallyParsed::goals)
            ).apply(instance, PartiallyParsed::new)
        ).validate(PartiallyParsed::validate);

        static PartiallyParsed create(BingoBoard board) {
            return new PartiallyParsed(board.size, List.of(board.states), List.of(board.goals));
        }

        private DataResult<PartiallyParsed> validate() {
            // fixedSize does create a shortened partial result, but we only care about it having a partial, as the
            // shortening is also handled in create() above.
            var result = DataResult.success(this);
            result = BingoUtil.combineError(result, Util.fixedSize(states, size * size));
            result = BingoUtil.combineError(result, Util.fixedSize(goals, size * size));
            return result;
        }
    }
}
