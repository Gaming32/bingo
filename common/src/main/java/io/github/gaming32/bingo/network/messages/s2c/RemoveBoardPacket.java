package io.github.gaming32.bingo.network.messages.s2c;

import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.network.AbstractCustomPayload;
import io.github.gaming32.bingo.network.BingoNetworking;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public enum RemoveBoardPacket implements AbstractCustomPayload {
    INSTANCE;

    public static final Type<RemoveBoardPacket> TYPE = AbstractCustomPayload.type("remove_board");
    public static final StreamCodec<ByteBuf, RemoveBoardPacket> CODEC = StreamCodec.unit(INSTANCE);

    @NotNull
    @Override
    public Type<RemoveBoardPacket> type() {
        return TYPE;
    }

    @Override
    public void handle(BingoNetworking.Context context) {
        BingoClient.clientGame = null;
    }
}
