package io.github.gaming32.bingo.data.tags.bingo;

import io.github.gaming32.bingo.util.ResourceLocations;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class BingoBlockTags {
    private BingoBlockTags() {
    }

    public static final TagKey<Block> ALL_MINERAL_BLOCKS = create("all_mineral_blocks");
    public static final TagKey<Block> BASIC_MINERAL_BLOCKS = create("basic_mineral_blocks");
    public static final TagKey<Block> COPPER_BLOCKS = create("copper_blocks");

    private static TagKey<Block> create(String name) {
        return TagKey.create(Registries.BLOCK, ResourceLocations.bingo(name));
    }
}
