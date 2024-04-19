package io.github.gaming32.bingo.fabric;

import io.github.gaming32.bingo.fabric.event.FabricClientEvents;
import io.github.gaming32.bingo.fabric.event.FabricEvents;
import io.github.gaming32.bingo.network.BingoNetworking;
import io.github.gaming32.bingo.platform.BingoPlatform;
import io.github.gaming32.bingo.platform.event.ClientEvents;
import io.github.gaming32.bingo.platform.event.Event;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;

public class FabricPlatform extends BingoPlatform {
    private final BingoNetworking networking;

    public FabricPlatform() {
        networking = new BingoNetworkingImpl();
        registerEvents();
    }

    @Override
    public BingoNetworking getNetworking() {
        return networking;
    }

    @Override
    public boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean isModLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id);
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
    public void registerKeyMappings(Consumer<Consumer<KeyMapping>> handler) {
        handler.accept(KeyBindingHelper::registerKeyBinding);
    }

    private void registerEvents() {
        Event.REGISTER_COMMANDS.setRegistrar(handler -> CommandRegistrationCallback.EVENT.register(handler::register));
        Event.PLAYER_JOIN.setRegistrar(FabricEvents.PLAYER_JOIN::register);
        Event.PLAYER_QUIT.setRegistrar(FabricEvents.PLAYER_QUIT::register);
        Event.SERVER_STARTED.setRegistrar(handler -> ServerLifecycleEvents.SERVER_STARTED.register(handler::accept));
        Event.SERVER_STOPPING.setRegistrar(handler -> ServerLifecycleEvents.SERVER_STOPPING.register(handler::accept));
        Event.SERVER_STOPPED.setRegistrar(handler -> ServerLifecycleEvents.SERVER_STOPPED.register(handler::accept));
        Event.RIGHT_CLICK_ITEM.setRegistrar(handler -> UseItemCallback.EVENT.register(((player, world, hand) -> {
            handler.accept(player, hand);
            return InteractionResultHolder.pass(ItemStack.EMPTY);
        })));
        Event.EXPLOSION_START.setRegistrar(FabricEvents.EXPLOSION::register);
        Event.SERVER_TICK_END.setRegistrar(handler -> ServerTickEvents.END_SERVER_TICK.register(handler::accept));

        if (isClient()) {
            ClientEvents.RENDER_HUD.setRegistrar(handler -> HudRenderCallback.EVENT.register(handler::renderHud));
            ClientEvents.KEY_RELEASED_PRE.setRegistrar(handler -> ScreenEvents.BEFORE_INIT.register(
                (client, screen, scaledWidth, scaledHeight) ->
                    ScreenKeyboardEvents.allowKeyRelease(screen).register(handler::onKeyReleased)
            ));
            ClientEvents.MOUSE_RELEASED_PRE.setRegistrar(handler -> ScreenEvents.BEFORE_INIT.register(
                (client, screen, scaledWidth, scaledHeight) ->
                    ScreenMouseEvents.allowMouseRelease(screen).register(handler::onMouseReleased)
            ));
            ClientEvents.PLAYER_QUIT.setRegistrar(FabricClientEvents.PLAYER_QUIT::register);
            ClientEvents.CLIENT_TICK_START.setRegistrar(handler -> ClientTickEvents.START_CLIENT_TICK.register(handler::accept));
        }
    }
}
