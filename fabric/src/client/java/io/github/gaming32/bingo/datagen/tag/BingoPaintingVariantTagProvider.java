package io.github.gaming32.bingo.datagen.tag;

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
            getOrCreateTagBuilder(BingoPaintingVariantTags.create("size_" + w / 16 + "x" + h / 16)).add(paintingVariant);
        }
    }
}
