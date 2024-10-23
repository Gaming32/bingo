package io.github.gaming32.bingo.fabric.datagen;

import io.github.gaming32.bingo.data.BingoRegistries;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class BingoDynamicRegistryProvider extends FabricDynamicRegistryProvider {
    public BingoDynamicRegistryProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void configure(HolderLookup.Provider registries, Entries entries) {
        entries.addAll(registries.lookupOrThrow(BingoRegistries.TAG));
        entries.addAll(registries.lookupOrThrow(BingoRegistries.DIFFICULTY));
    }

    @NotNull
    @Override
    public String getName() {
        return "Bingo registries";
    }
}
