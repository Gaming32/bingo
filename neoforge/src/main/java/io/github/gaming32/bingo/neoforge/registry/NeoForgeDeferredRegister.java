package io.github.gaming32.bingo.neoforge.registry;

import io.github.gaming32.bingo.platform.registry.DeferredRegister;
import io.github.gaming32.bingo.platform.registry.RegistryValue;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public final class NeoForgeDeferredRegister<T> implements DeferredRegister<T> {
    private final Registry<T> registry;
    private final net.neoforged.neoforge.registries.DeferredRegister<T> deferredRegister;

    public NeoForgeDeferredRegister(Registry<T> registry) {
        this.registry = registry;
        deferredRegister = net.neoforged.neoforge.registries.DeferredRegister.create(registry, "bingo");
    }

    @Override
    public Registry<T> registry() {
        return registry;
    }

    @Override
    public ResourceKey<? extends Registry<T>> registryKey() {
        return registry.key();
    }

    @Override
    @SuppressWarnings({"unchecked", "RedundantCast"})
    public <S extends T> RegistryValue<S> register(ResourceLocation location, Supplier<S> value) {
        if (!location.getNamespace().equals("bingo")) {
            throw new IllegalArgumentException("Can only use NeoForgeDeferredRegister with bingo namespace");
        }
        return (RegistryValue<S>)new NeoForgeRegistryValue<>(deferredRegister.register(location.getPath(), value));
    }

    public net.neoforged.neoforge.registries.DeferredRegister<T> getDeferredRegister() {
        return deferredRegister;
    }
}
