package io.github.gaming32.bingo.datagen.tag;

import io.github.gaming32.bingo.data.tags.bingo.BingoBlockTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.references.BlockItemIds;
import net.minecraft.world.level.block.BaseTorchBlock;

import java.util.concurrent.CompletableFuture;

public class BingoBlockTagProvider extends FabricTagsProvider.BlockTagsProvider {
    public BingoBlockTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        var copperBlocks = builder(BingoBlockTags.COPPER_BLOCKS);
        BlockItemIds.COPPER_BLOCK.forEach(copperBlocks::add);

        builder(BingoBlockTags.BASIC_MINERAL_BLOCKS).add(
            BlockItemIds.IRON_BLOCK.block(),
            BlockItemIds.GOLD_BLOCK.block(),
            BlockItemIds.DIAMOND_BLOCK.block()
        );

        builder(BingoBlockTags.ALL_MINERAL_BLOCKS).add(
            BlockItemIds.COAL_BLOCK.block(),
            BlockItemIds.COPPER_BLOCK.weathering().unaffected().block(),
            BlockItemIds.IRON_BLOCK.block(),
            BlockItemIds.GOLD_BLOCK.block(),
            BlockItemIds.DIAMOND_BLOCK.block(),
            BlockItemIds.REDSTONE_BLOCK.block(),
            BlockItemIds.LAPIS_BLOCK.block(),
            BlockItemIds.EMERALD_BLOCK.block(),
            BlockItemIds.QUARTZ_BLOCK.block(),
            BlockItemIds.NETHERITE_BLOCK.block()
        );

        var torches = builder(BingoBlockTags.TORCHES);

        arg.lookupOrThrow(Registries.BLOCK).listElements().forEach(block -> {
            if (block.value() instanceof BaseTorchBlock) {
                torches.add(block.key());
            }
        });
    }
}
