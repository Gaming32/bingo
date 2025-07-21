package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.EffectIcon;
import io.github.gaming32.bingo.util.ResourceLocations;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

public class EffectIconRenderer implements IconRenderer<EffectIcon> {
    private static final ResourceLocation EFFECT_BACKGROUND_SPRITE = ResourceLocations.minecraft("hud/effect_background");

    @Override
    public void render(EffectIcon icon, GuiGraphics graphics, int x, int y) {
        ResourceLocation sprite = Gui.getMobEffectSprite(icon.effect());
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, EFFECT_BACKGROUND_SPRITE, x, y, 16, 16);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x + 2, y + 2, 12, 12);
    }
}
