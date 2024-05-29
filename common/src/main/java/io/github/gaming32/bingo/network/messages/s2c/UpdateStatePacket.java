package io.github.gaming32.bingo.network.messages.s2c;

import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.network.AbstractCustomPayload;
import io.github.gaming32.bingo.network.BingoNetworking;
import io.github.gaming32.bingo.network.ClientPayloadHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record UpdateStatePacket(int index, BingoBoard.Teams newState) implements AbstractCustomPayload {
    public static final Type<UpdateStatePacket> TYPE = AbstractCustomPayload.type("update_state");
    public static final StreamCodec<ByteBuf, UpdateStatePacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, p -> p.index,
        BingoBoard.Teams.STREAM_CODEC, p -> p.newState,
        UpdateStatePacket::new
    );

    @NotNull
    @Override
    public Type<UpdateStatePacket> type() {
        return TYPE;
    }

    @Override
    public void handle(BingoNetworking.Context context) {
        ClientPayloadHandler.get().handleUpdateState(this);
    }
}
