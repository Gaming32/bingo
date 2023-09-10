package io.github.gaming32.bingo.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record IconTooltip(ResourceLocation icon) implements TooltipComponent {
}
