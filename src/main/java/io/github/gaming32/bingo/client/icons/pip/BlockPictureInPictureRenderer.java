package io.github.gaming32.bingo.client.icons.pip;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

public class BlockPictureInPictureRenderer extends PictureInPictureRenderer<BlockPictureInPictureRenderState> {
    private static final float RENDER_SIZE = 16F;
    private static final ItemTransform DEFAULT_TRANSFORM = new ItemTransform(
        new Vector3f(30f, 225f, 0f),
        new Vector3f(0f, 0f, 0f),
        new Vector3f(0.625f, 0.625f, 0.625f)
    );
    private static final Quaternionfc LIGHT_FIX_ROT = Axis.YP.rotationDegrees(285);

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
        Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);

        float scale = state.scale();
        pose.scale(RENDER_SIZE * scale, -RENDER_SIZE * scale, -RENDER_SIZE * scale);
        DEFAULT_TRANSFORM.apply(false, pose.last());
        pose.translate(.5, .5, .5);
        pose.last().normal().rotate(LIGHT_FIX_ROT);
        pose.translate(-.5, -.5, -.5);

        renderSingleBlock(state.block(), pose, bufferSource);
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

        SubmitNodeStorage submitNodeStorage = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher().getSubmitNodeStorage();

        minecraft.getModelManager()
            .specialBlockModelRenderer()
            .get()
            .renderByBlock(
                state.getBlock(), ItemDisplayContext.NONE, poseStack, submitNodeStorage,
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0
            );
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return (float) height / 2;
    }
}
