package io.github.gaming32.bingo.network.messages.s2c;

import io.github.gaming32.bingo.network.AbstractCustomPayload;
import io.github.gaming32.bingo.network.BingoNetworking;
import io.github.gaming32.bingo.network.ClientPayloadHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

public record UpdateEndTimePayload(long endTime) implements AbstractCustomPayload {
    public static final Type<UpdateEndTimePayload> TYPE = AbstractCustomPayload.type("update_end_time");
    public static final StreamCodec<ByteBuf, UpdateEndTimePayload> CODEC = ByteBufCodecs.VAR_LONG.map(UpdateEndTimePayload::new, UpdateEndTimePayload::endTime);

    @NotNull
    @Override
    public Type<UpdateEndTimePayload> type() {
        return TYPE;
    }

    @Override
    public void handle(BingoNetworking.Context context) {
        ClientPayloadHandler.get().handleUpdateEndTime(this);
    }
}
