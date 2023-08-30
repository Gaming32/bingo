package io.github.gaming32.bingo.network;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.game.BingoBoard;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Arrays;

public class ResyncStatesMessage extends BaseS2CMessage {
    private final BingoBoard.Teams[] states;

    public ResyncStatesMessage(BingoBoard.Teams[] states) {
        this.states = states;
    }

    public ResyncStatesMessage(FriendlyByteBuf buf) {
        states = buf.readList(b -> b.readEnum(BingoBoard.Teams.class)).toArray(BingoBoard.Teams[]::new);
    }

    @Override
    public MessageType getType() {
        return BingoNetwork.RESYNC_STATES;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(Arrays.asList(states), FriendlyByteBuf::writeEnum);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if (BingoClient.clientBoard == null) {
            Bingo.LOGGER.warn("BingoClient.clientBoard == null while handling " + getType().getId() + "!");
            return;
        }
        System.arraycopy(states, 0, BingoClient.clientBoard.states(), 0, BingoBoard.SIZE_SQ);
    }
}
