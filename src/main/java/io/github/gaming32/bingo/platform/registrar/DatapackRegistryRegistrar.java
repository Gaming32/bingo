package io.github.gaming32.bingo.platform.registrar;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public interface DatapackRegistryRegistrar {
    <T> void unsynced(ResourceKey<Registry<T>> registryKey, Codec<T> codec);

    default <T> void synced(ResourceKey<Registry<T>> registryKey, Codec<T> codec) {
        synced(registryKey, codec, codec);
    }

    <T> void synced(ResourceKey<Registry<T>> registryKey, Codec<T> codec, Codec<T> networkCodec);
}
