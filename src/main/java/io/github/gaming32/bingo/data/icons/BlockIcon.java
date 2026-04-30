package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.util.BingoCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public record BlockIcon(BlockState block, ItemStackTemplate item) implements GoalIcon.WithoutContext {
    public static final MapCodec<BlockIcon> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            BlockState.CODEC.fieldOf("block").forGetter(BlockIcon::block),
            BingoCodecs.LENIENT_ITEM_STACK_TEMPLATE.optionalFieldOf("item").forGetter(i -> Optional.of(i.item))
        ).apply(instance, BlockIcon::ofFallbackItem)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockIcon> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), BlockIcon::block,
        ItemStackTemplate.STREAM_CODEC, BlockIcon::item,
        BlockIcon::new
    );

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static BlockIcon ofFallbackItem(BlockState block, Optional<ItemStackTemplate> item) {
        return new BlockIcon(block, item.orElseGet(() -> stackFromBlock(block.getBlock())));
    }

    public static BlockIcon ofBlockAndItem(Block block, ItemLike item) {
        return new BlockIcon(block.defaultBlockState(), new ItemStackTemplate(item.asItem()));
    }

    public static BlockIcon ofBlock(BlockState block) {
        return new BlockIcon(block, stackFromBlock(block.getBlock()));
    }

    public static BlockIcon ofBlock(Block block) {
        return new BlockIcon(block.defaultBlockState(), stackFromBlock(block));
    }

    private static ItemStackTemplate stackFromBlock(Block block) {
        return new ItemStackTemplate(block.asItem() != Items.AIR ? block.asItem() : Items.STONE);
    }

    @Override
    public ItemStackTemplate getFallback() {
        return item;
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.BLOCK.get();
    }
}
