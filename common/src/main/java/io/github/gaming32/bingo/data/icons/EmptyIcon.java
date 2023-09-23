package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.Codec;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public enum EmptyIcon implements GoalIcon {
    INSTANCE;

    public static final Codec<EmptyIcon> CODEC = Codec.unit(INSTANCE);

    @Override
    public ItemStack item() {
        return ItemStack.EMPTY;
    }

    @Override
    public void render(GuiGraphics graphics, int x, int y) {
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.EMPTY.get();
    }
}
