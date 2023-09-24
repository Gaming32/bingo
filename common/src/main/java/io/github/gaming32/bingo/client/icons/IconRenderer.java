package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.GoalIcon;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public interface IconRenderer<I extends GoalIcon> {
    void render(I icon, GuiGraphics graphics, int x, int y);

    default void renderDecorations(I icon, Font font, GuiGraphics graphics, int x, int y) {
        graphics.renderItemDecorations(font, icon.item(), x, y);
    }
}
