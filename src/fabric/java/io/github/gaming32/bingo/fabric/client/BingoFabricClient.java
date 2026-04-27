package io.github.gaming32.bingo.fabric.client;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.fabric.BingoFabric;
import io.github.gaming32.bingo.network.BingoNetworking;
import io.github.gaming32.bingo.util.Identifiers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.networking.v1.FriendlyByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

import java.util.concurrent.CompletableFuture;

public class BingoFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BingoClient.init();

        ClientLoginNetworking.registerGlobalReceiver(BingoFabric.PROTOCOL_VERSION_PACKET, (client, handler, buf, listenerAdder) -> {
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
