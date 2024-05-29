package io.github.gaming32.bingo.datagen;

import io.github.gaming32.bingo.data.BingoDifficulties;
import io.github.gaming32.bingo.data.BingoDifficulty;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class BingoDifficultyProvider extends FabricCodecDataProvider<BingoDifficulty> {
    public BingoDifficultyProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, PackOutput.Target.DATA_PACK, "bingo/difficulties", BingoDifficulty.CODEC);
    }

    @Override
    @NotNull
    public String getName() {
        return "Bingo difficulties";
    }

    @Override
    protected void configure(BiConsumer<ResourceLocation, BingoDifficulty> adder, HolderLookup.Provider registries) {
        BingoDifficulty.builder(BingoDifficulties.VERY_EASY)
            .number(0)
            .build(adder);
        BingoDifficulty.builder(BingoDifficulties.EASY)
            .number(1)
            .build(adder);
        BingoDifficulty.builder(BingoDifficulties.MEDIUM)
            .number(2)
            .build(adder);
        BingoDifficulty.builder(BingoDifficulties.HARD)
            .number(3)
            .build(adder);
        BingoDifficulty.builder(BingoDifficulties.VERY_HARD)
            .number(4)
            .build(adder);
    }
}
