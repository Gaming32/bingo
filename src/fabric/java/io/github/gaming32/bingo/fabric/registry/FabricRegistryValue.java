package io.github.gaming32.bingo.fabric.registry;

import io.github.gaming32.bingo.platform.registry.RegistryValue;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class FabricRegistryValue<T> implements RegistryValue<T> {
    private final Holder.Reference<T> holder;

    public FabricRegistryValue(Holder.Reference<T> holder) {
        this.holder = holder;
    }

    @Override
    public T get() {
        return holder.value();
    }

    @Override
    public ResourceLocation id() {
        return holder.key().location();
    }

    @Override
    public ResourceKey<T> key() {
        return holder.key();
    }

    @Override
    public Holder<T> asHolder() {
        return holder;
    }
}
