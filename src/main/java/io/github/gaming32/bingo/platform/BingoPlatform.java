package io.github.gaming32.bingo.platform;

import com.mojang.serialization.Codec;
import io.github.gaming32.bingo.platform.event.ConfigurationTaskRegistrar;
import io.github.gaming32.bingo.platform.event.Event;
import io.github.gaming32.bingo.platform.registrar.ClientTooltipRegistrar;
import io.github.gaming32.bingo.platform.registrar.DataReloadListenerRegistrar;
import io.github.gaming32.bingo.platform.registrar.DatapackRegistryRegistrar;
import io.github.gaming32.bingo.platform.registrar.KeyMappingBuilder;
import io.github.gaming32.bingo.platform.registrar.KeyMappingBuilderImpl;
import io.github.gaming32.bingo.platform.registrar.PictureInPictureRendererRegistrar;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class BingoPlatform {
    private static IEventBus modEventBus;

    private BingoPlatform() {
    }

    public static IEventBus getModEventBus() {
        return modEventBus;
    }

    public static void setModEventBus(IEventBus modEventBus) {
        BingoPlatform.modEventBus = modEventBus;
    }

    public static boolean isClient() {
        return FMLEnvironment.getDist().isClient();
    }

    public static Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static boolean isModLoaded(String id) {
        return ModList.get().isLoaded(id);
    }

    public static void registerClientTooltips(Consumer<ClientTooltipRegistrar> handler) {
        modEventBus.addListener((RegisterClientTooltipComponentFactoriesEvent event) ->
            handler.accept(event::register)
        );
    }

    public static void registerPictureInPictureRenderers(Consumer<PictureInPictureRendererRegistrar> handler) {
        modEventBus.addListener((RegisterPictureInPictureRenderersEvent event) ->
            handler.accept(event::register)
        );
    }

    public static void registerKeyMappings(Consumer<KeyMappingBuilder> handler) {
        List<KeyMapping.Category> categories = new ArrayList<>();

        final KeyMappingBuilderImpl builder = new KeyMappingBuilderImpl() {
            @Override
            public KeyMappingExt register(Consumer<Minecraft> action) {
                final KeyMappingExt mapping = super.register(action);
                mapping.mapping().setKeyConflictContext(convertConflictContext(mapping.conflictContext()));
                return mapping;
            }

            @Override
            public KeyMapping.Category registerCategory(Identifier id) {
                KeyMapping.Category category = new KeyMapping.Category(id);
                categories.add(category);
                return category;
            }
        };
        handler.accept(builder);
        modEventBus.addListener((RegisterKeyMappingsEvent event) -> {
            builder.registerAll(event::register);
            for (KeyMapping.Category category : categories) {
                event.registerCategory(category);
            }
        });
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> builder.handleAll(Minecraft.getInstance()));
    }

    private static IKeyConflictContext convertConflictContext(KeyMappingBuilder.ConflictContext conflictContext) {
        return switch (conflictContext) {
            case UNIVERSAL -> KeyConflictContext.UNIVERSAL;
            case GUI -> KeyConflictContext.GUI;
            case IN_GAME -> KeyConflictContext.IN_GAME;
            case NEVER -> BingoConflictContext.NEVER;
        };
    }

    public static void registerDataReloadListeners(Consumer<DataReloadListenerRegistrar> handler) {
        NeoForge.EVENT_BUS.addListener((AddServerReloadListenersEvent event) -> handler.accept(
            (id, listener, dependencies) -> {
                event.addListener(id, listener.apply(event.getServerResources().getRegistryLookup()));
                for (final var dependency : dependencies) {
                    event.addDependency(dependency, id);
                }
            }
        ));
    }

    public static void registerDatapackRegistries(Consumer<DatapackRegistryRegistrar> handler) {
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

    public static void registerEvents() {
        Event.REGISTER_COMMANDS.setRegistrar(handler -> NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) ->
            handler.register(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection())
        ));
        Event.REGISTER_CONFIGURATION_TASKS.setRegistrar(handler -> modEventBus.addListener((RegisterConfigurationTasksEvent event) ->
            handler.accept(new ConfigurationTaskRegistrar() {
                @Override
                public boolean canSend(CustomPacketPayload.Type<?> packetType) {
                    return event.getListener().hasChannel(packetType);
                }

                @Override
                public void addTask(ConfigurationTask task) {
                    event.register(task);
                }
            })
        ));
        Event.PLAYER_JOIN.setRegistrar(handler -> NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                handler.accept(serverPlayer);
            }
        }));
        Event.PLAYER_QUIT.setRegistrar(handler -> NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                handler.accept(serverPlayer);
            }
        }));
        Event.COPY_PLAYER.setRegistrar(handler -> NeoForge.EVENT_BUS.addListener((PlayerEvent.Clone event) ->
            handler.accept((ServerPlayer)event.getOriginal(), (ServerPlayer)event.getEntity())
        ));
        Event.SERVER_STARTED.setRegistrar(handler -> NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) ->
            handler.accept(event.getServer())
        ));
        Event.SERVER_STOPPING.setRegistrar(handler -> NeoForge.EVENT_BUS.addListener((ServerStoppingEvent event) ->
            handler.accept(event.getServer())
        ));
        Event.SERVER_STOPPED.setRegistrar(handler -> NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) ->
            handler.accept(event.getServer())
        ));
        Event.RIGHT_CLICK_ITEM.setRegistrar(handler -> NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickItem event) ->
            handler.accept(event.getEntity(), event.getHand())
        ));
        Event.SERVER_EXPLOSION_START.setRegistrar(handler -> NeoForge.EVENT_BUS.addListener((ExplosionEvent.Start event) ->
            handler.accept((ServerLevel)event.getLevel(), event.getExplosion())
        ));
        Event.SERVER_TICK_END.setRegistrar(handler -> NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) ->
            handler.accept(event.getServer())
        ));
    }

    private enum BingoConflictContext implements IKeyConflictContext {
        NEVER {
            @Override
            public boolean isActive() {
                return false;
            }

            @Override
            public boolean conflicts(@NotNull IKeyConflictContext iKeyConflictContext) {
                return false;
            }
        }
    }
}
