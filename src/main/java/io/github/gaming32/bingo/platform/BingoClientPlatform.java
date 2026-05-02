package io.github.gaming32.bingo.platform;

import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.client.config.BingoConfigScreen;
import io.github.gaming32.bingo.platform.event.ClientEvents;
import io.github.gaming32.bingo.util.Identifiers;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.UnknownNullability;

public final class BingoClientPlatform {
    @UnknownNullability
    private static ModContainer modContainer;
    @UnknownNullability
    private static IEventBus modEventBus;

    private BingoClientPlatform() {
    }

    public static void setModContainer(ModContainer modContainer) {
        BingoClientPlatform.modContainer = modContainer;
    }

    public static void setModEventBus(IEventBus modEventBus) {
        BingoClientPlatform.modEventBus = modEventBus;
    }

    public static void registerEvents() {
        ClientEvents.KEY_RELEASED_PRE.setRegistrar(handler -> NeoForge.EVENT_BUS.addListener((ScreenEvent.KeyReleased.Pre event) -> {
            if (handler.onKeyReleased(event.getScreen(), event.getKeyEvent())) {
                event.setCanceled(true);
            }
        }));
        ClientEvents.MOUSE_RELEASED_PRE.setRegistrar(handler -> NeoForge.EVENT_BUS.addListener((ScreenEvent.MouseButtonReleased.Pre event) -> {
            if (handler.onMouseReleased(event.getScreen(), event.getMouseButtonEvent())) {
                event.setCanceled(true);
            }
        }));
        ClientEvents.PLAYER_QUIT.setRegistrar(handler -> NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) ->
            handler.accept(event.getPlayer())
        ));
        ClientEvents.CLIENT_TICK_START.setRegistrar(handler -> NeoForge.EVENT_BUS.addListener((ClientTickEvent.Pre event) ->
            handler.accept(Minecraft.getInstance())
        ));
        ClientEvents.CLIENT_TICK_END.setRegistrar(handler -> NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) ->
            handler.accept(Minecraft.getInstance())
        ));

        modEventBus.addListener((RegisterGuiLayersEvent event) -> event.registerAboveAll(
            Identifiers.bingo("hud"),
            (graphics, deltaTracker) -> BingoClient.renderBoardOnHud(Minecraft.getInstance(), graphics)
        ));

        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (minecraft, screen) -> new BingoConfigScreen(screen));
    }
}
