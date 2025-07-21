package io.github.gaming32.bingo.data.goal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.data.BingoRegistries;
import io.github.gaming32.bingo.util.ResourceLocations;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GoalManager extends SimpleJsonResourceReloadListener<BingoGoal> {
    public static final ResourceLocation ID = ResourceLocations.bingo("goals");

    private static Map<ResourceLocation, GoalHolder> goals = Map.of();
    private static Map<Integer, List<GoalHolder>> goalsByDifficulty = Map.of();
    private final HolderLookup.Provider registries;

    public GoalManager(HolderLookup.Provider registries) {
        super(registries, BingoGoal.CODEC, BingoRegistries.GOAL);
        this.registries = registries;
    }

    public static Set<ResourceLocation> getGoalIds() {
        return goals.keySet();
    }

    @Nullable
    public static GoalHolder getGoal(ResourceLocation id) {
        return goals.get(id);
    }

    public static List<GoalHolder> getGoalsByDifficulty(int difficulty) {
        if (difficulty < 0) {
            throw new IllegalArgumentException("Difficulties < 0 aren't allowed");
        }
        return goalsByDifficulty.getOrDefault(difficulty, List.of());
    }

    @NotNull
    @Override
    public String getName() {
        return ID.toString();
    }

    @Override
    protected void apply(Map<ResourceLocation, BingoGoal> goals, ResourceManager resourceManager, ProfilerFiller profiler) {
        final ImmutableMap.Builder<ResourceLocation, GoalHolder> result = ImmutableMap.builderWithExpectedSize(goals.size());
        final Map<Integer, ImmutableList.Builder<GoalHolder>> byDifficulty = HashMap.newHashMap(
            (int) registries.lookupOrThrow(BingoRegistries.DIFFICULTY).listElements().count()
        );
        for (final var entry : goals.entrySet()) {
            final var goal = entry.getValue();
            final GoalHolder holder = new GoalHolder(entry.getKey(), goal);
            validate(holder);
            result.put(holder.id(), holder);
            byDifficulty.computeIfAbsent(goal.getDifficulty().value().number(), k -> ImmutableList.builder()).add(holder);
        }
        GoalManager.goals = result.build();
        GoalManager.goalsByDifficulty = byDifficulty.entrySet()
            .stream()
            .collect(ImmutableMap.toImmutableMap(
                Map.Entry::getKey,
                e -> e.getValue().build()
            ));
        Bingo.LOGGER.info("Loaded {} bingo goals", GoalManager.goals.size());
    }

    private void validate(GoalHolder holder) {
        final var collector = new ProblemReporter.Collector();
        holder.goal().validateParsedCriteria(collector, registries);
        if (!collector.isEmpty()) {
            Bingo.LOGGER.warn("Found validation problems in goal {}:\n{}", holder.id(), collector.getReport());
        }
    }
}
