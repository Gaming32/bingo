package io.github.gaming32.bingo.platform;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.client.BingoClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value = Bingo.MOD_ID, dist = Dist.CLIENT)
public class BingoNeoForgeClient {
    public BingoNeoForgeClient(ModContainer modContainer, IEventBus modEventBus) {
        BingoClientPlatform.setModContainer(modContainer);
        BingoClientPlatform.setModEventBus(modEventBus);
        BingoClient.init();
    }
}
