package io.github.gaming32.bingo.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;

public record ClientIconTooltip(IconTooltip tooltip) implements ClientTooltipComponent {
    private static final int SIZE = 64;

    @Override
    public int getHeight(Font font) {
        return SIZE;
    }

    @Override
    public int getWidth(Font font) {
        return SIZE;
    }

    @Override
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor guiGraphics) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, tooltip.icon(), x, y, 0, 0, SIZE, SIZE, SIZE, SIZE);
    }
}
