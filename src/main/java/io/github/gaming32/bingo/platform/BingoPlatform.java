package io.github.gaming32.bingo.platform;

import io.github.gaming32.bingo.network.BingoNetworking;
import io.github.gaming32.bingo.platform.registrar.ClientTooltipRegistrar;
import io.github.gaming32.bingo.platform.registrar.DataReloadListenerRegistrar;
import io.github.gaming32.bingo.platform.registrar.DatapackRegistryRegistrar;
import io.github.gaming32.bingo.platform.registrar.KeyMappingBuilder;
import io.github.gaming32.bingo.platform.registrar.PictureInPictureRendererRegistrar;
import io.github.gaming32.bingo.platform.registry.DeferredRegister;
import io.github.gaming32.bingo.platform.registry.RegistryBuilder;
import net.minecraft.core.Registry;

import java.nio.file.Path;
import java.util.function.Consumer;

public abstract class BingoPlatform {
    public static BingoPlatform platform;

    public abstract BingoNetworking getNetworking();

    public abstract boolean isClient();

    public abstract Path getConfigDir();

    public abstract boolean isModLoaded(String id);

    public abstract void registerClientTooltips(Consumer<ClientTooltipRegistrar> handler);

    public abstract void registerPictureInPictureRenderers(Consumer<PictureInPictureRendererRegistrar> handler);

    public abstract void registerKeyMappings(Consumer<KeyMappingBuilder> handler);

    public abstract void registerDataReloadListeners(Consumer<DataReloadListenerRegistrar> handler);

    public abstract void registerDatapackRegistries(Consumer<DatapackRegistryRegistrar> handler);

    public abstract <T> DeferredRegister<T> createDeferredRegister(Registry<T> registry);

    public abstract <T> DeferredRegister<T> buildDeferredRegister(RegistryBuilder<T> builder);
}
