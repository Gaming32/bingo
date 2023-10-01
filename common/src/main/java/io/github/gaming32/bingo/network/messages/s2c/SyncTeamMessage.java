package io.github.gaming32.bingo.network.messages.s2c;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.game.BingoBoard;
import net.minecraft.network.FriendlyByteBuf;

public class SyncTeamMessage extends BaseS2CMessage {
    private final BingoBoard.Teams team;

    public SyncTeamMessage(BingoBoard.Teams team) {
        this.team = team;
    }

    public SyncTeamMessage(FriendlyByteBuf buf) {
        team = BingoBoard.Teams.fromBits(buf.readVarInt());
    }

    @Override
    public MessageType getType() {
        return BingoS2C.SYNC_TEAM;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(team.toBits());
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        BingoClient.clientTeam = team;
    }
}
