package io.github.gaming32.bingo.fabric.datagen.tag;

import io.github.gaming32.bingo.data.tags.BingoPaintingVariantTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.decoration.PaintingVariant;

import java.util.concurrent.CompletableFuture;

public class BingoPaintingVariantTagProvider extends FabricTagProvider<PaintingVariant> {
    public BingoPaintingVariantTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.PAINTING_VARIANT, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        for (PaintingVariant paintingVariant : BuiltInRegistries.PAINTING_VARIANT) {
            int w = paintingVariant.getWidth();
            int h = paintingVariant.getHeight();
            if (w == 16 && h == 16) {
                getOrCreateTagBuilder(BingoPaintingVariantTags.SIZE_1X1).add(paintingVariant);
            } else if (w == 32 && h == 16) {
                getOrCreateTagBuilder(BingoPaintingVariantTags.SIZE_2X1).add(paintingVariant);
            } else if (w == 16 && h == 32) {
                getOrCreateTagBuilder(BingoPaintingVariantTags.SIZE_1X2).add(paintingVariant);
            } else if (w == 32 && h == 32) {
                getOrCreateTagBuilder(BingoPaintingVariantTags.SIZE_2X2).add(paintingVariant);
            } else if (w == 64 && h == 32) {
                getOrCreateTagBuilder(BingoPaintingVariantTags.SIZE_4X2).add(paintingVariant);
            } else if (w == 64 && h == 48) {
                getOrCreateTagBuilder(BingoPaintingVariantTags.SIZE_4X3).add(paintingVariant);
            } else if (w == 64 && h == 64) {
                getOrCreateTagBuilder(BingoPaintingVariantTags.SIZE_4X4).add(paintingVariant);
            }
        }
    }
}
