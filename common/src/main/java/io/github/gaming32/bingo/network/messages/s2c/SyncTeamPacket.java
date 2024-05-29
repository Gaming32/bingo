package io.github.gaming32.bingo.network.messages.s2c;

import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.network.AbstractCustomPayload;
import io.github.gaming32.bingo.network.BingoNetworking;
import io.github.gaming32.bingo.network.ClientPayloadHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record SyncTeamPacket(BingoBoard.Teams team) implements AbstractCustomPayload {
    public static final Type<SyncTeamPacket> TYPE = AbstractCustomPayload.type("sync_team");
    public static final StreamCodec<ByteBuf, SyncTeamPacket> CODEC = BingoBoard.Teams.STREAM_CODEC
        .map(SyncTeamPacket::new, p -> p.team);

    @NotNull
    @Override
    public Type<SyncTeamPacket> type() {
        return TYPE;
    }

    @Override
    public void handle(BingoNetworking.Context context) {
        ClientPayloadHandler.get().handleSyncTeam(this);
    }
}
