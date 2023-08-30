package io.github.gaming32.bingo.client;

import net.minecraft.client.gui.GuiGraphics;

public enum BoardCorner {
    UPPER_LEFT {
        @Override
        public float getX(GuiGraphics graphics, float scale) {
            return BingoClient.BOARD_OFFSET;
        }

        @Override
        public float getY(GuiGraphics graphics, float scale) {
            return BingoClient.BOARD_OFFSET;
        }
    },
    UPPER_RIGHT {
        @Override
        public float getX(GuiGraphics graphics, float scale) {
            return graphics.guiWidth() / scale - BingoClient.BOARD_OFFSET - BingoClient.BOARD_WIDTH;
        }

        @Override
        public float getY(GuiGraphics graphics, float scale) {
            return BingoClient.BOARD_OFFSET;
        }
    },
    LOWER_LEFT {
        @Override
        public float getX(GuiGraphics graphics, float scale) {
            return BingoClient.BOARD_OFFSET;
        }

        @Override
        public float getY(GuiGraphics graphics, float scale) {
            return graphics.guiHeight() / scale - BingoClient.BOARD_OFFSET - BingoClient.BOARD_HEIGHT;
        }
    },
    LOWER_RIGHT {
        @Override
        public float getX(GuiGraphics graphics, float scale) {
            return graphics.guiWidth() / scale - BingoClient.BOARD_OFFSET - BingoClient.BOARD_WIDTH;
        }

        @Override
        public float getY(GuiGraphics graphics, float scale) {
            return graphics.guiHeight() / scale - BingoClient.BOARD_OFFSET - BingoClient.BOARD_HEIGHT;
        }
    };

    public abstract float getX(GuiGraphics graphics, float scale);

    public abstract float getY(GuiGraphics graphics, float scale);
}
