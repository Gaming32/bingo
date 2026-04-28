package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.GoalIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

public interface IconRenderer<I extends GoalIcon> {
    void render(I icon, GuiGraphicsExtractor graphics, int x, int y);

    default void renderDecorations(I icon, Font font, GuiGraphicsExtractor graphics, int x, int y) {
        graphics.itemDecorations(font, getIconItem(icon), x, y);
    }

    default ItemStack getIconItem(I icon) {
        final var connection = Minecraft.getInstance().getConnection();
        return connection != null ? icon.getFallback(connection.registryAccess()) : icon.getFallbackWithStaticContext();
    }

    static void renderCount(int count, Font font, GuiGraphicsExtractor graphics, int x, int y) {
        if (count == 1) return;
        final String text = Integer.toString(count);
        graphics.text(font, text, x + 19 - 2 - font.width(text), y + 6 + 3, 0xffffffff, true);
    }
}
