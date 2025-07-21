package io.github.gaming32.bingo.fabric.datagen.tag;

import io.github.gaming32.bingo.data.tags.bingo.BingoDamageTypeTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;

import java.util.concurrent.CompletableFuture;

public class BingoDamageTypeTagProvider extends FabricTagProvider<DamageType> {
    public BingoDamageTypeTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.DAMAGE_TYPE, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        builder(BingoDamageTypeTags.VOID).add(
            DamageTypes.FELL_OUT_OF_WORLD
        );
        builder(BingoDamageTypeTags.BERRY_BUSH).add(
            DamageTypes.SWEET_BERRY_BUSH
        );
    }
}
