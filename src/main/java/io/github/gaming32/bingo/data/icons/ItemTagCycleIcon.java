package io.github.gaming32.bingo.data.icons;

import com.google.common.collect.Iterables;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record ItemTagCycleIcon(TagKey<Item> tag, int count) implements GoalIcon.WithoutContext {
    public static final MapCodec<ItemTagCycleIcon> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(ItemTagCycleIcon::tag),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(ItemTagCycleIcon::count)
        ).apply(instance, ItemTagCycleIcon::new)
    );
    public static final StreamCodec<ByteBuf, ItemTagCycleIcon> STREAM_CODEC = StreamCodec.composite(
        TagKey.streamCodec(Registries.ITEM), ItemTagCycleIcon::tag,
        ByteBufCodecs.VAR_INT, ItemTagCycleIcon::count,
        ItemTagCycleIcon::new
    );

    public ItemTagCycleIcon(TagKey<Item> tag) {
        this(tag, 1);
    }

    @Override
    @SuppressWarnings("deprecation")
    public ItemStack getFallback() {
        return new ItemStack(
            Iterables.getFirst(BuiltInRegistries.ITEM.getTagOrEmpty(tag), Items.STONE.builtInRegistryHolder()),
            count
        );
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.ITEM_TAG_CYCLE.get();
    }
}
