package io.github.gaming32.bingo.fabric.datagen.tag;

import io.github.gaming32.bingo.data.tags.BingoPaintingVariantTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.decoration.PaintingVariant;

import java.util.concurrent.CompletableFuture;

public class BingoPaintingVariantTagProvider extends FabricTagProvider<PaintingVariant> {
    public BingoPaintingVariantTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.PAINTING_VARIANT, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        registries.lookupOrThrow(Registries.PAINTING_VARIANT).listElements().forEach(variant -> {
            int w = variant.value().width();
            int h = variant.value().height();
            getOrCreateTagBuilder(BingoPaintingVariantTags.create("size_" + w + "x" + h)).add(variant.key());
        });
    }
}
