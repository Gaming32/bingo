package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.GoalIcon;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public interface IconRenderer<I extends GoalIcon> {
    void render(I icon, GuiGraphics graphics, int x, int y);

    default void renderDecorations(I icon, Font font, GuiGraphics graphics, int x, int y) {
        graphics.renderItemDecorations(font, icon.item(), x, y);
    }

    static void renderCount(int count, Font font, GuiGraphics graphics, int x, int y) {
        if (count == 1) return;
        final String text = Integer.toString(count);
        graphics.pose().pushPose();
        graphics.pose().translate(0f, 0f, 200f);
        graphics.drawString(font, text, x + 19 - 2 - font.width(text), y + 6 + 3, 0xffffff, true);
        graphics.pose().popPose();
    }
}
