package io.github.gaming32.bingo.network.messages.s2c;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import io.github.gaming32.bingo.client.BingoClient;
import net.minecraft.network.FriendlyByteBuf;

public class RemoveBoardMessage extends BaseS2CMessage {
    public static final RemoveBoardMessage INSTANCE = new RemoveBoardMessage();

    private RemoveBoardMessage() {
    }

    @Override
    public MessageType getType() {
        return BingoS2C.REMOVE_BOARD;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        BingoClient.clientBoard = null;
    }
}
