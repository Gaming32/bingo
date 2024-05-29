package io.github.gaming32.bingo.client.platform.event;

import io.github.gaming32.bingo.platform.event.Event;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import java.util.function.Consumer;

public class ClientEvents {
    public static final Event<KeyReleaseHandler> KEY_RELEASED_PRE = new Event<>();
    public static final Event<MouseReleaseHandler> MOUSE_RELEASED_PRE = new Event<>();
    public static final Event<Consumer<LocalPlayer>> PLAYER_QUIT = new Event<>();
    public static final Event<Consumer<Minecraft>> CLIENT_TICK_START = new Event<>();
    public static final Event<Consumer<Minecraft>> CLIENT_TICK_END = new Event<>();
}
