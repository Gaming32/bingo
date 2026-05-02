package io.github.gaming32.bingo.platform;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.platform.event.FabricClientEvents;
import io.github.gaming32.bingo.platform.event.ClientEvents;
import io.github.gaming32.bingo.util.Identifiers;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.networking.v1.FriendlyByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

import java.util.concurrent.CompletableFuture;

public final class BingoClientPlatform {
    private BingoClientPlatform() {
    }

    public static void registerEvents() {
        ClientEvents.KEY_RELEASED_PRE.setRegistrar(handler -> ScreenEvents.BEFORE_INIT.register(
            (client, screen, scaledWidth, scaledHeight) ->
                ScreenKeyboardEvents.allowKeyRelease(screen).register((screen1, event) ->
                    !handler.onKeyReleased(screen1, event)
                )
        ));
        ClientEvents.MOUSE_RELEASED_PRE.setRegistrar(handler -> ScreenEvents.BEFORE_INIT.register(
            (client, screen, scaledWidth, scaledHeight) ->
                ScreenMouseEvents.allowMouseRelease(screen).register((screen1, event) ->
                    !handler.onMouseReleased(screen1, event)
                )
        ));
        ClientEvents.PLAYER_QUIT.setRegistrar(FabricClientEvents.PLAYER_QUIT::register);
        ClientEvents.CLIENT_TICK_START.setRegistrar(handler -> ClientTickEvents.START_CLIENT_TICK.register(handler::accept));
        ClientEvents.CLIENT_TICK_END.setRegistrar(handler -> ClientTickEvents.END_CLIENT_TICK.register(handler::accept));

        ClientLoginNetworking.registerGlobalReceiver(BingoPlatform.PROTOCOL_VERSION_PACKET, (client, handler, buf, listenerAdder) -> {
            final int serverVersion = buf.readVarInt();
            if (serverVersion != BingoNetworking.PROTOCOL_VERSION) {
                Bingo.LOGGER.warn("Bingo client and server versions don't match. A disconnect is probably imminent.");
            }
            final FriendlyByteBuf response = FriendlyByteBufs.create();
            response.writeVarInt(BingoNetworking.PROTOCOL_VERSION);
            return CompletableFuture.completedFuture(response);
        });

        HudElementRegistry.addLast(Identifiers.bingo("hud"), (graphics, deltaTracker) -> BingoClient.renderBoardOnHud(Minecraft.getInstance(), graphics));
    }
}
