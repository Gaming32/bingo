package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.SpriteIcon;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public class SpriteIconRenderer implements IconRenderer<SpriteIcon> {
    @Override
    public void render(SpriteIcon icon, GuiGraphicsExtractor graphics, int x, int y) {
        final var sprite = icon.sprite().withPrefix("bingo/icon/");
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 16, 16);
    }
}
