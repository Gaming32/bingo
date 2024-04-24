package io.github.gaming32.bingo.network.messages.c2s;

import io.github.gaming32.bingo.network.AbstractCustomPayload;
import io.github.gaming32.bingo.network.BingoNetworking;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class KeyPressedPacket extends AbstractCustomPayload {
    public static final Type<KeyPressedPacket> TYPE = type("key_pressed");
    public static final StreamCodec<ByteBuf, KeyPressedPacket> CODEC = ByteBufCodecs.STRING_UTF8
        .map(KeyPressedPacket::new, p -> p.key);

    private final String key;

    public KeyPressedPacket(String key) {
        this.key = key;
    }

    @NotNull
    @Override
    public Type<KeyPressedPacket> type() {
        return TYPE;
    }

    @Override
    public void handle(BingoNetworking.Context context) {
        if (context.player() instanceof ServerPlayer serverPlayer) {
            BingoTriggers.KEY_PRESSED.get().trigger(serverPlayer, key);
        }
    }
}
