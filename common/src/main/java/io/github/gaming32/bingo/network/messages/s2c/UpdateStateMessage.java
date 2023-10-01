package io.github.gaming32.bingo.network.messages.s2c;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.game.BingoBoard;
import net.minecraft.network.FriendlyByteBuf;

public class UpdateStateMessage extends BaseS2CMessage {
    private final int index;
    private final BingoBoard.Teams newState;

    public UpdateStateMessage(int index, BingoBoard.Teams newState) {
        this.index = index;
        this.newState = newState;
    }

    public UpdateStateMessage(FriendlyByteBuf buf) {
        index = buf.readVarInt();
        newState = BingoBoard.Teams.fromBits(buf.readVarInt());
    }

    @Override
    public MessageType getType() {
        return BingoS2C.UPDATE_STATE;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(index);
        buf.writeVarInt(newState.toBits());
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if (index < 0 || index >= BingoBoard.SIZE_SQ) {
            Bingo.LOGGER.warn("Invalid " + getType().getId() + " packet: invalid board index " + index);
            return;
        }
        if (BingoClient.clientBoard == null) {
            Bingo.LOGGER.warn("BingoClient.clientBoard == null while handling " + getType().getId() + "!");
            return;
        }
        BingoClient.clientBoard.states()[index] = newState;
    }
}
