package io.github.gaming32.bingo.platform.registry;

import io.github.gaming32.bingo.util.Identifiers;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

public final class DeferredRegister<T> {
    private final Registry<T> registry;
    private final net.neoforged.neoforge.registries.DeferredRegister<T> deferredRegister;

    private DeferredRegister(Registry<T> registry, net.neoforged.neoforge.registries.DeferredRegister<T> deferredRegister) {
        this.registry = registry;
        this.deferredRegister = deferredRegister;
    }

    public static <T> DeferredRegister<T> create(Registry<T> registry) {
        return new DeferredRegister<>(registry, net.neoforged.neoforge.registries.DeferredRegister.create(registry, "bingo"));
    }

    public Registry<T> registry() {
        return registry;
    }

    public ResourceKey<? extends Registry<T>> registryKey() {
        return registry.key();
    }

    @SuppressWarnings({"unchecked", "RedundantCast"})
    public <S extends T> RegistryValue<S> register(Identifier location, Supplier<S> value) {
        if (!location.getNamespace().equals("bingo")) {
            throw new IllegalArgumentException("Can only use DeferredRegister with bingo namespace");
        }
        return (RegistryValue<S>)new RegistryValue<>(deferredRegister.register(location.getPath(), value));
    }

    public <S extends T> RegistryValue<S> register(String id, Supplier<S> value) {
        return register(Identifiers.bingo(id), value);
    }
}
