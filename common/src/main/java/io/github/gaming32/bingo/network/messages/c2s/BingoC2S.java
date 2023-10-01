package io.github.gaming32.bingo.network.messages.c2s;

import dev.architectury.networking.simple.MessageType;

import static io.github.gaming32.bingo.network.BingoNetwork.NETWORK_MANAGER;

public class BingoC2S {
    public static final MessageType KEY_PRESSED = NETWORK_MANAGER.registerC2S("key_pressed", KeyPressedMessage::new);

    public static void load() {
    }
}
