package io.github.gaming32.bingo.network.messages.s2c;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.network.AbstractCustomPayload;
import io.github.gaming32.bingo.network.BingoNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class ResyncStatesPacket extends AbstractCustomPayload {
    public static final ResourceLocation ID = id("resync_states");

    private final BingoBoard.Teams[] states;

    public ResyncStatesPacket(BingoBoard.Teams[] states) {
        this.states = states;
    }

    public ResyncStatesPacket(FriendlyByteBuf buf) {
        states = buf.readList(b -> BingoBoard.Teams.fromBits(b.readVarInt())).toArray(BingoBoard.Teams[]::new);
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(Arrays.asList(states), (b, v) -> b.writeVarInt(v.toBits()));
    }

    @Override
    public void handle(BingoNetworking.Context context) {
        if (BingoClient.clientGame == null) {
            Bingo.LOGGER.warn("BingoClient.clientGame == null while handling " + ID + "!");
            return;
        }
        System.arraycopy(states, 0, BingoClient.clientGame.states(), 0, BingoClient.clientGame.size() * BingoClient.clientGame.size());
    }
}
