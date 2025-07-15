package io.github.gaming32.bingo.neoforge.client;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.client.config.BingoConfigScreen;
import io.github.gaming32.bingo.util.ResourceLocations;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Bingo.MOD_ID, dist = Dist.CLIENT)
public class BingoNeoForgeClient {
    public BingoNeoForgeClient(ModContainer mod, IEventBus bus) {
        BingoClient.init();

        bus.addListener((RegisterGuiLayersEvent event) -> event.registerAboveAll(
            ResourceLocations.bingo("hud"),
            (graphics, deltaTracker) -> BingoClient.renderBoardOnHud(Minecraft.getInstance(), graphics)
        ));

        mod.registerExtensionPoint(IConfigScreenFactory.class, (minecraft, screen) -> new BingoConfigScreen(screen));
    }
}
