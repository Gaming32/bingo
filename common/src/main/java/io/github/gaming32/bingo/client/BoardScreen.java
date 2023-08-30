package io.github.gaming32.bingo.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class BoardScreen extends Screen {
    public BoardScreen() {
        super(BingoClient.BOARD_TITLE);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        BingoClient.renderBingo(
            graphics, true,
            width / 2f - BingoClient.BOARD_WIDTH / 2f,
            height / 2f - BingoClient.BOARD_HEIGHT / 2f,
            1f
        );
    }

    @Override
    public void tick() {
        if (BingoClient.clientBoard == null && minecraft != null) {
            minecraft.setScreen(null);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
