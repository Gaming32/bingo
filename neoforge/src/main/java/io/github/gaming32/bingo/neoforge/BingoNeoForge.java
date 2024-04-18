package io.github.gaming32.bingo.neoforge;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.multiloader.MultiLoaderInterface;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Bingo.MOD_ID)
public class BingoNeoForge {
    public BingoNeoForge(IEventBus modEventBus) {
        MultiLoaderInterface.instance = new NeoForgeInterface(modEventBus);
        Bingo.init();
    }
}
