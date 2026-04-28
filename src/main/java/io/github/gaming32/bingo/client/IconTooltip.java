package io.github.gaming32.bingo.client;

import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record IconTooltip(Identifier icon) implements TooltipComponent {
}
