package io.github.gaming32.bingo.neoforge;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.github.gaming32.bingo.neoforge.registry.NeoForgeDeferredRegister;
import io.github.gaming32.bingo.network.BingoNetworking;
import io.github.gaming32.bingo.platform.BingoPlatform;
import io.github.gaming32.bingo.platform.event.ClientEvents;
import io.github.gaming32.bingo.platform.event.Event;
import io.github.gaming32.bingo.platform.registrar.ClientTooltipRegistrar;
import io.github.gaming32.bingo.platform.registrar.DataReloadListenerRegistrar;
import io.github.gaming32.bingo.platform.registrar.DatapackRegistryRegistrar;
import io.github.gaming32.bingo.platform.registrar.KeyMappingBuilder;
import io.github.gaming32.bingo.platform.registrar.KeyMappingBuilderImpl;
import io.github.gaming32.bingo.platform.registrar.PictureInPictureRendererRegistrar;
import io.github.gaming32.bingo.platform.registry.DeferredRegister;
import io.github.gaming32.bingo.platform.registry.RegistryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Path;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
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
    public void registerPictureInPictureRenderers(Consumer<PictureInPictureRendererRegistrar> handler) {
        modEventBus.addListener((RegisterPictureInPictureRenderersEvent event) ->
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

    @Override
    public void registerDataReloadListeners(Consumer<DataReloadListenerRegistrar> handler) {
        NeoForge.EVENT_BUS.addListener((AddServerReloadListenersEvent event) -> handler.accept(
            (id, listenerCreator, dependencies) -> {
                PreparableReloadListener listener = listenerCreator.create(Optional.empty());
                if (!(listener instanceof ContextAwareReloadListener)) {
                    listener = new BingoReloadListener(listener);
                }
                event.addListener(id, listener);
                for (final var dependency : dependencies) {
                    event.addDependency(dependency, id);
                }
            }
        ));
    }

    @Override
    public HolderLookup.Provider getRegistryAccessFromReloadListener(PreparableReloadListener listener) {
        return BingoReloadListener.getRegistryLookup(BingoReloadListener.asContextAwareListener(listener));
    }

    @Override
    public RegistryOps<JsonElement> makeRegistryOpsFromReloadListener(PreparableReloadListener listener, DynamicOps<JsonElement> wrapped) {
        return BingoReloadListener.makeConditionalOps(BingoReloadListener.asContextAwareListener(listener), wrapped);
    }

    @Override
    public void registerDatapackRegistries(Consumer<DatapackRegistryRegistrar> handler) {
        modEventBus.addListener((DataPackRegistryEvent.NewRegistry event) -> handler.accept(new DatapackRegistryRegistrar() {
            @Override
            public <T> void unsynced(ResourceKey<Registry<T>> registryKey, Codec<T> codec) {
                event.dataPackRegistry(registryKey, codec);
            }

            @Override
            public <T> void synced(ResourceKey<Registry<T>> registryKey, Codec<T> codec, @Nullable Codec<T> networkCodec) {
                event.dataPackRegistry(registryKey, codec, networkCodec);
            }
        }));
    }

    @Override
    public <T> DeferredRegister<T> createDeferredRegister(Registry<T> registry) {
        final NeoForgeDeferredRegister<T> register = new NeoForgeDeferredRegister<>(registry);
        register.getDeferredRegister().register(modEventBus);
        return register;
    }

    @Override
    public <T> DeferredRegister<T> buildDeferredRegister(RegistryBuilder<T> builder) {
        final Registry<T> registry =
            new net.neoforged.neoforge.registries.RegistryBuilder<>(builder.getKey())
                .sync(builder.isSynced())
                .defaultKey(builder.getDefaultId())
                .create();
        modEventBus.addListener((NewRegistryEvent event) -> event.register(registry));
        return createDeferredRegister(registry);
    }

    private void registerEvents() {
        final IEventBus bus = NeoForge.EVENT_BUS;
        Event.REGISTER_COMMANDS.setRegistrar(handler -> bus.addListener((RegisterCommandsEvent event) ->
            handler.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection())
        ));
        Event.REGISTER_CONFIGURATION_TASKS.setRegistrar(handler -> modEventBus.addListener((RegisterConfigurationTasksEvent event) ->
            handler.accept(new NeoForgeConfigurationTaskRegistrar(event))
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
        Event.COPY_PLAYER.setRegistrar(handler -> bus.addListener((PlayerEvent.Clone event) ->
            handler.accept((ServerPlayer)event.getOriginal(), (ServerPlayer)event.getEntity())
        ));
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
        Event.SERVER_EXPLOSION_START.setRegistrar(handler -> bus.addListener((ExplosionEvent.Start event) ->
            handler.accept((ServerLevel)event.getLevel(), event.getExplosion())
        ));
        Event.SERVER_TICK_END.setRegistrar(handler -> bus.addListener((ServerTickEvent.Post event) ->
            handler.accept(event.getServer())
        ));

        if (isClient()) {
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
            ClientEvents.CLIENT_TICK_START.setRegistrar(handler -> bus.addListener((ClientTickEvent.Pre event) ->
                handler.accept(Minecraft.getInstance())
            ));
            ClientEvents.CLIENT_TICK_END.setRegistrar(handler -> bus.addListener((ClientTickEvent.Post event) ->
                handler.accept(Minecraft.getInstance())
            ));
        }
    }

    private static class BingoReloadListener extends ContextAwareReloadListener {
        private static final WeakHashMap<PreparableReloadListener, BingoReloadListener> bingoReloadListeners = new WeakHashMap<>();

        // These are protected methods and part of API, but we need to call them reflectively for other instances of CARL.
        private static final MethodHandle GET_REGISTRY_LOOKUP = getMethodHandle("getRegistryLookup", HolderLookup.Provider.class);
        private static final MethodHandle MAKE_CONDITIONAL_OPS = getMethodHandle("makeConditionalOps", ConditionalOps.class, DynamicOps.class);

        private static MethodHandle getMethodHandle(String name, Class<?> returnType, Class<?>... args) {
            try {
                return MethodHandles.privateLookupIn(ContextAwareReloadListener.class, MethodHandles.lookup()).findVirtual(ContextAwareReloadListener.class, name, MethodType.methodType(returnType, args));
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
        }

        private final PreparableReloadListener delegate;

        private BingoReloadListener(PreparableReloadListener delegate) {
            this.delegate = delegate;
            bingoReloadListeners.put(delegate, this);
        }

        public static ContextAwareReloadListener asContextAwareListener(PreparableReloadListener listener) {
            if (listener instanceof ContextAwareReloadListener carl) {
                return carl;
            } else {
                BingoReloadListener bingoListener = bingoReloadListeners.get(listener);
                if (bingoListener == null) {
                    throw new IllegalArgumentException("Reload listener was not created via the bingo platform");
                }
                return bingoListener;
            }
        }

        @Override
        @NotNull
        public CompletableFuture<Void> reload(@NotNull PreparationBarrier preparationBarrier, @NotNull ResourceManager resourceManager, @NotNull Executor executor, @NotNull Executor executor1) {
            return delegate.reload(preparationBarrier, resourceManager, executor, executor1);
        }

        @Override
        @NotNull
        public String getName() {
            return delegate.getName();
        }

        @SuppressWarnings("unused")
        private void unused() {
            // This method exists to remind us to update the method handles in this class when these methods change
            HolderLookup.Provider ignored = getRegistryLookup();
            ConditionalOps<JsonElement> ignored1 = makeConditionalOps(JsonOps.INSTANCE);
        }

        public static HolderLookup.Provider getRegistryLookup(ContextAwareReloadListener carl) {
            try {
                return (HolderLookup.Provider) GET_REGISTRY_LOOKUP.invoke(carl);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        @SuppressWarnings("unchecked")
        public static ConditionalOps<JsonElement> makeConditionalOps(ContextAwareReloadListener carl, DynamicOps<JsonElement> ops) {
            try {
                return (ConditionalOps<JsonElement>) MAKE_CONDITIONAL_OPS.invoke(carl, ops);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
