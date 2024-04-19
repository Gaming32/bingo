package io.github.gaming32.bingo.neoforge;

import io.github.gaming32.bingo.neoforge.registry.NeoForgeDeferredRegister;
import io.github.gaming32.bingo.network.BingoNetworking;
import io.github.gaming32.bingo.platform.BingoPlatform;
import io.github.gaming32.bingo.platform.event.ClientEvents;
import io.github.gaming32.bingo.platform.event.Event;
import io.github.gaming32.bingo.platform.registry.DeferredRegister;
import io.github.gaming32.bingo.platform.registry.RegistryBuilder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.nio.file.Path;
import java.util.function.Consumer;

public class NeoForgePlatform extends BingoPlatform {
    private final IEventBus modEventBus;
    private final BingoNetworking networking;

    public NeoForgePlatform(IEventBus modEventBus) {
        this.modEventBus = modEventBus;
        networking = new BingoNetworkingImpl(modEventBus);
        registerEvents();
    }

    @Override
    public BingoNetworking getNetworking() {
        return networking;
    }

    @Override
    public boolean isClient() {
        return FMLEnvironment.dist.isClient();
    }

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean isModLoaded(String id) {
        return ModList.get().isLoaded(id);
    }

    @Override
    public void registerClientTooltips(Consumer<ClientTooltipRegistrar> handler) {
        modEventBus.addListener((RegisterClientTooltipComponentFactoriesEvent event) ->
            handler.accept(event::register)
        );
    }

    @Override
    public void registerKeyMappings(Consumer<Consumer<KeyMapping>> handler) {
        modEventBus.addListener((RegisterKeyMappingsEvent event) -> handler.accept(event::register));
    }

    @Override
    public void registerDataReloadListeners(Consumer<DataReloadListenerRegistrar> handler) {
        modEventBus.addListener((AddReloadListenerEvent event) -> handler.accept(
            (id, listener, dependencies) -> event.addListener(listener)
        ));
    }

    @Override
    public <T> DeferredRegister<T> createDeferredRegister(Registry<T> registry) {
        final NeoForgeDeferredRegister<T> register = new NeoForgeDeferredRegister<>(registry);
        register.getDeferredRegister().register(modEventBus);
        return register;
    }

    @Override
    public <T> DeferredRegister<T> buildDeferredRegister(RegistryBuilder builder) {
        return createDeferredRegister(
            new net.neoforged.neoforge.registries.RegistryBuilder<>(ResourceKey.<T>createRegistryKey(builder.getId()))
                .sync(builder.isSynced())
                .defaultKey(builder.getDefaultId()).create()
        );
    }

    private void registerEvents() {
        final IEventBus bus = NeoForge.EVENT_BUS;
        Event.REGISTER_COMMANDS.setRegistrar(handler -> bus.addListener((RegisterCommandsEvent event) ->
            handler.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection())
        ));
        Event.PLAYER_JOIN.setRegistrar(handler -> bus.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                handler.accept(serverPlayer);
            }
        }));
        Event.PLAYER_QUIT.setRegistrar(handler -> bus.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                handler.accept(serverPlayer);
            }
        }));
        Event.SERVER_STARTED.setRegistrar(handler -> bus.addListener((ServerStartedEvent event) ->
            handler.accept(event.getServer())
        ));
        Event.SERVER_STOPPING.setRegistrar(handler -> bus.addListener((ServerStoppingEvent event) ->
            handler.accept(event.getServer())
        ));
        Event.SERVER_STOPPED.setRegistrar(handler -> bus.addListener((ServerStoppedEvent event) ->
            handler.accept(event.getServer())
        ));
        Event.RIGHT_CLICK_ITEM.setRegistrar(handler -> bus.addListener((PlayerInteractEvent.RightClickItem event) ->
            handler.accept(event.getEntity(), event.getHand())
        ));
        Event.EXPLOSION_START.setRegistrar(handler -> bus.addListener((ExplosionEvent.Start event) ->
            handler.accept(event.getLevel(), event.getExplosion())
        ));
        Event.SERVER_TICK_END.setRegistrar(handler -> bus.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                handler.accept(event.getServer());
            }
        }));

        if (isClient()) {
            ClientEvents.RENDER_HUD.setRegistrar(handler -> bus.addListener((RenderGuiEvent.Post event) ->
                handler.renderHud(event.getGuiGraphics(), event.getPartialTick())
            ));
            ClientEvents.KEY_RELEASED_PRE.setRegistrar(handler -> bus.addListener((ScreenEvent.KeyReleased.Pre event) -> {
                if (handler.onKeyReleased(event.getScreen(), event.getKeyCode(), event.getScanCode(), event.getModifiers())) {
                    event.setCanceled(true);
                }
            }));
            ClientEvents.MOUSE_RELEASED_PRE.setRegistrar(handler -> bus.addListener((ScreenEvent.MouseButtonReleased.Pre event) -> {
                if (handler.onMouseReleased(event.getScreen(), event.getMouseX(), event.getMouseY(), event.getButton())) {
                    event.setCanceled(true);
                }
            }));
            ClientEvents.PLAYER_QUIT.setRegistrar(handler -> bus.addListener((ClientPlayerNetworkEvent.LoggingOut event) ->
                handler.accept(event.getPlayer())
            ));
            ClientEvents.CLIENT_TICK_START.setRegistrar(handler -> bus.addListener((TickEvent.ClientTickEvent event) -> {
                if (event.phase == TickEvent.Phase.START) {
                    handler.accept(Minecraft.getInstance());
                }
            }));
        }
    }
}
