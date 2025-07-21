package io.github.gaming32.bingo.fabric.datagen;

import io.github.gaming32.bingo.data.BingoDifficulties;
import io.github.gaming32.bingo.data.BingoRegistries;
import io.github.gaming32.bingo.data.BingoTags;
import io.github.gaming32.bingo.fabric.datagen.goal.BingoGoalProvider;
import io.github.gaming32.bingo.fabric.datagen.tag.BingoBlockTagProvider;
import io.github.gaming32.bingo.fabric.datagen.tag.BingoDamageTypeTagProvider;
import io.github.gaming32.bingo.fabric.datagen.tag.BingoEntityTypeTagProvider;
import io.github.gaming32.bingo.fabric.datagen.tag.BingoFeatureTagProvider;
import io.github.gaming32.bingo.fabric.datagen.tag.BingoItemTagProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.JsonKeySortOrderCallback;
import net.minecraft.core.RegistrySetBuilder;

public class BingoDataGenFabric implements DataGeneratorEntrypoint {
    private static final boolean DUMP_BINGO_COMMAND = false;

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(BingoGoalProvider::new);
        pack.addProvider(BingoDynamicRegistryProvider::new);

        final BingoBlockTagProvider blockTagProvider = pack.addProvider(BingoBlockTagProvider::new);
        pack.addProvider((output, registriesFuture) -> new BingoItemTagProvider(output, registriesFuture, blockTagProvider));
        pack.addProvider(BingoEntityTypeTagProvider::new);
        pack.addProvider(BingoFeatureTagProvider::new);
        pack.addProvider(BingoDamageTypeTagProvider::new);

        if (DUMP_BINGO_COMMAND) {
            pack.addProvider(BingoCommandDumper::new);
        }
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(BingoRegistries.TAG, BingoTags::bootstrap);
        registryBuilder.add(BingoRegistries.DIFFICULTY, BingoDifficulties::bootstrap);
    }

    @Override
    public void addJsonKeySortOrders(JsonKeySortOrderCallback callback) {
        callback.add("bingo_type", 0);
    }
}
