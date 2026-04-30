package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.GoalIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public interface IconRenderer<I extends GoalIcon> {
    void render(I icon, GuiGraphicsExtractor graphics, int x, int y);

    default void renderDecorations(I icon, Font font, GuiGraphicsExtractor graphics, int x, int y) {
        ItemStackTemplate iconItem = getIconItemTemplate(icon);
        graphics.itemDecorations(font, iconItem.withCount(1).create(), x, y);
        renderCount(iconItem.count(), font, graphics, x, y);
    }

    default ItemStack getIconItem(I icon) {
        return getIconItemTemplate(icon).withCount(1).create();
    }

    default ItemStackTemplate getIconItemTemplate(I icon) {
        final var connection = Minecraft.getInstance().getConnection();
        return connection != null ? icon.getFallback(connection.registryAccess()) : icon.getFallbackWithStaticContext();
    }

    static void renderCount(int count, Font font, GuiGraphicsExtractor graphics, int x, int y) {
        if (count == 1) return;
        final String text = Integer.toString(count);
        graphics.text(font, text, x + 19 - 2 - font.width(text), y + 6 + 3, 0xffffffff, true);
    }
}
