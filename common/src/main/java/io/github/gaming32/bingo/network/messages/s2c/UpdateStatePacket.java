package io.github.gaming32.bingo.network.messages.s2c;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.network.AbstractCustomPayload;
import io.github.gaming32.bingo.network.BingoNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class UpdateStatePacket extends AbstractCustomPayload {
    public static final ResourceLocation ID = id("update_state");

    private final int index;
    private final BingoBoard.Teams newState;

    public UpdateStatePacket(int index, BingoBoard.Teams newState) {
        this.index = index;
        this.newState = newState;
    }

    public UpdateStatePacket(FriendlyByteBuf buf) {
        index = buf.readVarInt();
        newState = BingoBoard.Teams.fromBits(buf.readVarInt());
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(index);
        buf.writeVarInt(newState.toBits());
    }

    @Override
    public void handle(BingoNetworking.Context context) {
        if (BingoClient.clientGame == null) {
            Bingo.LOGGER.warn("BingoClient.clientGame == null while handling " + ID + "!");
            return;
        }
        if (index < 0 || index >= BingoClient.clientGame.size() * BingoClient.clientGame.size()) {
            Bingo.LOGGER.warn("Invalid " + ID + " packet: invalid board index " + index);
            return;
        }
        BingoClient.clientGame.states()[index] = newState;
    }
}
