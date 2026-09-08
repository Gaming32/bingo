package io.github.gaming32.bingo.gametest;

import io.github.gaming32.bingo.util.Identifiers;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public enum GametestSyncPayload implements CustomPacketPayload {
    INSTANCE;

    public static final Type<GametestSyncPayload> TYPE = new Type<>(Identifiers.bingo("gametest_sync"));
    public static final StreamCodec<ByteBuf, GametestSyncPayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<GametestSyncPayload> type() {
        return TYPE;
    }
}
