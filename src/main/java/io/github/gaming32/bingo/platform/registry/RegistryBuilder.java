package io.github.gaming32.bingo.platform.registry;

import io.github.gaming32.bingo.platform.BingoPlatform;
import io.github.gaming32.bingo.util.Identifiers;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

public class RegistryBuilder<T> {
    private final ResourceKey<Registry<T>> key;
    private boolean synced;
    private Identifier defaultId;

    public RegistryBuilder(ResourceKey<Registry<T>> key) {
        this.key = key;
    }

    public RegistryBuilder<T> synced() {
        this.synced = true;
        return this;
    }

    public RegistryBuilder<T> defaultId(Identifier id) {
        this.defaultId = id;
        return this;
    }

    public RegistryBuilder<T> defaultId(String id) {
        return defaultId(Identifiers.bingo(id));
    }

    public DeferredRegister<T> build() {
        return BingoPlatform.platform.buildDeferredRegister(this);
    }

    public ResourceKey<Registry<T>> getKey() {
        return key;
    }

    public boolean isSynced() {
        return synced;
    }

    public Identifier getDefaultId() {
        return defaultId;
    }
}
