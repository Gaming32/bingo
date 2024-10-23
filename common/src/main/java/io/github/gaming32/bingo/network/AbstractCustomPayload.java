package io.github.gaming32.bingo.network;

import io.github.gaming32.bingo.util.ResourceLocations;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;

public interface AbstractCustomPayload extends CustomPacketPayload {
    static <P extends CustomPacketPayload> Type<P> type(String id) {
        return new Type<>(ResourceLocations.bingo(id));
    }

    void handle(BingoNetworking.Context context);

    @ApiStatus.NonExtendable
    default void sendToServer() {
        BingoNetworking.instance().sendToServer(this);
    }

    @ApiStatus.NonExtendable
    default void sendTo(ServerPlayer player) {
        BingoNetworking.instance().sendTo(player, this);
    }

    @ApiStatus.NonExtendable
    default void sendTo(Iterable<ServerPlayer> players) {
        BingoNetworking.instance().sendTo(players, this);
    }
}
