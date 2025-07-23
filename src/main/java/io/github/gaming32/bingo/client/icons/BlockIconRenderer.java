package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.client.icons.pip.BlockPictureInPictureRenderState;
import io.github.gaming32.bingo.data.icons.BlockIcon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.joml.Vector2f;

public class BlockIconRenderer implements IconRenderer<BlockIcon> {
    @Override
    public void render(BlockIcon icon, GuiGraphics graphics, int x, int y) {
        Vector2f topLeft = graphics.pose().transformPosition(x, y, new Vector2f());
        Vector2f bottomRight = graphics.pose().transformPosition(x + 16, y + 16, new Vector2f());

        graphics.guiRenderState.submitPicturesInPictureState(new BlockPictureInPictureRenderState(
            icon.block(),
            (int) topLeft.x,
            (int) bottomRight.x,
            (int) topLeft.y,
            (int) bottomRight.y,
            1,
            new ScreenRectangle((int) topLeft.x, (int) topLeft.y, 16, 16),
            new ScreenRectangle((int) topLeft.x, (int) topLeft.y, 16, 16)
        ));
    }
}
