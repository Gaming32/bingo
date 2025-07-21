package io.github.gaming32.bingo.network.messages.s2c;

import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.network.AbstractCustomPayload;
import io.github.gaming32.bingo.network.BingoNetworking;
import io.github.gaming32.bingo.network.ClientPayloadHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record UpdateStatePayload(int index, BingoBoard.Teams newState) implements AbstractCustomPayload {
    public static final Type<UpdateStatePayload> TYPE = AbstractCustomPayload.type("update_state");
    public static final StreamCodec<ByteBuf, UpdateStatePayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, p -> p.index,
        BingoBoard.Teams.STREAM_CODEC, p -> p.newState,
        UpdateStatePayload::new
    );

    @NotNull
    @Override
    public Type<UpdateStatePayload> type() {
        return TYPE;
    }

    @Override
    public void handle(BingoNetworking.Context context) {
        ClientPayloadHandler.get().handleUpdateState(this);
    }
}
