package io.github.gaming32.bingo.network.messages.s2c;

import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.network.AbstractCustomPayload;
import io.github.gaming32.bingo.network.BingoNetworking;
import io.github.gaming32.bingo.network.ClientPayloadHandler;
import io.github.gaming32.bingo.util.BingoStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record ResyncStatesPayload(BingoBoard.Teams[] states) implements AbstractCustomPayload {
    public static final Type<ResyncStatesPayload> TYPE = AbstractCustomPayload.type("resync_states");
    public static final StreamCodec<ByteBuf, ResyncStatesPayload> CODEC = BingoBoard.Teams.STREAM_CODEC
        .apply(BingoStreamCodecs.array(BingoBoard.Teams[]::new))
        .map(ResyncStatesPayload::new, p -> p.states);

    @NotNull
    @Override
    public Type<ResyncStatesPayload> type() {
        return TYPE;
    }

    @Override
    public void handle(BingoNetworking.Context context) {
        ClientPayloadHandler.get().handleResyncStates(this);
    }
}
