package io.github.gaming32.bingo.fabric.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.RegistriesDatapackGenerator;
import net.minecraft.resources.RegistryDataLoader;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class CustomRegistriesDatapackGenerator extends RegistriesDatapackGenerator {
    public CustomRegistriesDatapackGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    public abstract List<RegistryDataLoader.RegistryData<?>> getGeneratedRegistries();
}
