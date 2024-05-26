package io.github.gaming32.bingo.platform.registrar;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.Collection;
import java.util.List;

public interface DataReloadListenerRegistrar {
    ReloadableServerResources serverResources();

    HolderLookup.Provider registryAccess();

    default void register(ResourceLocation id, PreparableReloadListener listener) {
        register(id, listener, List.of());
    }

    void register(ResourceLocation id, PreparableReloadListener listener, Collection<ResourceLocation> dependencies);
}
