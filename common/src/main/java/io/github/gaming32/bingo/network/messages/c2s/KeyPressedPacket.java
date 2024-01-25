package io.github.gaming32.bingo.network.messages.c2s;

import io.github.gaming32.bingo.network.AbstractCustomPayload;
import io.github.gaming32.bingo.network.BingoNetworking;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class KeyPressedPacket extends AbstractCustomPayload {
    public static final ResourceLocation ID = id("key_pressed");

    private final String key;

    public KeyPressedPacket(String key) {
        this.key = key;
    }

    public KeyPressedPacket(FriendlyByteBuf buf) {
        this.key = buf.readUtf();
    }

    @NotNull
    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
         buf.writeUtf(key);
    }

    @Override
    public void handle(BingoNetworking.Context context) {
        if (context.player() instanceof ServerPlayer serverPlayer) {
            BingoTriggers.KEY_PRESSED.get().trigger(serverPlayer, key);
        }
    }
}
