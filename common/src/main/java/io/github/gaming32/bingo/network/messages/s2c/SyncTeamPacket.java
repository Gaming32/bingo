package io.github.gaming32.bingo.network.messages.s2c;

import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.network.AbstractCustomPayload;
import io.github.gaming32.bingo.network.BingoNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class SyncTeamPacket extends AbstractCustomPayload {
    public static final ResourceLocation ID = id("sync_team");

    private final BingoBoard.Teams team;

    public SyncTeamPacket(BingoBoard.Teams team) {
        this.team = team;
    }

    public SyncTeamPacket(FriendlyByteBuf buf) {
        team = BingoBoard.Teams.fromBits(buf.readVarInt());
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(team.toBits());
    }

    @Override
    public void handle(BingoNetworking.Context context) {
        BingoClient.clientTeam = team;
    }
}
