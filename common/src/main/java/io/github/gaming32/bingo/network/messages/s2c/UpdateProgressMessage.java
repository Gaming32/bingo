package io.github.gaming32.bingo.network.messages.s2c;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.game.GoalProgress;
import net.minecraft.network.FriendlyByteBuf;

public class UpdateProgressMessage extends BaseS2CMessage {
    private final int index;
    private final int progress;
    private final int maxProgress;

    public UpdateProgressMessage(int index, int progress, int maxProgress) {
        this.index = index;
        this.progress = progress;
        this.maxProgress = maxProgress;
    }

    public UpdateProgressMessage(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }

    @Override
    public MessageType getType() {
        return BingoS2C.UPDATE_PROGRESS;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(index);
        buf.writeVarInt(progress);
        buf.writeVarInt(maxProgress);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if (BingoClient.clientBoard == null) {
            Bingo.LOGGER.warn("BingoClient.clientBoard == null while handling " + getType().getId() + "!");
            return;
        }
        BingoClient.clientBoard.progress()[index] = new GoalProgress(progress, maxProgress);
    }
}
