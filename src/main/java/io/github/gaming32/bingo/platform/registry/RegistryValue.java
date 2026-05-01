package io.github.gaming32.bingo.platform.registry;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

public final class RegistryValue<T> {
    private final Holder.Reference<T> holder;

    RegistryValue(Holder.Reference<T> holder) {
        this.holder = holder;
    }

    public T get() {
        return holder.value();
    }

    public Identifier id() {
        return holder.key().identifier();
    }

    public ResourceKey<T> key() {
        return holder.key();
    }

    public Holder<T> asHolder() {
        return holder;
    }
}
