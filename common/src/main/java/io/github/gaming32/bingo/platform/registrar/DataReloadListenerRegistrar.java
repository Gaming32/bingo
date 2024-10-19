package io.github.gaming32.bingo.platform.registrar;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public interface DataReloadListenerRegistrar {
    default void register(ResourceLocation id, Function<HolderLookup.Provider, PreparableReloadListener> listener) {
        register(id, listener, List.of());
    }

    void register(
        ResourceLocation id,
        Function<HolderLookup.Provider, PreparableReloadListener> listener,
        Collection<ResourceLocation> dependencies
    );
}
