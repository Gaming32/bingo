package io.github.gaming32.bingo.fabric;

import io.github.gaming32.bingo.Bingo;
import net.fabricmc.api.ModInitializer;

public class BingoFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Bingo.init();
    }
}