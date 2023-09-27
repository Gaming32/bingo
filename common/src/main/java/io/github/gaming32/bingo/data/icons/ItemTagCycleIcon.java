package io.github.gaming32.bingo.data.icons;

import com.google.common.collect.Iterables;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record ItemTagCycleIcon(TagKey<Item> tag, int count) implements GoalIcon {
    public static final Codec<ItemTagCycleIcon> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(ItemTagCycleIcon::tag),
            ExtraCodecs.strictOptionalField(Codec.INT, "count", 1).forGetter(ItemTagCycleIcon::count)
        ).apply(instance, ItemTagCycleIcon::new)
    );

    public ItemTagCycleIcon(TagKey<Item> tag) {
        this(tag, 1);
    }

    @Override
    public ItemStack item() {
        return new ItemStack(Iterables.getFirst(BuiltInRegistries.ITEM.getTagOrEmpty(tag), Items.AIR.arch$holder()), count);
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.ITEM_TAG_CYCLE.get();
    }
}
