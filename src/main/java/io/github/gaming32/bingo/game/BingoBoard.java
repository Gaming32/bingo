package io.github.gaming32.bingo.game;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.data.BingoDifficulty;
import io.github.gaming32.bingo.data.BingoRegistries;
import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.data.goal.GoalHolder;
import io.github.gaming32.bingo.data.goal.GoalManager;
import io.github.gaming32.bingo.network.messages.both.ManualHighlightPayload;
import io.github.gaming32.bingo.util.BingoUtil;
import io.github.gaming32.bingo.util.ResourceLocations;
import io.github.gaming32.bingo.util.Vec2i;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import org.apache.commons.lang3.text.WordUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.OptionalInt;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class BingoBoard {
    public static final int DEFAULT_SIZE = 5;
    public static final int NUM_MANUAL_HIGHLIGHT_COLORS = 3;

    public static final Codec<BingoBoard> PERSISTENCE_CODEC = PartiallyParsed.CODEC.xmap(
        BingoBoard::create, PartiallyParsed::create
    );

    private final BoardShape shape;
    private final int size;
    private final Teams[] states;
    private final @Nullable Integer[][] manualHighlights;
    private final int[] manualHighlightModCount;
    private final ActiveGoal[] goals;
    private final Map<ResourceLocation, ActiveGoal> byVanillaId;
    private final Object2IntMap<ActiveGoal> toGoalIndex;

    private BingoBoard(BoardShape shape, int size, int teamCount) {
        this.shape = shape;
        this.size = size;
        final int goalCount = shape.getGoalCount(size);
        this.states = new Teams[goalCount];
        this.manualHighlights = new Integer[teamCount][goalCount];
        this.manualHighlightModCount = new int[teamCount];
        this.goals = new ActiveGoal[goalCount];
        this.byVanillaId = HashMap.newHashMap(goalCount);
        this.toGoalIndex = new Object2IntOpenHashMap<>(goalCount);

        Arrays.fill(states, Teams.NONE);
        toGoalIndex.defaultReturnValue(-1);
    }

    private static BingoBoard create(PartiallyParsed parsed) {
        final var size = parsed.size;
        final int goalCount = parsed.shape.getGoalCount(size);
        final BingoBoard board = new BingoBoard(parsed.shape, size, parsed.manualHighlights.size());
        parsed.states.toArray(board.states);
        parsed.goals.toArray(board.goals);
        for (int teamIndex = 0; teamIndex < parsed.manualHighlights.size(); teamIndex++) {
            for (int i = 0; i < goalCount; i++) {
                OptionalInt manualHighlight = parsed.manualHighlights.get(teamIndex).get(i);
                board.manualHighlights[teamIndex][i] = manualHighlight.isEmpty() ? null : manualHighlight.getAsInt();
            }
        }
        for (int i = 0; i < goalCount; i++) {
            final ActiveGoal goal = board.goals[i];
            board.byVanillaId.put(generateVanillaId(i), goal);
            board.toGoalIndex.put(goal, i);
        }
        return board;
    }

    public static BingoBoard generate(
        BoardShape shape,
        int size,
        int difficulty,
        int teamCount,
        RandomSource rand,
        Predicate<GoalHolder> isAllowedGoal,
        Collection<GoalHolder> requiredGoals,
        HolderSet<BingoTag> excludedTags,
        boolean allowsClientRequired,
        HolderLookup.Provider registries
    ) {
        final int goalCount = shape.getGoalCount(size);
        final BingoBoard board = new BingoBoard(shape, size, teamCount);
        final GoalHolder[] generatedSheet = generateGoals(
            registries.lookupOrThrow(BingoRegistries.DIFFICULTY),
            shape,
            size,
            difficulty,
            rand,
            isAllowedGoal,
            requiredGoals,
            excludedTags,
            allowsClientRequired
        );
        for (int i = 0; i < goalCount; i++) {
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
        BoardShape shape,
        int size,
        int difficulty,
        RandomSource rand,
        Predicate<GoalHolder> isAllowedGoal,
        Collection<GoalHolder> requiredGoals,
        HolderSet<BingoTag> excludedTags,
        boolean allowsClientRequired
    ) {
        final Queue<GoalHolder> requiredGoalQueue = new ArrayDeque<>(requiredGoals);

        final int goalCount = shape.getGoalCount(size);
        final GoalHolder[] generatedSheet = new GoalHolder[goalCount];

        final Int2ObjectMap<List<int[]>> linesForCells = new Int2ObjectOpenHashMap<>();
        for (final int[] line : shape.getLines(size)) {
            for (final int cell : line) {
                linesForCells.computeIfAbsent(cell, k -> new ArrayList<>()).add(line);
            }
        }

        final var difficulties = BingoDifficulty.getNumbers(difficultyLookup);
        final int[] difficultyLayout = generateDifficulty(difficulties, goalCount, difficulty, rand);
        final int[] indices = BingoUtil.shuffle(BingoUtil.generateIntArray(goalCount), rand);

        final Set<ResourceLocation> usedGoals = HashSet.newHashSet(goalCount);
        final Object2IntOpenHashMap<Holder<BingoTag>> tagCount = new Object2IntOpenHashMap<>();
        final Set<String> antisynergys = new HashSet<>();
        final Set<String> reactants = new HashSet<>();
        final Set<String> catalysts = new HashSet<>();

        for (final var tag : excludedTags) {
            tagCount.put(tag, tag.value().getMaxForDifficulty(difficulty, goalCount));
        }

        for (int i = 0; i < goalCount; i++) {
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

                if (goalCandidate.goal().getTags().size() != 0) {
                    for (final var tag : goalCandidate.goal().getTags()) {
                        if (tagCount.getInt(tag) >= tag.value().getMaxForDifficulty(difficulty, goalCount)) {
                            continue goalGen;
                        }
                    }

                    if (goalCandidate.goal().getTags().stream().anyMatch(t -> !t.value().allowedOnSameLine())) {
                        for (final int[] line : linesForCells.get(indices[i])) {
                            for (final int cell : line) {
                                if (cell != indices[i] && generatedSheet[cell] != null) {
                                    final var tags = generatedSheet[cell].goal().getTags();
                                    if (tags.size() > 0) {
                                        if (tags.stream().anyMatch(t ->
                                            !t.value().allowedOnSameLine() && goalCandidate.goal().getTags().contains(t)
                                        )) {
                                            continue goalGen;
                                        }
                                    }
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

    private static int[] generateDifficulty(NavigableSet<Integer> difficulties, int goalCount, int difficulty, RandomSource rand) {
        final int[] layout = new int[goalCount];

        final Iterator<Integer> available = difficulties.headSet(difficulty, true).descendingIterator();
        if (!available.hasNext()) {
            throw new IllegalArgumentException("No difficulty exists with number " + difficulty);
        }
        final int difficulty1 = available.next();
        if (!available.hasNext()) {
            Arrays.fill(layout, difficulty1);
        } else {
            final int difficulty2 = available.next();
            final int amountOf1 = rand.nextInt(goalCount * 3 / 5, goalCount * 3 / 5 + (int) Math.sqrt(goalCount));
            final int[] indices = BingoUtil.shuffle(BingoUtil.generateIntArray(goalCount), rand);
            Arrays.fill(layout, difficulty2);
            for (int i = 0; i < amountOf1; i++) {
                layout[indices[i]] = difficulty1;
            }
        }
        return layout;
    }

    public BoardShape getShape() {
        return shape;
    }

    public int getSize() {
        return size;
    }

    public @Nullable Integer[] getTeamManualHighlights(Teams team) {
        Preconditions.checkArgument(team.one(), "Team must be a single team");
        return manualHighlights[team.getFirstIndex()];
    }

    public int getManualHighlightModCount(Teams team) {
        Preconditions.checkArgument(team.one(), "Team must be a single team");
        return manualHighlightModCount[team.getFirstIndex()];
    }

    public void setTeamManualHighlight(MinecraftServer server, BingoGame game, Teams team, int slot, @Nullable Integer value, @Nullable ServerPlayer cause) {
        Preconditions.checkArgument(team.one(), "Team must be a single team");
        int modCount = ++manualHighlightModCount[team.getFirstIndex()];
        manualHighlights[team.getFirstIndex()][slot] = value;

        ManualHighlightPayload clientboundPacket = new ManualHighlightPayload(slot, value == null ? 0 : value + 1, modCount);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player == cause) {
                continue;
            }
            Teams playerTeam = game.getTeam(player);
            if (playerTeam.and(team)) {
                clientboundPacket.sendTo(player);
            }
        }
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

        final Vec2i visualSize = shape.getVisualSize(size);
        final StringBuilder result = new StringBuilder();

        for (int y = 0; y < visualSize.y(); y++) {
            for (int column = 0; column < visualSize.x(); column++) {
                result.append('+').repeat('-', boxWidth - 1);
            }
            result.append("+\n");

            final String[][] texts = new String[visualSize.x()][];
            for (int x = 0; x < visualSize.x(); x++) {
                final int goalIndex = shape.getCellFromCoords(size, x, y);
                if (goalIndex == -1) {
                    texts[x] = new String[0];
                } else {
                    texts[x] = WordUtils.wrap(goals[goalIndex].name().getString(), boxWidth - 3, "\n", true).split("\n");
                }
            }

            for (int line = 0; line < boxHeight - 1; line++) {
                for (int x = 0; x < visualSize.x(); x++) {
                    final String box = line < texts[x].length ? texts[x][line] : "";
                    result.append("| ").append(box).repeat(' ', boxWidth - box.length() - 2);
                }
                result.append("|\n");
            }
        }

        for (int column = 0; column < visualSize.x(); column++) {
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

    private record PartiallyParsed(BoardShape shape, int size, List<Teams> states, List<List<OptionalInt>> manualHighlights, List<ActiveGoal> goals) {
        static final Codec<PartiallyParsed> CODEC = RecordCodecBuilder.<PartiallyParsed>create(
            instance -> instance.group(
                BoardShape.CODEC.optionalFieldOf("shape", BoardShape.SQUARE).forGetter(PartiallyParsed::shape),
                ExtraCodecs.POSITIVE_INT.fieldOf("size").forGetter(PartiallyParsed::size),
                Teams.CODEC.listOf().fieldOf("states").forGetter(PartiallyParsed::states),
                Codec.INT.xmap(i -> i == 0 ? OptionalInt.empty() : OptionalInt.of(i - 1), i -> i.isEmpty() ? 0 : i.getAsInt() + 1)
                    .listOf()
                    .listOf()
                    .fieldOf("manual_highlights").forGetter(PartiallyParsed::manualHighlights),
                ActiveGoal.PERSISTENCE_CODEC.listOf().fieldOf("goals").forGetter(PartiallyParsed::goals)
            ).apply(instance, PartiallyParsed::new)
        ).validate(PartiallyParsed::validate);

        static PartiallyParsed create(BingoBoard board) {
            List<List<OptionalInt>> manualHighlights = Arrays.stream(board.manualHighlights)
                .map(teamHighlights -> Arrays.stream(teamHighlights).map(i -> i == null ? OptionalInt.empty() : OptionalInt.of(i)).toList())
                .toList();
            return new PartiallyParsed(board.shape, board.size, List.of(board.states), manualHighlights, List.of(board.goals));
        }

        private DataResult<PartiallyParsed> validate() {
            // fixedSize does create a shortened partial result, but we only care about it having a partial, as the
            // shortening is also handled in create() above.
            final int goalCount = shape.getGoalCount(size);
            var result = DataResult.success(this);
            result = BingoUtil.combineError(result, Util.fixedSize(states, goalCount));
            result = BingoUtil.combineError(result, Util.fixedSize(goals, goalCount));
            for (List<OptionalInt> teamHighlight : manualHighlights) {
                result = BingoUtil.combineError(result, Util.fixedSize(teamHighlight, goalCount));
            }
            return result;
        }
    }
}
