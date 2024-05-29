package io.github.gaming32.bingo.client.platform.event;

import net.minecraft.client.gui.screens.Screen;

@FunctionalInterface
public interface MouseReleaseHandler {
    boolean onMouseReleased(Screen screen, double mouseX, double mouseY, int modifiers);
}
