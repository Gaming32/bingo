package io.github.gaming32.bingo.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public record ClientIconTooltip(IconTooltip tooltip) implements ClientTooltipComponent {
    private static final int SIZE = 64;

    @Override
    public int getHeight() {
        return SIZE;
    }

    @Override
    public int getWidth(Font font) {
        return SIZE;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
        Minecraft.getInstance().getTextureManager().getTexture(tooltip.icon()).setFilter(true, false);
        guiGraphics.blit(tooltip.icon(), x, y, SIZE, SIZE, 0f, 0f, SIZE, SIZE, SIZE, SIZE);
    }
}
