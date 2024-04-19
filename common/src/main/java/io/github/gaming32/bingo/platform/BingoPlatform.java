package io.github.gaming32.bingo.platform;

import io.github.gaming32.bingo.network.BingoNetworking;
import io.github.gaming32.bingo.platform.registry.DeferredRegister;
import io.github.gaming32.bingo.platform.registry.RegistryBuilder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class BingoPlatform {
    public static BingoPlatform platform;

    public abstract BingoNetworking getNetworking();

    public abstract boolean isClient();

    public abstract Path getConfigDir();

    public abstract boolean isModLoaded(String id);

    public abstract void registerClientTooltips(Consumer<ClientTooltipRegistrar> handler);

    public abstract void registerKeyMappings(Consumer<Consumer<KeyMapping>> handler);

    public abstract void registerDataReloadListeners(Consumer<DataReloadListenerRegistrar> handler);

    public abstract <T> DeferredRegister<T> createDeferredRegister(Registry<T> registry);

    public abstract <T> DeferredRegister<T> buildDeferredRegister(RegistryBuilder builder);

    public interface ClientTooltipRegistrar {
        <T extends TooltipComponent> void register(Class<T> clazz, Function<? super T, ? extends ClientTooltipComponent> factory);
    }

    public interface DataReloadListenerRegistrar {
        default void register(ResourceLocation id, PreparableReloadListener listener) {
            register(id, listener, List.of());
        }

        void register(ResourceLocation id, PreparableReloadListener listener, Collection<ResourceLocation> dependencies);
    }
}
