package io.github.gaming32.bingo.client.icons;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.gaming32.bingo.data.icons.BlockIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
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
        final BakedModel model = minecraft.getBlockRenderer().getBlockModel(icon.block());
        graphics.pose().pushPose();
        graphics.pose().translate(x + 8f, y + 8f, 150f);
        graphics.pose().mulPose(new Matrix4f().scaling(1f, -1f, 1f));
        graphics.pose().scale(16f, 16f, 16f);
        final boolean flatLight = !model.usesBlockLight();
        if (flatLight) {
            Lighting.setupForFlatItems();
        }
        renderInGui(icon.block(), graphics.pose(), graphics.bufferSource(), model);
        graphics.flush();
        if (flatLight) {
            Lighting.setupFor3DItems();
        }
        graphics.pose().popPose();
    }

    private static void renderInGui(BlockState state, PoseStack poseStack, MultiBufferSource buffer, BakedModel model) {
        poseStack.pushPose();
        ItemTransform transform = model.getTransforms().gui;
        if (transform == ItemTransform.NO_TRANSFORM) {
            transform = DEFAULT_TRANSFORM;
        }
        transform.apply(false, poseStack);
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        renderModel(state, poseStack, buffer, model);
        poseStack.popPose();
    }

    private static void renderModel(BlockState state, PoseStack poseStack, MultiBufferSource buffer, BakedModel model) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (state.getRenderShape() != RenderShape.MODEL) {
            minecraft.getBlockRenderer().renderSingleBlock(
                state, poseStack, buffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY
            );
            return;
        }

        assert minecraft.level != null;
        final int color = minecraft.getBlockColors().getColor(state, minecraft.level, BlockPos.ZERO, 0);
        final float r = (color >> 16 & 0xff) / 255f;
        final float g = (color >> 8 & 0xff) / 255f;
        final float b = (color & 0xff) / 255f;
        minecraft.getBlockRenderer().getModelRenderer().renderModel(
            poseStack.last(),
            buffer.getBuffer(ItemBlockRenderTypes.getRenderType(state, false)),
            state, model, r, g, b, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY
        );
    }
}
