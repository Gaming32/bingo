package io.github.gaming32.bingo.client.platform.registrar;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.function.Function;

public interface ClientTooltipRegistrar {
    <T extends TooltipComponent> void register(Class<T> clazz, Function<? super T, ? extends ClientTooltipComponent> factory);
}
