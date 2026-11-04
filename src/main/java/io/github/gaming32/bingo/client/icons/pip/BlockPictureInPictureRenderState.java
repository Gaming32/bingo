package io.github.gaming32.bingo.client.icons.pip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public record BlockPictureInPictureRenderState(
    BlockModelRenderState modelRenderState,
    BlockState block,
    boolean animated,
    int x0,
    int x1,
    int y0,
    int y1,
    float scale,
    @Nullable ScreenRectangle scissorArea,
    @Nullable ScreenRectangle bounds
) implements PictureInPictureRenderState {
    public static BlockPictureInPictureRenderState create(
        BlockState block,
        int x0,
        int x1,
        int y0,
        int y1,
        float scale,
        @Nullable ScreenRectangle scissorArea
    ) {
        final Minecraft minecraft = Minecraft.getInstance();
        BlockModelRenderState modelRenderState = new BlockModelRenderState();
        minecraft.getBlockEntityRenderDispatcher().blockModelResolver.update(modelRenderState, block, BlockDisplayContext.create());
        final boolean animated = minecraft.getModelManager().getBlockStateModelSet().get(block).hasMaterialFlag(BakedQuad.FLAG_ANIMATED);
        return new BlockPictureInPictureRenderState(modelRenderState, block, animated, x0, x1, y0, y1, scale, scissorArea, PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea));
    }
}
