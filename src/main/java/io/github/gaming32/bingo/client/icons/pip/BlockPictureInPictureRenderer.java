package io.github.gaming32.bingo.client.icons.pip;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class BlockPictureInPictureRenderer extends PictureInPictureRenderer<BlockPictureInPictureRenderState> {
    private static final ItemTransform DEFAULT_TRANSFORM = new ItemTransform(
        new Vector3f(30f, 225f, 0f),
        new Vector3f(0f, 0f, 0f),
        new Vector3f(0.625f, 0.625f, 0.625f)
    );

    public BlockPictureInPictureRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    @NotNull
    public Class<BlockPictureInPictureRenderState> getRenderStateClass() {
        return BlockPictureInPictureRenderState.class;
    }

    @Override
    protected void renderToTexture(BlockPictureInPictureRenderState state, PoseStack pose) {
        pose.pushPose();
//        pose.translate(state.x0() + 1f, state.y0() + 4f, 150f);
//        pose.mulPose(new Matrix4f().scaling(1f, -1f, 1f));
//        pose.scale(16f, 16f, 16f);
        pose.pushPose();
        DEFAULT_TRANSFORM.apply(false, pose.last());
//        pose.translate(-0.5f, -0.5f, -0.5f);
        renderSingleBlock(state.block(), pose, bufferSource);
        pose.popPose();
        pose.popPose();
    }

    @Override
    @NotNull
    protected String getTextureLabel() {
        return "bingo block";
    }

    private static void renderSingleBlock(BlockState state, PoseStack poseStack, MultiBufferSource bufferSource) {
        final var minecraft = Minecraft.getInstance();
        final var dispatcher = minecraft.getBlockRenderer();
        final var model = dispatcher.getBlockModel(state);

        final int color = minecraft.getBlockColors().getColor(state, minecraft.level, BlockPos.ZERO, 0);
        final float r = (color >> 16 & 0xff) / 255f;
        final float g = (color >> 8 & 0xff) / 255f;
        final float b = (color & 0xff) / 255f;

        ModelBlockRenderer.renderModel(
            poseStack.last(),
            bufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(state)),
            model, r, g, b,
            LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY
        );
        minecraft.getModelManager()
            .specialBlockModelRenderer()
            .get()
            .renderByBlock(
                state.getBlock(), ItemDisplayContext.NONE, poseStack, bufferSource,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY
            );
    }
}
