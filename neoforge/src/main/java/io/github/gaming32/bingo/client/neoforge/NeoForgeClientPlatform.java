package io.github.gaming32.bingo.client.neoforge;

import io.github.gaming32.bingo.client.platform.BingoClientPlatform;
import io.github.gaming32.bingo.client.platform.registrar.ClientTooltipRegistrar;
import io.github.gaming32.bingo.client.platform.registrar.KeyMappingBuilder;
import io.github.gaming32.bingo.client.platform.registrar.KeyMappingBuilderImpl;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;

import java.util.function.Consumer;

public class NeoForgeClientPlatform extends BingoClientPlatform {
    private final IEventBus modEventBus;

    public NeoForgeClientPlatform(IEventBus modEventBus) {
        this.modEventBus = modEventBus;
    }

    @Override
    public void registerClientTooltips(Consumer<ClientTooltipRegistrar> handler) {
        modEventBus.addListener((RegisterClientTooltipComponentFactoriesEvent event) ->
            handler.accept(event::register)
        );
    }

    @Override
    public void registerKeyMappings(Consumer<KeyMappingBuilder> handler) {
        final KeyMappingBuilderImpl builder = new KeyMappingBuilderImpl() {
            @Override
            public KeyMappingExt register(Consumer<Minecraft> action) {
                final KeyMappingExt mapping = super.register(action);
                mapping.mapping().setKeyConflictContext(KeyConflictContext.valueOf(mapping.conflictContext().name()));
                return mapping;
            }
        };
        handler.accept(builder);
        modEventBus.addListener((RegisterKeyMappingsEvent event) -> builder.registerAll(event::register));
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> builder.handleAll(Minecraft.getInstance()));
    }
}
