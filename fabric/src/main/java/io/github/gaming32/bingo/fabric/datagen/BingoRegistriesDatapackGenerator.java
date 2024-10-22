package io.github.gaming32.bingo.fabric.datagen;

import io.github.gaming32.bingo.data.BingoDifficulty;
import io.github.gaming32.bingo.data.BingoRegistries;
import io.github.gaming32.bingo.data.BingoTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.RegistriesDatapackGenerator;
import net.minecraft.resources.RegistryDataLoader;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BingoRegistriesDatapackGenerator extends CustomRegistriesDatapackGenerator {
    public BingoRegistriesDatapackGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public List<RegistryDataLoader.RegistryData<?>> getGeneratedRegistries() {
        return List.of(
            new RegistryDataLoader.RegistryData<>(BingoRegistries.TAG, BingoTag.CODEC, false),
            new RegistryDataLoader.RegistryData<>(BingoRegistries.DIFFICULTY, BingoDifficulty.CODEC, false)
        );
    }
}
