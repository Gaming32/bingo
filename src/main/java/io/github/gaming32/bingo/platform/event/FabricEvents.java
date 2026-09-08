package io.github.gaming32.bingo.platform.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerExplosion;

import java.util.function.BiConsumer;

public class FabricEvents {
    public static final Event<BiConsumer<ServerLevel, ServerExplosion>> SERVER_EXPLOSION = EventFactory.createArrayBacked(
        BiConsumer.class, handlers -> (level, explosion) -> {
            for (final var handler : handlers) {
                handler.accept(level, explosion);
            }
        }
    );
}
