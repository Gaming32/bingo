package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public record ItemIcon(ItemStack item) implements GoalIcon {
    public static final Codec<ItemIcon> CODEC = ItemStack.CODEC.xmap(ItemIcon::new, ItemIcon::item);

    @Override
    @Environment(EnvType.CLIENT)
    public void render(GuiGraphics graphics, int x, int y) {
        graphics.renderFakeItem(item, x, y);
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.ITEM.get();
    }
}
