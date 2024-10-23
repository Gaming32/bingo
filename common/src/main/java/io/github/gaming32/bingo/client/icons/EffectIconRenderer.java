package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.EffectIcon;
import io.github.gaming32.bingo.util.ResourceLocations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class EffectIconRenderer implements IconRenderer<EffectIcon> {
    private static final ResourceLocation EFFECT_BACKGROUND_SPRITE = ResourceLocations.minecraft("hud/effect_background");

    @Override
    public void render(EffectIcon icon, GuiGraphics graphics, int x, int y) {
        var sprite = Minecraft.getInstance().getMobEffectTextures().get(icon.effect());
        graphics.blitSprite(RenderType::guiTextured, EFFECT_BACKGROUND_SPRITE, x, y, 16, 16);
        graphics.blitSprite(RenderType::guiTextured, sprite, x + 2, y + 2, 12, 12);
    }
}
