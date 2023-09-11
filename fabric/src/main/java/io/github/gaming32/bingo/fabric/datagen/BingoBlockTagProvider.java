package io.github.gaming32.bingo.fabric.datagen;

import io.github.gaming32.bingo.data.tags.BingoBlockTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

public class BingoBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public BingoBlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        getOrCreateTagBuilder(BingoBlockTags.COPPER_BLOCKS).add(
            Blocks.COPPER_BLOCK,
            Blocks.EXPOSED_COPPER,
            Blocks.WEATHERED_COPPER,
            Blocks.OXIDIZED_COPPER
        ).add(
            Blocks.WAXED_COPPER_BLOCK,
            Blocks.WAXED_EXPOSED_COPPER,
            Blocks.WAXED_WEATHERED_COPPER,
            Blocks.WAXED_OXIDIZED_COPPER
        );

        getOrCreateTagBuilder(BingoBlockTags.BASIC_MINERAL_BLOCKS).add(
            Blocks.IRON_BLOCK,
            Blocks.GOLD_BLOCK,
            Blocks.DIAMOND_BLOCK
        );

        getOrCreateTagBuilder(BingoBlockTags.ALL_MINERAL_BLOCKS).add(
            Blocks.COAL_BLOCK,
            Blocks.COPPER_BLOCK,
            Blocks.IRON_BLOCK,
            Blocks.GOLD_BLOCK,
            Blocks.DIAMOND_BLOCK,
            Blocks.REDSTONE_BLOCK,
            Blocks.LAPIS_BLOCK,
            Blocks.EMERALD_BLOCK,
            Blocks.QUARTZ_BLOCK,
            Blocks.NETHERITE_BLOCK
        );
    }
}
