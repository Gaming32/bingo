package io.github.gaming32.bingo.platform.registrar;

import io.github.gaming32.bingo.platform.BingoPlatform;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DataReloadListenerRegistrar {
    default void register(ResourceLocation id, ReloadListenerCreator listenerCreator) {
        register(id, listenerCreator, List.of());
    }

    void register(
        ResourceLocation id,
        ReloadListenerCreator listenerCreator,
        Collection<ResourceLocation> dependencies
    );

    @FunctionalInterface
    interface ReloadListenerCreator {
        /**
         * Creates a {@code PreparableReloadListener}. On Fabric, the registries will be present. On NeoForge, the
         * registries won't be present but will be injected later, either automatically by NeoForge (if it implements
         * {@code ContextAwareReloadListener} as some vanilla classes do, see {@code makeConditionalOps()}), or manually
         * by the bingo NeoForge platform. If you need to get a registry access or registry ops, use
         * {@link BingoPlatform#getRegistryAccessFromReloadListener} and
         * {@link BingoPlatform#makeRegistryOpsFromReloadListener} respectively.
         */
        PreparableReloadListener create(Optional<HolderLookup.Provider> registries);
    }
}
