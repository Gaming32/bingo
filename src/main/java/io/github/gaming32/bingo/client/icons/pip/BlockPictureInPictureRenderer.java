package io.github.gaming32.bingo.client.icons.pip;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

// If you don't want to use your own brain while porting, get inspired by what XFactHD does:
// https://github.com/XFactHD/FramedBlocks/blob/26.1/src/main/java/io/github/xfacthd/framedblocks/client/screen/pip/BlockPictureInPictureRenderer.java
public class BlockPictureInPictureRenderer extends PictureInPictureRenderer<BlockPictureInPictureRenderState> {
    private static final float RENDER_SIZE = 16F;
    private static final ItemTransform DEFAULT_TRANSFORM = new ItemTransform(
        new Vector3f(30f, 225f, 0f),
        new Vector3f(0f, 0f, 0f),
        new Vector3f(0.625f, 0.625f, 0.625f)
    );
    private static final Quaternionfc LIGHT_FIX_ROT = Axis.YP.rotationDegrees(285);

    @Nullable
    private BlockState lastBlockState;

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

        FeatureRenderDispatcher featureRenderDispatcher = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
        SubmitNodeStorage submitNodeStorage = featureRenderDispatcher.getSubmitNodeStorage();
        state.modelRenderState().submit(pose, submitNodeStorage, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
        featureRenderDispatcher.renderAllFeatures();
        bufferSource.endBatch();

        lastBlockState = state.block();
    }

    @Override
    protected boolean textureIsReadyToBlit(BlockPictureInPictureRenderState renderState) {
        return lastBlockState == renderState.block();
    }

    @Override
    @NotNull
    protected String getTextureLabel() {
        return "bingo block";
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return (float) height / 2;
    }
}
