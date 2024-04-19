package io.github.gaming32.bingo.fabric.registry;

import io.github.gaming32.bingo.platform.registry.DeferredRegister;
import io.github.gaming32.bingo.platform.registry.RegistryValue;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public record FabricDeferredRegister<T>(Registry<T> registry) implements DeferredRegister<T> {
    @Override
    public ResourceKey<? extends Registry<T>> registryKey() {
        return registry.key();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends T> RegistryValue<S> register(ResourceLocation location, Supplier<S> value) {
        return new FabricRegistryValue<>((Holder.Reference<S>)Registry.registerForHolder(registry, location, value.get()));
    }
}
