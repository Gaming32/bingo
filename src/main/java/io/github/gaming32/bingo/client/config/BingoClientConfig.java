package io.github.gaming32.bingo.client.config;

import com.electronwill.nightconfig.core.file.FileConfig;

import java.nio.file.Path;

public class BingoClientConfig {
    private final FileConfig config;

    private BoardCorner boardCorner = BoardCorner.UPPER_RIGHT;
    private float boardScale = 1f;
    private boolean showScoreCounter = true;

    public BingoClientConfig(Path configPath) {
        config = FileConfig.of(configPath).checked();
    }

    public void load() {
        config.load();
        boardCorner = config.getEnumOrElse("board.corner", BoardCorner.UPPER_RIGHT);
        boardScale = config.getOrElse("board.scale", 1.0).floatValue();
        showScoreCounter = config.getOrElse("board.showScoreCounter", true);
    }

    public void save() {
        config.clear();
        config.set("board.corner", boardCorner.name());
        config.set("board.scale", boardScale);
        config.set("board.showScoreCounter", showScoreCounter);
        config.save();
    }

    public void reset() {
        boardCorner = BoardCorner.UPPER_RIGHT;
        boardScale = 1f;
        showScoreCounter = true;
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
}
