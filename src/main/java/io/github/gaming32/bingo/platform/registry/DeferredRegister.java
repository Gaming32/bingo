package io.github.gaming32.bingo.platform.registry;

import io.github.gaming32.bingo.util.Identifiers;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

public final class DeferredRegister<T> {
    private final Registry<T> registry;

    private DeferredRegister(Registry<T> registry) {
        this.registry = registry;
    }

    public static <T> DeferredRegister<T> create(Registry<T> registry) {
        return new DeferredRegister<>(registry);
    }

    public Registry<T> registry() {
        return registry;
    }

    public ResourceKey<? extends Registry<T>> registryKey() {
        return registry.key();
    }

    public <S extends T> RegistryValue<S> register(Identifier location, Supplier<S> value) {
        return new RegistryValue<>(Registry.registerForHolder(registry, location, value.get()));
    }

    public <S extends T> RegistryValue<S> register(String id, Supplier<S> value) {
        return register(Identifiers.bingo(id), value);
    }
}
