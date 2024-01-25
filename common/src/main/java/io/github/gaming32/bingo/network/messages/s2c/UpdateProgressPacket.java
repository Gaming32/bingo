package io.github.gaming32.bingo.network.messages.s2c;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.game.GoalProgress;
import io.github.gaming32.bingo.network.AbstractCustomPayload;
import io.github.gaming32.bingo.network.BingoNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class UpdateProgressPacket extends AbstractCustomPayload {
    public static final ResourceLocation ID = id("update_progress");

    private final int index;
    private final int progress;
    private final int maxProgress;

    public UpdateProgressPacket(int index, int progress, int maxProgress) {
        this.index = index;
        this.progress = progress;
        this.maxProgress = maxProgress;
    }

    public UpdateProgressPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(index);
        buf.writeVarInt(progress);
        buf.writeVarInt(maxProgress);
    }

    @Override
    public void handle(BingoNetworking.Context context) {
        if (BingoClient.clientGame == null) {
            Bingo.LOGGER.warn("BingoClient.clientGame == null while handling " + ID + "!");
            return;
        }
        BingoClient.clientGame.progress()[index] = new GoalProgress(progress, maxProgress);
    }
}
