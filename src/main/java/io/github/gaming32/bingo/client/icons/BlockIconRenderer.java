package io.github.gaming32.bingo.client.icons;

import io.github.gaming32.bingo.client.icons.pip.BlockPictureInPictureRenderState;
import io.github.gaming32.bingo.data.icons.BlockIcon;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.joml.Vector2f;

public class BlockIconRenderer implements IconRenderer<BlockIcon> {
    @Override
    public void render(BlockIcon icon, GuiGraphicsExtractor graphics, int x, int y) {
        Vector2f topLeft = graphics.pose().transformPosition(x, y, new Vector2f());
        Vector2f bottomRight = graphics.pose().transformPosition(x + 16, y + 16, new Vector2f());

        // The scissor area must stay within the screen, or rendering will crash
        final ScreenRectangle scissorArea = new ScreenRectangle(
            (int) topLeft.x,
            (int) topLeft.y,
            (int) bottomRight.x - (int) topLeft.x,
            (int) bottomRight.y - (int) topLeft.y
        ).intersection(new ScreenRectangle(0, 0, graphics.guiWidth(), graphics.guiHeight()));
        if (scissorArea == null) {
            return;
        }

        graphics.guiRenderState.addPicturesInPictureState(BlockPictureInPictureRenderState.create(
            icon.block(),
            (int) topLeft.x,
            (int) bottomRight.x,
            (int) topLeft.y,
            (int) bottomRight.y,
            1,
            scissorArea
        ));
    }
}
