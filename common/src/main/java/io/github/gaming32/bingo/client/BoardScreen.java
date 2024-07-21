package io.github.gaming32.bingo.client;

import io.github.gaming32.bingo.game.BingoBoard;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.PlayerTeam;

public class BoardScreen extends Screen {
    private Button leftButton;
    private Button rightButton;

    public BoardScreen() {
        super(BingoClient.BOARD_TITLE);
    }

    @Override
    protected void init() {
        final int buttonY = height / 2 + BingoClient.getBoardHeight() / 2 + font.lineHeight + BingoClient.BOARD_OFFSET * 2;
        leftButton = addRenderableWidget(
            Button.builder(Component.literal("<"), b -> switchTeam(-1))
                .width(Button.DEFAULT_HEIGHT)
                .pos(width / 2 - Button.DEFAULT_HEIGHT - 4, buttonY)
                .build()
        );
        rightButton = addRenderableWidget(
            Button.builder(Component.literal(">"), b -> switchTeam(1))
                .width(Button.DEFAULT_HEIGHT)
                .pos(width / 2 + 4, buttonY)
                .build()
        );
        updateButtonVisibility();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        if (BingoClient.clientGame == null) return;
        final PositionAndScale pos = getPosition();
        BingoClient.renderBingo(graphics, true, pos);
        assert minecraft != null;
        if (minecraft.player != null && minecraft.player.isSpectator()) {
            final PlayerTeam team = BingoClient.clientGame.getTeams()[BingoClient.clientTeam.getFirstIndex()];
            final Integer color = team.getColor().getColor();
            graphics.drawCenteredString(
                font,
                BingoClient.getDisplayName(team),
                width / 2, (int)pos.y() + BingoClient.getBoardHeight() + BingoClient.BOARD_OFFSET,
                color != null ? color : 0xffffff
            );
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        assert minecraft != null;
        if (BingoClient.clientGame != null && BingoClient.detectPress(keyCode, scanCode, getPosition())) {
            return true;
        } else if (leftButton.visible && minecraft.options.keyLeft.matches(keyCode, scanCode)) {
            switchTeam(-1);
        } else if (rightButton.visible && minecraft.options.keyRight.matches(keyCode, scanCode)) {
            switchTeam(-1);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (BingoClient.clientGame != null && BingoClient.detectClick(button, getPosition())) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void tick() {
        if (minecraft == null) return;
        if (BingoClient.clientGame == null) {
            minecraft.setScreen(null);
        } else {
            updateButtonVisibility();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public PositionAndScale getPosition() {
        return new PositionAndScale(
            width / 2f - BingoClient.getBoardWidth() / 2f,
            height / 2f - BingoClient.getBoardHeight() / 2f,
            1f
        );
    }

    private void updateButtonVisibility() {
        assert minecraft != null;
        final boolean isSpectator = minecraft.player != null && minecraft.player.isSpectator();
        leftButton.visible = isSpectator;
        rightButton.visible = isSpectator;
    }

    private void switchTeam(int dir) {
        final int currentIndex = BingoClient.clientTeam.getFirstIndex();
        final int newIndex = Math.floorMod(currentIndex + dir, BingoClient.clientGame.getTeams().length);
        BingoClient.clientTeam = BingoBoard.Teams.fromOne(newIndex);
    }
}
