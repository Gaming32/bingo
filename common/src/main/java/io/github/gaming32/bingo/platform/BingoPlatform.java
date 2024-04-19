package io.github.gaming32.bingo.platform;

import io.github.gaming32.bingo.network.BingoNetworking;

public abstract class BingoPlatform {
    public static BingoPlatform platform;

    public abstract BingoNetworking getNetworking();

    public abstract boolean isClient();
}
