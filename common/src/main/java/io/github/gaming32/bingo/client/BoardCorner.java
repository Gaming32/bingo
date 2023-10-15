package io.github.gaming32.bingo.client;

import me.shedaniel.clothconfig2.gui.entries.SelectionListEntry;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum BoardCorner implements SelectionListEntry.Translatable {
    UPPER_LEFT(false, false) {
        @Override
        public float getX(float guiWidth, float scale) {
            return BingoClient.BOARD_OFFSET;
        }

        @Override
        public float getY(float guiHeight, float scale) {
            return BingoClient.BOARD_OFFSET;
        }
    },
    UPPER_RIGHT(false, true) {
        @Override
        public float getX(float guiWidth, float scale) {
            return guiWidth / scale - BingoClient.BOARD_OFFSET - BingoClient.getBoardWidth();
        }

        @Override
        public float getY(float guiHeight, float scale) {
            final Minecraft minecraft = Minecraft.getInstance();
            float result = BingoClient.BOARD_OFFSET;
            if (minecraft.isDemo()) {
                result += 15;
            }
            return result;
        }
    },
    LOWER_LEFT(true, false) {
        @Override
        public float getX(float guiWidth, float scale) {
            return BingoClient.BOARD_OFFSET;
        }

        @Override
        public float getY(float guiHeight, float scale) {
            return guiHeight / scale - BingoClient.BOARD_OFFSET - BingoClient.getBoardHeight();
        }
    },
    LOWER_RIGHT(true, true) {
        @Override
        public float getX(float guiWidth, float scale) {
            return guiWidth / scale - BingoClient.BOARD_OFFSET - BingoClient.getBoardWidth();
        }

        @Override
        public float getY(float guiHeight, float scale) {
            return guiHeight / scale - BingoClient.BOARD_OFFSET - BingoClient.getBoardHeight();
        }
    };

    public final boolean isOnBottom, isOnRight;

    BoardCorner(boolean isOnBottom, boolean isOnRight) {
        this.isOnBottom = isOnBottom;
        this.isOnRight = isOnRight;
    }

    public abstract float getX(float guiWidth, float scale);

    public abstract float getY(float guiHeight, float scale);

    @NotNull
    @Override
    public String getKey() {
        return "bingo.board_corner." + name().toLowerCase(Locale.ROOT);
    }
}
