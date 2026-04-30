package io.github.gaming32.bingo.client.icons.pip;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public record BlockPictureInPictureRenderState(
    BlockModelRenderState modelRenderState,
    BlockState block,
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
        BlockModelRenderState modelRenderState = new BlockModelRenderState();
        Minecraft.getInstance().blockModelResolver.update(modelRenderState, block, BlockDisplayContext.create());
        return new BlockPictureInPictureRenderState(modelRenderState, block, x0, x1, y0, y1, scale, scissorArea, PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea));
    }
}
