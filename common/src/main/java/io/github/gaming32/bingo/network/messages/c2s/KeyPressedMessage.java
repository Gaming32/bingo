package io.github.gaming32.bingo.network.messages.c2s;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class KeyPressedMessage extends BaseC2SMessage {
    private final String key;

    public KeyPressedMessage(String key) {
        this.key = key;
    }

    public KeyPressedMessage(FriendlyByteBuf buf) {
        this.key = buf.readUtf();
    }

    @Override
    public MessageType getType() {
        return BingoC2S.KEY_PRESSED;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
         buf.writeUtf(key);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
            BingoTriggers.KEY_PRESSED.trigger(serverPlayer, key);
        }
    }
}
