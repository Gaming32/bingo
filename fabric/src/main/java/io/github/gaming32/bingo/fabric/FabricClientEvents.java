package io.github.gaming32.bingo.fabric;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.player.LocalPlayer;

import java.util.function.Consumer;

public class FabricClientEvents {
    public static final Event<Consumer<LocalPlayer>> PLAYER_QUIT = EventFactory.createArrayBacked(
        Consumer.class, handlers -> player -> {
            for (final Consumer<LocalPlayer> handler : handlers) {
                handler.accept(player);
            }
        }
    );
}
