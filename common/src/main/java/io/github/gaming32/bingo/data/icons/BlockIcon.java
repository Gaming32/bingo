package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public record BlockIcon(BlockState block, ItemStack item) implements GoalIcon {
    public static final MapCodec<BlockIcon> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            BlockState.CODEC.fieldOf("block").forGetter(BlockIcon::block),
            ExtraCodecs.strictOptionalField(ItemStack.CODEC, "item").forGetter(i -> Optional.of(i.item))
        ).apply(instance, BlockIcon::ofFallbackItem)
    );

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static BlockIcon ofFallbackItem(BlockState block, Optional<ItemStack> item) {
        return new BlockIcon(block, item.orElseGet(() -> stackFromBlock(block.getBlock())));
    }

    public static BlockIcon ofBlock(BlockState block) {
        return new BlockIcon(block, stackFromBlock(block.getBlock()));
    }

    public static BlockIcon ofBlock(Block block) {
        return new BlockIcon(block.defaultBlockState(), stackFromBlock(block));
    }

    private static ItemStack stackFromBlock(Block block) {
        return new ItemStack(block.asItem() != Items.AIR ? block : Blocks.STONE);
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.BLOCK.get();
    }
}
