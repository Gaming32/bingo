package io.github.gaming32.bingo.platform.registry;

import io.github.gaming32.bingo.platform.BingoPlatform;
import io.github.gaming32.bingo.util.ResourceLocations;
import net.minecraft.resources.ResourceLocation;

public class RegistryBuilder {
    private final ResourceLocation id;
    private boolean synced;
    private ResourceLocation defaultId;

    public RegistryBuilder(ResourceLocation id) {
        this.id = id;
    }

    public RegistryBuilder(String id) {
        this(ResourceLocations.bingo(id));
    }

    public RegistryBuilder synced() {
        this.synced = true;
        return this;
    }

    public RegistryBuilder defaultId(ResourceLocation id) {
        this.defaultId = id;
        return this;
    }

    public RegistryBuilder defaultId(String id) {
        return defaultId(ResourceLocations.bingo(id));
    }

    public <T> DeferredRegister<T> build() {
        return BingoPlatform.platform.buildDeferredRegister(this);
    }

    public ResourceLocation getId() {
        return id;
    }

    public boolean isSynced() {
        return synced;
    }

    public ResourceLocation getDefaultId() {
        return defaultId;
    }
}
