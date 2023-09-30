package io.github.gaming32.bingo.client.icons;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.gaming32.bingo.data.icons.BlockIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

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
        graphics.pose().mulPoseMatrix(new Matrix4f().scaling(1f, -1f, 1f));
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
        renderModelLists(model, state, poseStack, buffer.getBuffer(ItemBlockRenderTypes.getRenderType(state, true)));
        poseStack.popPose();
    }

    private static void renderModelLists(BakedModel model, BlockState state, PoseStack poseStack, VertexConsumer buffer) {
        final RandomSource random = RandomSource.create();
        final long seed = 42;
        for (final Direction direction : Direction.values()) {
            random.setSeed(seed);
            renderQuadList(poseStack, buffer, model.getQuads(state, direction, random));
        }
        random.setSeed(seed);
        renderQuadList(poseStack, buffer, model.getQuads(state, null, random));
    }

    private static void renderQuadList(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads) {
        final PoseStack.Pose pose = poseStack.last();
        for (final BakedQuad quad : quads) {
            buffer.putBulkData(pose, quad, 1f, 1f, 1f, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        }
    }
}
