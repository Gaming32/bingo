package io.github.gaming32.bingo.platform.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public interface DeferredRegister<T> {
    Registry<T> registry();

    ResourceKey<? extends Registry<T>> registryKey();

    <S extends T> RegistryValue<S> register(ResourceLocation location, Supplier<S> value);

    default <S extends T> RegistryValue<S> register(String id, Supplier<S> value) {
        return register(id(id), value);
    }

    static ResourceLocation id(String id) {
        return new ResourceLocation(id.indexOf(':') < 0 ? "bingo:" + id : id);
    }
}
