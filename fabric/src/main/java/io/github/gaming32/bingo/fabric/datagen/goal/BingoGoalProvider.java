package io.github.gaming32.bingo.fabric.datagen.goal;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import io.github.gaming32.bingo.data.BingoGoal;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class BingoGoalProvider extends FabricCodecDataProvider<BingoGoal> {
    private static final List<GoalProviderProvider> PROVIDERS = List.of(
        VeryEasyGoalProvider::new,
        EasyGoalProvider::new,
        MediumGoalProvider::new,
        HardGoalProvider::new,
        VeryHardGoalProvider::new
    );

    public BingoGoalProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, PackOutput.Target.DATA_PACK, "bingo/goals", BingoGoal.CODEC);
    }

    @Override
    @NotNull
    public String getName() {
        return "Bingo goals";
    }

    @Override
    protected void configure(BiConsumer<ResourceLocation, BingoGoal> adder, HolderLookup.Provider registries) {
        final DynamicOps<JsonElement> oldOps = BingoGoal.Builder.JSON_OPS.get();
        try {
            BingoGoal.Builder.JSON_OPS.set(registries.createSerializationContext(oldOps));
            for (final GoalProviderProvider provider : PROVIDERS) {
                provider.create(adder, registries).addGoals();
            }
        } finally {
            BingoGoal.Builder.JSON_OPS.set(oldOps);
        }
    }

    @FunctionalInterface
    private interface GoalProviderProvider {
        DifficultyGoalProvider create(BiConsumer<ResourceLocation, BingoGoal> adder, HolderLookup.Provider registries);
    }
}
