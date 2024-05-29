package io.github.gaming32.bingo.network.messages.s2c;

import io.github.gaming32.bingo.network.AbstractCustomPayload;
import io.github.gaming32.bingo.network.BingoNetworking;
import io.github.gaming32.bingo.network.ClientPayloadHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public enum RemoveBoardPayload implements AbstractCustomPayload {
    INSTANCE;

    public static final Type<RemoveBoardPayload> TYPE = AbstractCustomPayload.type("remove_board");
    public static final StreamCodec<ByteBuf, RemoveBoardPayload> CODEC = StreamCodec.unit(INSTANCE);

    @NotNull
    @Override
    public Type<RemoveBoardPayload> type() {
        return TYPE;
    }

    @Override
    public void handle(BingoNetworking.Context context) {
        ClientPayloadHandler.get().handleRemoveBoard();
    }
}
