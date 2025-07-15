package io.github.gaming32.bingo.fabric;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import io.github.gaming32.bingo.fabric.event.FabricClientEvents;
import io.github.gaming32.bingo.fabric.event.FabricEvents;
import io.github.gaming32.bingo.fabric.registry.FabricDeferredRegister;
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
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialGuiElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
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
            TooltipComponentCallback.EVENT.register(component -> {
                final var factory = builtFactories.get(component.getClass());
                return factory != null ? factory.apply(component) : null;
            });
        }
    }

    @Override
    public void registerPictureInPictureRenderers(Consumer<PictureInPictureRendererRegistrar> handler) {
        handler.accept(FabricPlatform::registerPictureInPictureRenderer);
    }

    private static <S extends PictureInPictureRenderState> void registerPictureInPictureRenderer(Class<S> stateClass, Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<S>> factory) {
        SpecialGuiElementRegistry.register(context -> factory.apply(context.vertexConsumers()));
    }

    @Override
    public void registerKeyMappings(Consumer<KeyMappingBuilder> handler) {
        final KeyMappingBuilderImpl builder = new KeyMappingBuilderImpl();
        handler.accept(builder);
        builder.registerAll(KeyBindingHelper::registerKeyBinding);
        ClientTickEvents.END_CLIENT_TICK.register(builder::handleAll);
    }

    @Override
    public void registerDataReloadListeners(Consumer<DataReloadListenerRegistrar> handler) {
        final var helper = ResourceManagerHelper.get(PackType.SERVER_DATA);
        handler.accept((id, listener, dependencies) -> helper.registerReloadListener(id, listener.andThen(
            vanilla -> new IdentifiableResourceReloadListener() {
                @Override
                public ResourceLocation getFabricId() {
                    return id;
                }

                @Override
                public Collection<ResourceLocation> getFabricDependencies() {
                    return dependencies;
                }

                @Override
                public @NotNull CompletableFuture<Void> reload(
                    PreparationBarrier preparationBarrier,
                    ResourceManager resourceManager,
                    Executor backgroundExecutor,
                    Executor gameExecutor
                ) {
                    return vanilla.reload(
                        preparationBarrier,
                        resourceManager,
                        backgroundExecutor,
                        gameExecutor
                    );
                }

                @Override
                public @NotNull String getName() {
                    return vanilla.getName();
                }
            }
        )));
    }

    @Override
    public void registerDatapackRegistries(Consumer<DatapackRegistryRegistrar> handler) {
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

    @Override
    public <T> DeferredRegister<T> createDeferredRegister(Registry<T> registry) {
        return new FabricDeferredRegister<>(registry);
    }

    @Override
    public <T> DeferredRegister<T> buildDeferredRegister(RegistryBuilder<T> builder) {
        final var fabricBuilder = builder.getDefaultId() != null
            ? FabricRegistryBuilder.createDefaulted(builder.getKey(), builder.getDefaultId())
            : FabricRegistryBuilder.createSimple(builder.getKey());
        if (builder.isSynced()) {
            fabricBuilder.attribute(RegistryAttribute.SYNCED);
        }
        return new FabricDeferredRegister<>(fabricBuilder.buildAndRegister());
    }

    private void registerEvents() {
        Event.REGISTER_COMMANDS.setRegistrar(handler -> CommandRegistrationCallback.EVENT.register(handler::register));
        Event.REGISTER_CONFIGURATION_TASKS.setRegistrar(handler -> ServerConfigurationConnectionEvents.CONFIGURE.register(
            (packetListener, server) ->
                handler.accept(new FabricConfigurationTaskRegistrar(packetListener))
            )
        );
        Event.PLAYER_JOIN.setRegistrar(FabricEvents.PLAYER_JOIN::register);
        Event.PLAYER_QUIT.setRegistrar(FabricEvents.PLAYER_QUIT::register);
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

        if (isClient()) {
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
}
