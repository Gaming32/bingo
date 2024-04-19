package io.github.gaming32.bingo.platform;

import io.github.gaming32.bingo.network.BingoNetworking;

import java.nio.file.Path;

public abstract class BingoPlatform {
    public static BingoPlatform platform;

    public abstract BingoNetworking getNetworking();

    public abstract boolean isClient();

    public abstract Path getConfigDir();

    public abstract boolean isModLoaded(String id);
}
