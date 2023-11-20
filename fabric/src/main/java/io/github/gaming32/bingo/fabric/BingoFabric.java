package io.github.gaming32.bingo.fabric;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.network.BingoNetwork;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public class BingoFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Bingo.init();

        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            final FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeVarInt(BingoNetwork.PROTOCOL_VERSION);
            sender.sendPacket(BingoNetwork.PROTOCOL_VERSION_PACKET, buf);
        });

        ServerLoginNetworking.registerGlobalReceiver(BingoNetwork.PROTOCOL_VERSION_PACKET, (server, handler, understood, buf, synchronizer, responseSender) -> {
            if (!understood) return;
            final int clientVersion = buf.readVarInt();
            if (clientVersion < BingoNetwork.PROTOCOL_VERSION) {
                handler.disconnect(Component.translatable("bingo.outdated_client", clientVersion, BingoNetwork.PROTOCOL_VERSION));
            } else if (BingoNetwork.PROTOCOL_VERSION < clientVersion) {
                handler.disconnect(Component.translatable("bingo.outdated_server", BingoNetwork.PROTOCOL_VERSION, clientVersion));
            }
        });
    }
}