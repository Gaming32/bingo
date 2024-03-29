package io.github.gaming32.bingo.fabric.datagen;

import io.github.gaming32.bingo.fabric.datagen.goal.BingoGoalProvider;
import io.github.gaming32.bingo.fabric.datagen.tag.BingoBlockTagProvider;
import io.github.gaming32.bingo.fabric.datagen.tag.BingoDamageTypeTagProvider;
import io.github.gaming32.bingo.fabric.datagen.tag.BingoEntityTypeTagProvider;
import io.github.gaming32.bingo.fabric.datagen.tag.BingoFeatureTagProvider;
import io.github.gaming32.bingo.fabric.datagen.tag.BingoItemTagProvider;
import io.github.gaming32.bingo.fabric.datagen.tag.BingoPaintingVariantTagProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class BingoDataGenFabric implements DataGeneratorEntrypoint {
    private static final boolean DUMP_BINGO_COMMAND = false;

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(BingoGoalProvider::new);
        pack.addProvider(BingoTagProvider::new);
        pack.addProvider(BingoDifficultyProvider::new);

        final BingoBlockTagProvider blockTagProvider = pack.addProvider(BingoBlockTagProvider::new);
        pack.addProvider((output, registriesFuture) -> new BingoItemTagProvider(output, registriesFuture, blockTagProvider));
        pack.addProvider(BingoEntityTypeTagProvider::new);
        pack.addProvider(BingoFeatureTagProvider::new);
        pack.addProvider(BingoDamageTypeTagProvider::new);
        pack.addProvider(BingoPaintingVariantTagProvider::new);

        if (DUMP_BINGO_COMMAND) {
            pack.addProvider(BingoCommandDumper::new);
        }
    }
}
