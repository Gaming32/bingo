package io.github.gaming32.bingo.platform.registrar;

import io.github.gaming32.bingo.util.KeyMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;

public interface DataReloadListenerRegistrar {
    void register(Identifier id, ReloadListener listener);

    interface ReloadListener {
        void run(HolderLookup.Provider registries, KeyMap dataStore);
    }
}
