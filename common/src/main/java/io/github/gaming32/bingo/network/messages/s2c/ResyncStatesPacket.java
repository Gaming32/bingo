package io.github.gaming32.bingo.network.messages.s2c;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.network.AbstractCustomPayload;
import io.github.gaming32.bingo.network.BingoNetworking;
import io.github.gaming32.bingo.util.BingoStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public class ResyncStatesPacket extends AbstractCustomPayload {
    public static final Type<ResyncStatesPacket> TYPE = type("resync_states");
    public static final StreamCodec<ByteBuf, ResyncStatesPacket> CODEC = BingoBoard.Teams.STREAM_CODEC
        .apply(BingoStreamCodecs.array(BingoBoard.Teams[]::new))
        .map(ResyncStatesPacket::new, p -> p.states);

    private final BingoBoard.Teams[] states;

    public ResyncStatesPacket(BingoBoard.Teams[] states) {
        this.states = states;
    }

    @NotNull
    @Override
    public Type<ResyncStatesPacket> type() {
        return TYPE;
    }

    @Override
    public void handle(BingoNetworking.Context context) {
        if (BingoClient.clientGame == null) {
            Bingo.LOGGER.warn("BingoClient.clientGame == null while handling {}!", TYPE);
            return;
        }
        System.arraycopy(states, 0, BingoClient.clientGame.states(), 0, BingoClient.clientGame.size() * BingoClient.clientGame.size());
    }
}
