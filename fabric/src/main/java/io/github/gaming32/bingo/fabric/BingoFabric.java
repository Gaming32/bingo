package io.github.gaming32.bingo.fabric;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.multiloader.MultiLoaderInterface;
import io.github.gaming32.bingo.network.BingoNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class BingoFabric implements ModInitializer {
    public static final ResourceLocation PROTOCOL_VERSION_PACKET = new ResourceLocation("bingo:protocol_version");

    @Override
    public void onInitialize() {
        MultiLoaderInterface.instance = new FabricInterface();
        Bingo.init();

        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            final FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeVarInt(BingoNetworking.PROTOCOL_VERSION);
            sender.sendPacket(PROTOCOL_VERSION_PACKET, buf);
        });

        ServerLoginNetworking.registerGlobalReceiver(PROTOCOL_VERSION_PACKET, (server, handler, understood, buf, synchronizer, responseSender) -> {
            if (!understood) return;
            final int clientVersion = buf.readVarInt();
            if (clientVersion < BingoNetworking.PROTOCOL_VERSION) {
                handler.disconnect(Component.translatable("bingo.outdated_client", clientVersion, BingoNetworking.PROTOCOL_VERSION));
            } else if (BingoNetworking.PROTOCOL_VERSION < clientVersion) {
                handler.disconnect(Component.translatable("bingo.outdated_server", BingoNetworking.PROTOCOL_VERSION, clientVersion));
            }
        });
    }
}