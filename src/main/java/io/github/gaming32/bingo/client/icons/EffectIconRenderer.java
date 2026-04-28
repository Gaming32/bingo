package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.EffectIcon;
import io.github.gaming32.bingo.util.Identifiers;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class EffectIconRenderer implements IconRenderer<EffectIcon> {
    private static final Identifier EFFECT_BACKGROUND_SPRITE = Identifiers.minecraft("hud/effect_background");

    @Override
    public void render(EffectIcon icon, GuiGraphicsExtractor graphics, int x, int y) {
        Identifier sprite = Gui.getMobEffectSprite(icon.effect());
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, EFFECT_BACKGROUND_SPRITE, x, y, 16, 16);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x + 2, y + 2, 12, 12);
    }
}
