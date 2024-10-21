package io.github.gaming32.bingo.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderType;

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
    public void renderImage(Font font, int x, int y, int width, int height, GuiGraphics guiGraphics) {
        Minecraft.getInstance().getTextureManager().getTexture(tooltip.icon()).setFilter(true, false);
        guiGraphics.blit(RenderType::guiTextured, tooltip.icon(), x, y, SIZE, SIZE, 0, 0, SIZE, SIZE, SIZE, SIZE);
    }
}
