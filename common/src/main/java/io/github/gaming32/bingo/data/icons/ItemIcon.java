package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public record ItemIcon(ItemStack item) implements GoalIcon {
    public static final Codec<ItemIcon> CODEC = ItemStack.CODEC.xmap(ItemIcon::new, ItemIcon::item);

    public static ItemIcon ofItem(ItemLike item) {
        return new ItemIcon(new ItemStack(item));
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.ITEM.get();
    }
}
