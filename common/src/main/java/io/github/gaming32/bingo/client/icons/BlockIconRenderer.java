package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.data.icons.BlockIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class BlockIconRenderer implements IconRenderer<BlockIcon> {
    private static final ItemTransform DEFAULT_TRANSFORM = new ItemTransform(
        new Vector3f(30f, 225f, 0f),
        new Vector3f(0f, 0f, 0f),
        new Vector3f(0.625f, 0.625f, 0.625f)
    );

    @Override
    public void render(BlockIcon icon, GuiGraphics graphics, int x, int y) {
        final Minecraft minecraft = Minecraft.getInstance();
        graphics.pose().pushPose();
        graphics.pose().translate(x + 8f, y + 8f, 150f);
        graphics.pose().mulPose(new Matrix4f().scaling(1f, -1f, 1f));
        graphics.pose().scale(16f, 16f, 16f);
        graphics.drawSpecial(bufferSource -> {
            graphics.pose().pushPose();
            DEFAULT_TRANSFORM.apply(false, graphics.pose().last());
            graphics.pose().translate(-0.5f, -0.5f, -0.5f);
            minecraft.getBlockRenderer().renderSingleBlock(
                icon.block(), graphics.pose(), bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY
            );
            graphics.pose().popPose();
        });
        graphics.pose().popPose();
    }

}
