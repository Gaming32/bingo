package io.github.gaming32.bingo.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class BoardScreen extends Screen {
    public BoardScreen() {
        super(BingoClient.BOARD_TITLE);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        BingoClient.renderBingo(
            graphics, true,
            width / 2f - BingoClient.getBoardWidth() / 2f,
            height / 2f - BingoClient.getBoardHeight() / 2f,
            1f
        );
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (BingoClient.detectPress(
            keyCode, scanCode,
            width / 2f - BingoClient.getBoardWidth() / 2f,
            height / 2f - BingoClient.getBoardHeight() / 2f,
            1f
        )) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (BingoClient.detectClick(
            button,
            width / 2f - BingoClient.getBoardWidth() / 2f,
            height / 2f - BingoClient.getBoardHeight() / 2f,
            1f
        )) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
