package io.github.gaming32.bingo.fabric.datagen;

import io.github.gaming32.bingo.fabric.datagen.goal.BingoGoalProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class BingoDataGenFabric implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(BingoGoalProvider::new);
        pack.addProvider(BingoTagProvider::new);
        pack.addProvider(BingoItemTagProvider::new);
        pack.addProvider(BingoBlockTagProvider::new);
        pack.addProvider(BingoFeatureTagProvider::new);
    }
}
