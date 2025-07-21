package io.github.gaming32.bingo.neoforge.registry;

import io.github.gaming32.bingo.platform.registry.RegistryValue;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

public class NeoForgeRegistryValue<T> implements RegistryValue<T> {
    private final DeferredHolder<T, T> holder;

    public NeoForgeRegistryValue(DeferredHolder<T, T> holder) {
        this.holder = holder;
    }

    @Override
    public T get() {
        return holder.value();
    }

    @Override
    public ResourceLocation id() {
        return holder.getId();
    }

    @Override
    public ResourceKey<T> key() {
        return holder.getKey();
    }

    @Override
    public Holder<T> asHolder() {
        return holder;
    }
}
