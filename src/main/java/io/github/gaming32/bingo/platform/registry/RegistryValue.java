package io.github.gaming32.bingo.platform.registry;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class RegistryValue<T> {
    private final DeferredHolder<T, T> holder;

    RegistryValue(DeferredHolder<T, T> holder) {
        this.holder = holder;
    }

    public T get() {
        return holder.value();
    }

    public Identifier id() {
        return holder.getId();
    }

    public ResourceKey<T> key() {
        return holder.getKey();
    }

    public Holder<T> asHolder() {
        return holder;
    }
}
