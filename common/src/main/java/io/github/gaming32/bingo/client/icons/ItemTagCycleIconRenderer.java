package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.ItemTagCycleIcon;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class ItemTagCycleIconRenderer implements IconRenderer<ItemTagCycleIcon> {
    @Override
    public void render(ItemTagCycleIcon icon, GuiGraphics graphics, int x, int y) {
        final Optional<HolderSet.Named<Item>> items = BuiltInRegistries.ITEM.getTag(icon.tag());
        if (items.isEmpty()) return;
        graphics.renderFakeItem(new ItemStack(getIcon(items.get())), x, y);
    }

    @Override
    public void renderDecorations(ItemTagCycleIcon icon, Font font, GuiGraphics graphics, int x, int y) {
        if (icon.count() == 1) return;
        final String text = Integer.toString(icon.count());
        graphics.pose().pushPose();
        graphics.pose().translate(0f, 0f, 200f);
        graphics.drawString(font, text, x + 19 - 2 - font.width(text), y + 6 + 3, 0xffffff, true);
        graphics.pose().popPose();
    }

    private static Holder<Item> getIcon(HolderSet.Named<Item> icons) {
        return icons.get((int)((Util.getMillis() / CycleIconRenderer.TIME_PER_ICON) % icons.size()));
    }
}
