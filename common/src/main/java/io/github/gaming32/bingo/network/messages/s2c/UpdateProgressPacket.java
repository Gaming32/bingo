package io.github.gaming32.bingo.network.messages.s2c;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.game.GoalProgress;
import io.github.gaming32.bingo.network.AbstractCustomPayload;
import io.github.gaming32.bingo.network.BingoNetworking;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record UpdateProgressPacket(int index, int progress, int maxProgress) implements AbstractCustomPayload {
    public static final Type<UpdateProgressPacket> TYPE = AbstractCustomPayload.type("update_progress");
    public static final StreamCodec<ByteBuf, UpdateProgressPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, p -> p.index,
        ByteBufCodecs.VAR_INT, p -> p.progress,
        ByteBufCodecs.VAR_INT, p -> p.maxProgress,
        UpdateProgressPacket::new
    );

    @NotNull
    @Override
    public Type<UpdateProgressPacket> type() {
        return TYPE;
    }

    @Override
    public void handle(BingoNetworking.Context context) {
        if (BingoClient.clientGame == null) {
            Bingo.LOGGER.warn("BingoClient.clientGame == null while handling {}!", TYPE);
            return;
        }
        BingoClient.clientGame.progress()[index] = new GoalProgress(progress, maxProgress);
    }
}
