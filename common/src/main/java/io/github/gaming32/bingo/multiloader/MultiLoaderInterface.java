package io.github.gaming32.bingo.multiloader;

import io.github.gaming32.bingo.network.BingoNetworking;

public abstract class MultiLoaderInterface {
    public static MultiLoaderInterface instance;

    public abstract BingoNetworking getNetworking();
}
