package io.github.gaming32.bingo.neoforge.client;

import io.github.gaming32.bingo.client.config.BingoConfigScreen;
import io.github.gaming32.bingo.neoforge.BingoNeoForge;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BingoNeoForgeClient {
    @SubscribeEvent
    public static void accept(FMLClientSetupEvent event) {
        BingoNeoForge.container.registerExtensionPoint(IConfigScreenFactory.class, (minecraft, screen) -> new BingoConfigScreen(screen));
    }
}
