package io.github.gaming32.bingo.platform.registry;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public interface RegistryValue<T> {
    T get();

    ResourceLocation id();

    ResourceKey<T> key();

    Holder<T> asHolder();
}
