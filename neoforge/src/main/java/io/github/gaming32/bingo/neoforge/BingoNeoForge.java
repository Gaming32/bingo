package io.github.gaming32.bingo.neoforge;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.platform.BingoPlatform;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(Bingo.MOD_ID)
public class BingoNeoForge {
    public static ModContainer container;

    public BingoNeoForge(ModContainer container, IEventBus modEventBus) {
        BingoNeoForge.container = container;
        BingoPlatform.platform = new NeoForgePlatform(modEventBus);
        Bingo.init();
    }
}
