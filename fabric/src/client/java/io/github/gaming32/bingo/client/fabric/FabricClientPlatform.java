package io.github.gaming32.bingo.client.fabric;

import io.github.gaming32.bingo.client.fabric.event.FabricClientEvents;
import io.github.gaming32.bingo.client.platform.BingoClientPlatform;
import io.github.gaming32.bingo.client.platform.event.ClientEvents;
import io.github.gaming32.bingo.client.platform.registrar.ClientTooltipRegistrar;
import io.github.gaming32.bingo.client.platform.registrar.KeyMappingBuilder;
import io.github.gaming32.bingo.client.platform.registrar.KeyMappingBuilderImpl;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.function.Consumer;
import java.util.function.Function;

public class FabricClientPlatform extends BingoClientPlatform {
    public FabricClientPlatform() {
        registerEvents();
    }

    @Override
    public void registerClientTooltips(Consumer<ClientTooltipRegistrar> handler) {
        handler.accept(new ClientTooltipRegistrar() {
            @Override
            public <T extends TooltipComponent> void register(Class<T> clazz, Function<? super T, ? extends ClientTooltipComponent> factory) {
                TooltipComponentCallback.EVENT.register(data -> {
                    if (clazz.isInstance(data)) {
                        return factory.apply(clazz.cast(data));
                    }
                    return null;
                });
            }
        });
    }

    @Override
    public void registerKeyMappings(Consumer<KeyMappingBuilder> handler) {
        final KeyMappingBuilderImpl builder = new KeyMappingBuilderImpl();
        handler.accept(builder);
        builder.registerAll(KeyBindingHelper::registerKeyBinding);
        ClientTickEvents.END_CLIENT_TICK.register(builder::handleAll);
    }

    private void registerEvents() {
        ClientEvents.KEY_RELEASED_PRE.setRegistrar(handler -> ScreenEvents.BEFORE_INIT.register(
            (client, screen, scaledWidth, scaledHeight) ->
                ScreenKeyboardEvents.allowKeyRelease(screen).register((screen1, key, scancode, modifiers) ->
                    !handler.onKeyReleased(screen1, key, scancode, modifiers)
                )
        ));
        ClientEvents.MOUSE_RELEASED_PRE.setRegistrar(handler -> ScreenEvents.BEFORE_INIT.register(
            (client, screen, scaledWidth, scaledHeight) ->
                ScreenMouseEvents.allowMouseRelease(screen).register((screen1, mouseX, mouseY, button) ->
                    !handler.onMouseReleased(screen1, mouseX, mouseY, button)
                )
        ));
        ClientEvents.PLAYER_QUIT.setRegistrar(FabricClientEvents.PLAYER_QUIT::register);
        ClientEvents.CLIENT_TICK_START.setRegistrar(handler -> ClientTickEvents.START_CLIENT_TICK.register(handler::accept));
        ClientEvents.CLIENT_TICK_END.setRegistrar(handler -> ClientTickEvents.END_CLIENT_TICK.register(handler::accept));
    }
}
