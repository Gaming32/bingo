package io.github.gaming32.bingo.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.ArrayList;
import java.util.List;

public class TooltipBuilder {
    private final List<ClientTooltipComponent> lines = new ArrayList<>();

    public TooltipBuilder add(ClientTooltipComponent component) {
        lines.add(component);
        return this;
    }

    public TooltipBuilder add(TooltipComponent component) {
        return add(ClientTooltipComponent.create(component));
    }

    public TooltipBuilder add(FormattedCharSequence text) {
        return add(ClientTooltipComponent.create(text));
    }

    public TooltipBuilder add(Component component) {
        return add(component.getVisualOrderText());
    }

    public void draw(Font font, GuiGraphics graphics, int mouseX, int mouseY) {
        draw(font, graphics, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE);
    }

    public void draw(Font font, GuiGraphics graphics, int mouseX, int mouseY, ClientTooltipPositioner positioner) {
        graphics.renderTooltipInternal(font, lines, mouseX, mouseY, positioner);
        graphics.flush();
    }
}
