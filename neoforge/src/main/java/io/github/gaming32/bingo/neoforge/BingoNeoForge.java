package io.github.gaming32.bingo.neoforge;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.client.config.BingoConfigScreen;
import io.github.gaming32.bingo.platform.BingoPlatform;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.ConfigScreenHandler;

@Mod(Bingo.MOD_ID)
public class BingoNeoForge {
    public BingoNeoForge(ModContainer container, IEventBus modEventBus) {
        BingoPlatform.platform = new NeoForgePlatform(modEventBus);
        Bingo.init();

        if (BingoPlatform.platform.isClient()) {
            container.registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(
                    (minecraft, screen) -> new BingoConfigScreen(screen)
                )
            );
        }
    }
}
