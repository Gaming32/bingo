package io.github.gaming32.bingo.data.goal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.data.BingoRegistries;
import io.github.gaming32.bingo.platform.BingoPlatform;
import io.github.gaming32.bingo.util.Identifiers;
import io.github.gaming32.bingo.util.Key;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ProblemReporter;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoalManager {
    public static final Identifier ID = Identifiers.bingo("goals");
    public static final Key<GoalManager> KEY = Key.create("goals");

    private final HolderLookup.RegistryLookup<BingoGoal> goals;
    private final Map<Integer, List<GoalHolder>> goalsByDifficulty;

    public GoalManager(HolderLookup.Provider registries) {
        this.goals = registries.lookupOrThrow(BingoRegistries.GOAL);
        final Map<Integer, ImmutableList.Builder<GoalHolder>> byDifficulty = HashMap.newHashMap(
            (int) registries.lookupOrThrow(BingoRegistries.DIFFICULTY).listElements().count()
        );
        MutableInt goalCount = new MutableInt();
        goals.listElements().forEach(entry -> {
            final var goal = entry.value();
            final GoalHolder holder = new GoalHolder(entry.key().identifier(), goal);
            validate(registries, holder);
            byDifficulty.computeIfAbsent(goal.getDifficulty().value().number(), _ -> ImmutableList.builder()).add(holder);
            goalCount.increment();
        });
        goalsByDifficulty = byDifficulty.entrySet()
            .stream()
            .collect(ImmutableMap.toImmutableMap(
                Map.Entry::getKey,
                e -> e.getValue().build()
            ));
        Bingo.LOGGER.info("Loaded {} bingo goals", goalCount);
    }

    public static GoalManager getInstance(MinecraftServer server) {
        return BingoPlatform.getDataStore(server).getOrThrow(KEY);
    }

    @Nullable
    public GoalHolder getGoal(Identifier id) {
        final var goalRef = goals.get(ResourceKey.create(BingoRegistries.GOAL, id));
        return goalRef.map(bingoGoalReference -> new GoalHolder(id, bingoGoalReference.value())).orElse(null);
    }

    public List<GoalHolder> getGoalsByDifficulty(int difficulty) {
        if (difficulty < 0) {
            throw new IllegalArgumentException("Difficulties < 0 aren't allowed");
        }
        return goalsByDifficulty.getOrDefault(difficulty, List.of());
    }

    private void validate(HolderLookup.Provider registries, GoalHolder holder) {
        final var collector = new ProblemReporter.Collector();
        holder.goal().validateParsedCriteria(collector, registries);
        if (!collector.isEmpty()) {
            Bingo.LOGGER.warn("Found validation problems in goal {}:\n{}", holder.id(), collector.getReport());
        }
    }
}
