package io.github.gaming32.bingo.fabric.client;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.network.BingoNetwork;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

import java.util.concurrent.CompletableFuture;

public class BingoFabricClient {
    public static void init() {
        ClientLoginNetworking.registerGlobalReceiver(BingoNetwork.PROTOCOL_VERSION_PACKET, (client, handler, buf, listenerAdder) -> {
            final int serverVersion = buf.readVarInt();
            if (serverVersion != BingoNetwork.PROTOCOL_VERSION) {
                Bingo.LOGGER.warn("Bingo client and server versions don't match. A disconnect is probably imminent.");
            }
            final FriendlyByteBuf response = PacketByteBufs.create();
            response.writeVarInt(BingoNetwork.PROTOCOL_VERSION);
            return CompletableFuture.completedFuture(response);
        });
    }
}
