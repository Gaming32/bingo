package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public record ItemIcon(ItemStack item) implements GoalIcon {
    public static final MapCodec<ItemIcon> CODEC = ItemStack.CODEC
        .xmap(ItemIcon::new, ItemIcon::item)
        .fieldOf("item");

    public static ItemIcon ofItem(ItemLike item) {
        return new ItemIcon(new ItemStack(item));
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.ITEM.get();
    }
}
