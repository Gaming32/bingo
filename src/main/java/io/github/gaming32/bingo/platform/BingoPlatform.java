package io.github.gaming32.bingo.platform;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import io.github.gaming32.bingo.platform.event.FabricEvents;
import io.github.gaming32.bingo.network.BingoNetworking;
import io.github.gaming32.bingo.platform.event.ConfigurationTaskRegistrar;
import io.github.gaming32.bingo.platform.event.Event;
import io.github.gaming32.bingo.platform.registrar.ClientTooltipRegistrar;
import io.github.gaming32.bingo.platform.registrar.DataReloadListenerRegistrar;
import io.github.gaming32.bingo.platform.registrar.DatapackRegistryRegistrar;
import io.github.gaming32.bingo.platform.registrar.KeyMappingBuilder;
import io.github.gaming32.bingo.platform.registrar.KeyMappingBuilderImpl;
import io.github.gaming32.bingo.platform.registrar.PictureInPictureRendererRegistrar;
import io.github.gaming32.bingo.util.Identifiers;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.networking.v1.FriendlyByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

public final class BingoPlatform {
    public static final Identifier PROTOCOL_VERSION_PACKET = Identifiers.bingo("protocol_version");

    private BingoPlatform() {
    }

    public static boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    public static Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static boolean isModLoaded(String id) {
        return FabricLoader.getInstance().isModLoaded(id);
    }

    public static void registerClientTooltips(Consumer<ClientTooltipRegistrar> handler) {
        final var factories = ImmutableMap.<Class<? extends TooltipComponent>, Function<TooltipComponent, ClientTooltipComponent>>builder();
        handler.accept(new ClientTooltipRegistrar() {
            @Override
            @SuppressWarnings("unchecked")
            public <T extends TooltipComponent> void register(Class<T> clazz, Function<? super T, ? extends ClientTooltipComponent> factory) {
                factories.put(clazz, (Function<TooltipComponent, ClientTooltipComponent>) factory);
            }
        });
        final var builtFactories = factories.build();
        if (!builtFactories.isEmpty()) {
            ClientTooltipComponentCallback.EVENT.register(component -> {
                final var factory = builtFactories.get(component.getClass());
                return factory != null ? factory.apply(component) : null;
            });
        }
    }

    public static void registerPictureInPictureRenderers(Consumer<PictureInPictureRendererRegistrar> handler) {
        handler.accept(BingoPlatform::registerPictureInPictureRenderer);
    }

    private static <S extends PictureInPictureRenderState> void registerPictureInPictureRenderer(Class<S> stateClass, Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<S>> factory) {
        PictureInPictureRendererRegistry.register(context -> factory.apply(context.bufferSource()));
    }

    public static void registerKeyMappings(Consumer<KeyMappingBuilder> handler) {
        final KeyMappingBuilderImpl builder = new KeyMappingBuilderImpl() {
            @Override
            public KeyMapping.Category registerCategory(Identifier id) {
                return KeyMapping.Category.register(id);
            }
        };
        handler.accept(builder);
        builder.registerAll(KeyMappingHelper::registerKeyMapping);
        ClientTickEvents.END_CLIENT_TICK.register(builder::handleAll);
    }

    public static void registerDataReloadListeners(Consumer<DataReloadListenerRegistrar> handler) {
        final var helper = ResourceLoader.get(PackType.SERVER_DATA);
        handler.accept((id, listener, dependencies) -> {
            helper.registerReloadListener(id, new PreparableReloadListener() {
                private WeakReference<HolderLookup.Provider> currentLookup;
                private PreparableReloadListener delegate;

                @Override
                @NotNull
                public CompletableFuture<Void> reload(SharedState sharedState, Executor executor, PreparationBarrier preparationBarrier, Executor executor2) {
                    HolderLookup.Provider lookup = sharedState.get(ResourceLoader.REGISTRY_LOOKUP_KEY);
                    if (currentLookup == null || lookup != currentLookup.get()) {
                        currentLookup = new WeakReference<>(lookup);
                        delegate = listener.apply(lookup);
                    }

                    return delegate.reload(sharedState, executor, preparationBarrier, executor2);
                }
            });
        });
    }

    public static void registerDatapackRegistries(Consumer<DatapackRegistryRegistrar> handler) {
        handler.accept(new DatapackRegistryRegistrar() {
            @Override
            public <T> void unsynced(ResourceKey<Registry<T>> registryKey, Codec<T> codec) {
                DynamicRegistries.register(registryKey, codec);
            }

            @Override
            public <T> void synced(ResourceKey<Registry<T>> registryKey, Codec<T> codec, Codec<T> networkCodec) {
                DynamicRegistries.registerSynced(registryKey, codec, networkCodec);
            }
        });
    }

    public static void registerEvents() {
        Event.REGISTER_COMMANDS.setRegistrar(handler -> CommandRegistrationCallback.EVENT.register(handler::register));
        Event.REGISTER_CONFIGURATION_TASKS.setRegistrar(handler -> ServerConfigurationConnectionEvents.CONFIGURE.register(
                (packetListener, server) ->
                    handler.accept(new ConfigurationTaskRegistrar() {
                        @Override
                        public boolean canSend(CustomPacketPayload.Type<?> packetType) {
                            return ServerConfigurationNetworking.canSend(packetListener, packetType);
                        }

                        @Override
                        public void addTask(ConfigurationTask task) {
                            packetListener.addTask(task);
                        }
                    })
            )
        );
        Event.PLAYER_JOIN.setRegistrar(handler -> ServerPlayerEvents.JOIN.register(handler::accept));
        Event.PLAYER_QUIT.setRegistrar(handler -> ServerPlayerEvents.LEAVE.register(handler::accept));
        Event.COPY_PLAYER.setRegistrar(handler -> ServerPlayerEvents.COPY_FROM.register(
            (oldPlayer, newPlayer, alive) -> handler.accept(oldPlayer, newPlayer)
        ));
        Event.SERVER_STARTED.setRegistrar(handler -> ServerLifecycleEvents.SERVER_STARTED.register(handler::accept));
        Event.SERVER_STOPPING.setRegistrar(handler -> ServerLifecycleEvents.SERVER_STOPPING.register(handler::accept));
        Event.SERVER_STOPPED.setRegistrar(handler -> ServerLifecycleEvents.SERVER_STOPPED.register(handler::accept));
        Event.RIGHT_CLICK_ITEM.setRegistrar(handler -> UseItemCallback.EVENT.register((player, world, hand) -> {
            handler.accept(player, hand);
            return InteractionResult.PASS;
        }));
        Event.SERVER_EXPLOSION_START.setRegistrar(FabricEvents.SERVER_EXPLOSION::register);
        Event.SERVER_TICK_END.setRegistrar(handler -> ServerTickEvents.END_SERVER_TICK.register(handler::accept));

        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            final FriendlyByteBuf buf = FriendlyByteBufs.create();
            buf.writeVarInt(BingoNetworking.PROTOCOL_VERSION);
            sender.sendPacket(PROTOCOL_VERSION_PACKET, buf);
        });

        ServerLoginNetworking.registerGlobalReceiver(PROTOCOL_VERSION_PACKET, (server, handler, understood, buf, synchronizer, responseSender) -> {
            if (!understood) return;
            final int clientVersion = buf.readVarInt();
            if (clientVersion < BingoNetworking.PROTOCOL_VERSION) {
                handler.disconnect(Component.translatable("bingo.outdated_client", clientVersion, BingoNetworking.PROTOCOL_VERSION));
            } else if (BingoNetworking.PROTOCOL_VERSION < clientVersion) {
                handler.disconnect(Component.translatable("bingo.outdated_server", BingoNetworking.PROTOCOL_VERSION, clientVersion));
            }
        });
    }
}
