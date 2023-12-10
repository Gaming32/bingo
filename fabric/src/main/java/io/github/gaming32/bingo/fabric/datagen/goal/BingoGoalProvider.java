package io.github.gaming32.bingo.fabric.datagen.goal;

import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.util.BingoUtil;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
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

    public BingoGoalProvider(FabricDataOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "bingo/goals");
    }

    @Override
    @NotNull
    public CompletableFuture<?> run(CachedOutput output) {
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

        addGoals(goalAdder);

        return CompletableFuture.allOf(generators.toArray(CompletableFuture[]::new));
    }

    @Override
    @NotNull
    public String getName() {
        return "Bingo goals";
    }

    private void addGoals(Consumer<BingoGoal.Holder> goalAdder) {
        new VeryEasyGoalProvider(goalAdder).addGoals();
        new EasyGoalProvider(goalAdder).addGoals();
        new MediumGoalProvider(goalAdder).addGoals();
        new HardGoalProvider(goalAdder).addGoals();
        new VeryHardGoalProvider(goalAdder).addGoals();
    }
}
