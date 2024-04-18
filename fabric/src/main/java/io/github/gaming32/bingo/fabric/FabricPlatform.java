package io.github.gaming32.bingo.fabric;

import io.github.gaming32.bingo.multiloader.BingoPlatform;
import io.github.gaming32.bingo.multiloader.ClientEvents;
import io.github.gaming32.bingo.multiloader.Event;
import io.github.gaming32.bingo.network.BingoNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;

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
