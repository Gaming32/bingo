package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.ItemIcon;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStackTemplate;

public class ItemIconRenderer implements IconRenderer<ItemIcon> {
    @Override
    public void render(ItemIcon icon, GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fakeItem(icon.item().withCount(1).create(), x, y);
    }

    @Override
    public ItemStackTemplate getIconItemTemplate(ItemIcon icon) {
        return icon.item();
    }
}
