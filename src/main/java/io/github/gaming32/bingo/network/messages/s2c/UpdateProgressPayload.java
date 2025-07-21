package io.github.gaming32.bingo.network.messages.s2c;

import io.github.gaming32.bingo.network.AbstractCustomPayload;
import io.github.gaming32.bingo.network.BingoNetworking;
import io.github.gaming32.bingo.network.ClientPayloadHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record UpdateProgressPayload(int index, int progress, int maxProgress) implements AbstractCustomPayload {
    public static final Type<UpdateProgressPayload> TYPE = AbstractCustomPayload.type("update_progress");
    public static final StreamCodec<ByteBuf, UpdateProgressPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, p -> p.index,
        ByteBufCodecs.VAR_INT, p -> p.progress,
        ByteBufCodecs.VAR_INT, p -> p.maxProgress,
        UpdateProgressPayload::new
    );

    @NotNull
    @Override
    public Type<UpdateProgressPayload> type() {
        return TYPE;
    }

    @Override
    public void handle(BingoNetworking.Context context) {
        ClientPayloadHandler.get().handleUpdateProgress(this);
    }
}
