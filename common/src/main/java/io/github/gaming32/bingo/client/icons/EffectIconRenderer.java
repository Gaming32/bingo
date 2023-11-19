package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.EffectIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public class EffectIconRenderer implements IconRenderer<EffectIcon> {
    private static final ResourceLocation EFFECT_BACKGROUND_SPRITE = new ResourceLocation("hud/effect_background");

    @Override
    public void render(EffectIcon icon, GuiGraphics graphics, int x, int y) {
        graphics.blitSprite(EFFECT_BACKGROUND_SPRITE, x, y, 16, 16);

        TextureAtlasSprite effectAtlas = Minecraft.getInstance().getMobEffectTextures().get(icon.effect());
        graphics.setColor(1.0F, 1.0F, 1.0F, 1);
        graphics.blit(x + 2, y + 2, 0, 12, 12, effectAtlas);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
