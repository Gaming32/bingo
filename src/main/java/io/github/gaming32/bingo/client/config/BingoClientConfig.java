package io.github.gaming32.bingo.client.config;

import com.electronwill.nightconfig.core.file.FileConfig;
import io.github.gaming32.bingo.game.BingoBoard;

import java.nio.file.Path;
import java.util.Arrays;

public class BingoClientConfig {
    private static final int[] DEFAULT_MANUAL_HIGHLIGHT_COLORS = { 0xff5050, 0x00af00, 0x5050ff };

    private final FileConfig config;

    private BoardCorner boardCorner = BoardCorner.UPPER_RIGHT;
    private float boardScale = 1f;
    private boolean showScoreCounter = true;
    private final int[] manualHighlightColors = new int[BingoBoard.NUM_MANUAL_HIGHLIGHT_COLORS];

    public BingoClientConfig(Path configPath) {
        config = FileConfig.of(configPath).checked();
        Arrays.setAll(manualHighlightColors, i -> DEFAULT_MANUAL_HIGHLIGHT_COLORS[i]);
    }

    public void load() {
        config.load();
        boardCorner = config.getEnumOrElse("board.corner", BoardCorner.UPPER_RIGHT);
        boardScale = config.getOrElse("board.scale", 1.0).floatValue();
        showScoreCounter = config.getOrElse("board.showScoreCounter", true);
        for (int value = 0; value < BingoBoard.NUM_MANUAL_HIGHLIGHT_COLORS; value++) {
            manualHighlightColors[value] = config.getIntOrElse("board.manualHighlightColor" + value, DEFAULT_MANUAL_HIGHLIGHT_COLORS[value]);
        }
    }

    public void save() {
        config.clear();
        config.set("board.corner", boardCorner.name());
        config.set("board.scale", boardScale);
        config.set("board.showScoreCounter", showScoreCounter);
        for (int value = 0; value < BingoBoard.NUM_MANUAL_HIGHLIGHT_COLORS; value++) {
            config.set("board.manualHighlightColor" + value, manualHighlightColors[value]);
        }
        config.save();
    }

    public void reset() {
        boardCorner = BoardCorner.UPPER_RIGHT;
        boardScale = 1f;
        showScoreCounter = true;
        Arrays.setAll(manualHighlightColors, i -> DEFAULT_MANUAL_HIGHLIGHT_COLORS[i]);
    }

    public BoardCorner getBoardCorner() {
        return boardCorner;
    }

    public void setBoardCorner(BoardCorner boardCorner) {
        this.boardCorner = boardCorner;
    }

    public float getBoardScale() {
        return boardScale;
    }

    public void setBoardScale(float boardScale) {
        this.boardScale = boardScale;
    }

    public boolean isShowScoreCounter() {
        return showScoreCounter;
    }

    public void setShowScoreCounter(boolean showScoreCounter) {
        this.showScoreCounter = showScoreCounter;
    }

    public int getManualHighlightColor(int value) {
        return manualHighlightColors[value];
    }

    public void setManualHighlightColor(int value, int color) {
        manualHighlightColors[value] = color;
    }
}
