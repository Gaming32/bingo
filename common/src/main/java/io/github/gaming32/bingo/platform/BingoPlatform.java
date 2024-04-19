package io.github.gaming32.bingo.platform;

import io.github.gaming32.bingo.network.BingoNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.nio.file.Path;
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

    public interface ClientTooltipRegistrar {
        <T extends TooltipComponent> void register(Class<T> clazz, Function<? super T, ? extends ClientTooltipComponent> factory);
    }
}
