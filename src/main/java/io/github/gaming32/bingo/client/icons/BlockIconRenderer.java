package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.client.icons.pip.BlockPictureInPictureRenderState;
import io.github.gaming32.bingo.data.icons.BlockIcon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;

public class BlockIconRenderer implements IconRenderer<BlockIcon> {
    @Override
    public void render(BlockIcon icon, GuiGraphics graphics, int x, int y) {
        graphics.guiRenderState.submitPicturesInPictureState(new BlockPictureInPictureRenderState(
            icon.block(),
            x,
            x + 16,
            y,
            y + 16,
            1,
            new ScreenRectangle(x, y, 16, 16),
            new ScreenRectangle(x, y, 16, 16)
        ));
    }
}
