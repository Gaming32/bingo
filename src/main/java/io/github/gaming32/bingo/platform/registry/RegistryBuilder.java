package io.github.gaming32.bingo.platform.registry;

import io.github.gaming32.bingo.util.Identifiers;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
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
        final var fabricBuilder = defaultId != null
            ? FabricRegistryBuilder.createDefaulted(key, defaultId)
            : FabricRegistryBuilder.create(key);
        if (synced) {
            fabricBuilder.attribute(RegistryAttribute.SYNCED);
        }

        // FIXME: this technically shouldn't be optional, but prevents clients from being kicked on different mod loaders
        fabricBuilder.attribute(RegistryAttribute.OPTIONAL);

        return DeferredRegister.create(fabricBuilder.buildAndRegister());
    }
}
