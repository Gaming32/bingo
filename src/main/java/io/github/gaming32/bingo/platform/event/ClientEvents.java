package io.github.gaming32.bingo.platform.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;

import java.util.function.Consumer;

public class ClientEvents {
    public static final Event<KeyReleaseHandler> KEY_RELEASED_PRE = new Event<>();
    public static final Event<MouseReleaseHandler> MOUSE_RELEASED_PRE = new Event<>();
    public static final Event<Consumer<LocalPlayer>> PLAYER_QUIT = new Event<>();
    public static final Event<Consumer<Minecraft>> CLIENT_TICK_START = new Event<>();
    public static final Event<Consumer<Minecraft>> CLIENT_TICK_END = new Event<>();

    @FunctionalInterface
    public interface KeyReleaseHandler {
        boolean onKeyReleased(Screen screen, KeyEvent event);
    }

    @FunctionalInterface
    public interface MouseReleaseHandler {
        boolean onMouseReleased(Screen screen, MouseButtonEvent event);
    }
}
