package io.github.gaming32.bingo.network;

import io.github.gaming32.bingo.util.Identifiers;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;

public interface AbstractCustomPayload extends CustomPacketPayload {
    static <P extends CustomPacketPayload> Type<P> type(String id) {
        return new Type<>(Identifiers.bingo(id));
    }

    void handle(BingoNetworking.Context context);

    @ApiStatus.NonExtendable
    default void sendToServer() {
        BingoNetworking.sendToServer(this);
    }

    @ApiStatus.NonExtendable
    default void sendTo(ServerPlayer player) {
        BingoNetworking.sendTo(player, this);
    }

    @ApiStatus.NonExtendable
    default void sendTo(Iterable<ServerPlayer> players) {
        BingoNetworking.sendTo(players, this);
    }
}
