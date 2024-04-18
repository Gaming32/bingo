package io.github.gaming32.bingo.neoforge;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.multiloader.BingoPlatform;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Bingo.MOD_ID)
public class BingoNeoForge {
    public BingoNeoForge(IEventBus modEventBus) {
        BingoPlatform.platform = new NeoForgePlatform(modEventBus);
        Bingo.init();
    }
}
