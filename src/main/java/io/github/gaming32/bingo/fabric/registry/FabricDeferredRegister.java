package io.github.gaming32.bingo.fabric.registry;

import io.github.gaming32.bingo.platform.registry.DeferredRegister;
import io.github.gaming32.bingo.platform.registry.RegistryValue;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.function.Supplier;

public record FabricDeferredRegister<T>(Registry<T> registry) implements DeferredRegister<T> {
    @Override
    public ResourceKey<? extends Registry<T>> registryKey() {
        return registry.key();
    }

    @Override
    public <S extends T> RegistryValue<S> register(Identifier location, Supplier<S> value) {
        return new FabricRegistryValue<>(Registry.registerForHolder(registry, location, value.get()));
    }
}
