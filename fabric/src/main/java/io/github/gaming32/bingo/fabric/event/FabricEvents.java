package io.github.gaming32.bingo.fabric.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ServerExplosion;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FabricEvents {
    public static final Event<Consumer<ServerPlayer>> PLAYER_JOIN = EventFactory.createArrayBacked(
        Consumer.class, handlers -> player -> {
            for (final Consumer<ServerPlayer> handler : handlers) {
                handler.accept(player);
            }
        }
    );

    public static final Event<Consumer<ServerPlayer>> PLAYER_QUIT = EventFactory.createArrayBacked(
        Consumer.class, handlers -> player -> {
            for (final Consumer<ServerPlayer> handler : handlers) {
                handler.accept(player);
            }
        }
    );

    public static final Event<BiConsumer<ServerLevel, ServerExplosion>> SERVER_EXPLOSION = EventFactory.createArrayBacked(
        BiConsumer.class, handlers -> (level, explosion) -> {
            for (final var handler : handlers) {
                handler.accept(level, explosion);
            }
        }
    );
}
