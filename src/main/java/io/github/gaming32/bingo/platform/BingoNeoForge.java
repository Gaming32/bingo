package io.github.gaming32.bingo.platform;

import io.github.gaming32.bingo.Bingo;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Bingo.MOD_ID)
public class BingoNeoForge {
    public BingoNeoForge(IEventBus modEventBus) {
        BingoPlatform.setModEventBus(modEventBus);
        Bingo.init();
    }
}
