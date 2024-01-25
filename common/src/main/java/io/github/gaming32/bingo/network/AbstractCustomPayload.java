package io.github.gaming32.bingo.network;

import io.github.gaming32.bingo.Bingo;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public abstract class AbstractCustomPayload implements CustomPacketPayload {
    protected static ResourceLocation id(String id) {
        return new ResourceLocation(Bingo.MOD_ID, id);
    }

    public final void sendToServer() {
        BingoNetworking.instance().sendToServer(this);
    }

    public final void sendTo(ServerPlayer player) {
        BingoNetworking.instance().sendTo(player, this);
    }

    public final void sendTo(Iterable<ServerPlayer> players) {
        BingoNetworking.instance().sendTo(players, this);
    }

    public abstract void handle(BingoNetworking.Context context);
}
