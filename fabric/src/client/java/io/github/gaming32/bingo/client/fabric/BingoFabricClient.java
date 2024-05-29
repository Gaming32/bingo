package io.github.gaming32.bingo.client.fabric;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.client.platform.BingoClientPlatform;
import io.github.gaming32.bingo.fabric.BingoFabric;
import io.github.gaming32.bingo.fabric.BingoNetworkingImpl;
import io.github.gaming32.bingo.network.BingoNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.concurrent.CompletableFuture;

public class BingoFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BingoClientPlatform.platform = new FabricClientPlatform();
        BingoClient.init();

        BingoNetworkingImpl.RegistrarImpl.initClientHandlers(BingoFabricClient::registerQueuedClientPayloadHandler);

        ClientLoginNetworking.registerGlobalReceiver(BingoFabric.PROTOCOL_VERSION_PACKET, (client, handler, buf, listenerAdder) -> {
            final int serverVersion = buf.readVarInt();
            if (serverVersion != BingoNetworking.PROTOCOL_VERSION) {
                Bingo.LOGGER.warn("Bingo client and server versions don't match. A disconnect is probably imminent.");
            }
            final FriendlyByteBuf response = PacketByteBufs.create();
            response.writeVarInt(BingoNetworking.PROTOCOL_VERSION);
            return CompletableFuture.completedFuture(response);
        });
    }

    private static <P extends CustomPacketPayload> void registerQueuedClientPayloadHandler(
        BingoNetworkingImpl.RegistrarImpl.PayloadTypeAndHandler<P> handler
    ) {
        ClientPlayNetworking.registerGlobalReceiver(handler.type(), (payload, context) -> handler.handler().accept(
            payload, new BingoNetworking.Context(context.player(), context.responseSender()::sendPacket)
        ));
    }
}
