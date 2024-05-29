package io.github.gaming32.bingo.client.platform;

import io.github.gaming32.bingo.client.platform.registrar.ClientTooltipRegistrar;
import io.github.gaming32.bingo.client.platform.registrar.KeyMappingBuilder;

import java.util.function.Consumer;

public abstract class BingoClientPlatform {
    public static BingoClientPlatform platform;

    public abstract void registerClientTooltips(Consumer<ClientTooltipRegistrar> handler);

    public abstract void registerKeyMappings(Consumer<KeyMappingBuilder> handler);
}
