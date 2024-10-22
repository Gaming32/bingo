package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.ItemStack;

public enum EmptyIcon implements GoalIcon.WithoutContext {
    INSTANCE;

    public static final MapCodec<EmptyIcon> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public ItemStack getFallback() {
        return ItemStack.EMPTY;
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.EMPTY.get();
    }
}
