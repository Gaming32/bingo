package io.github.gaming32.bingo.fabric.datagen.goal;

import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.util.BingoUtil;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class BingoGoalProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;
    private final CompletableFuture<HolderLookup.Provider> registriesFuture;

    public BingoGoalProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "bingo/goals");
        this.registriesFuture = registriesFuture;
    }

    @Override
    @NotNull
    public CompletableFuture<?> run(CachedOutput output) {
        return registriesFuture.thenCompose(registries -> {
            Set<ResourceLocation> existingGoals = new HashSet<>();
            List<CompletableFuture<?>> generators = new ArrayList<>();

            Consumer<BingoGoal.Holder> goalAdder = goal -> {
                if (!existingGoals.add(goal.id())) {
                    throw new IllegalArgumentException("Duplicate goal " + goal.id());
                } else {
                    Path path = pathProvider.json(goal.id());
                    generators.add(DataProvider.saveStable(output, BingoUtil.toJsonElement(BingoGoal.CODEC, goal.goal()), path));
                }
            };

            addGoals(goalAdder, registries);

            return CompletableFuture.allOf(generators.toArray(CompletableFuture[]::new));
        });
    }

    @Override
    @NotNull
    public String getName() {
        return "Bingo goals";
    }

    private void addGoals(Consumer<BingoGoal.Holder> goalAdder, HolderLookup.Provider registries) {
        new VeryEasyGoalProvider(goalAdder).addGoals(registries);
        new EasyGoalProvider(goalAdder).addGoals(registries);
        new MediumGoalProvider(goalAdder).addGoals(registries);
        new HardGoalProvider(goalAdder).addGoals(registries);
        new VeryHardGoalProvider(goalAdder).addGoals(registries);
    }
}
