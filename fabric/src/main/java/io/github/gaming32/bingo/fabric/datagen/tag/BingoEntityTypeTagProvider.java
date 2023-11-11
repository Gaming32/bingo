package io.github.gaming32.bingo.fabric.datagen.tag;

import io.github.gaming32.bingo.data.tags.BingoEntityTypeTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.EntityType;

import java.util.concurrent.CompletableFuture;

public class BingoEntityTypeTagProvider extends FabricTagProvider.EntityTypeTagProvider {
    public BingoEntityTypeTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        getOrCreateTagBuilder(BingoEntityTypeTags.TAMABLE).add(
            EntityType.ALLAY,
            EntityType.AXOLOTL,
            EntityType.CAMEL,
            EntityType.CAT,
            EntityType.DONKEY,
            EntityType.FOX,
            EntityType.HORSE,
            EntityType.LLAMA,
            EntityType.MULE,
            EntityType.OCELOT,
            EntityType.PARROT,
            EntityType.SKELETON_HORSE,
            EntityType.STRIDER,
            EntityType.TRADER_LLAMA,
            EntityType.WOLF
        );

        getOrCreateTagBuilder(BingoEntityTypeTags.BOATS).add(
            EntityType.BOAT,
            EntityType.CHEST_BOAT
        );
    }
}
