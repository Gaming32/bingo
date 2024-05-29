package io.github.gaming32.bingo.client.platform.event;

import net.minecraft.client.gui.screens.Screen;

@FunctionalInterface
public interface KeyReleaseHandler {
    boolean onKeyReleased(Screen screen, int keyCode, int scanCode, int modifiers);
}
