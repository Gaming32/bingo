package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.SpriteIcon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;

public class SpriteIconRenderer implements IconRenderer<SpriteIcon> {
    @Override
    public void render(SpriteIcon icon, GuiGraphics graphics, int x, int y) {
        final var sprite = icon.sprite().withPrefix("bingo/icon/");
        graphics.blitSprite(RenderType::guiTextured, sprite, x, y, 16, 16);
    }
}
