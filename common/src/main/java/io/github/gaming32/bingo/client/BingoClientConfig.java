package io.github.gaming32.bingo.client;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "bingo-client")
@Config.Gui.Background(Config.Gui.Background.TRANSPARENT)
public class BingoClientConfig implements ConfigData {
    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public BoardConfig board = new BoardConfig();

    public static class BoardConfig {
        @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
        public BoardCorner corner = BoardCorner.UPPER_RIGHT;

        public float scale = 1f;
    }
}
