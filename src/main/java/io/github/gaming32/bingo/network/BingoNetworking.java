package io.github.gaming32.bingo.network;

import io.github.gaming32.bingo.platform.BingoPlatform;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class BingoNetworking {
    public static final int PROTOCOL_VERSION = 12;

    public static void onRegister(Consumer<Registrar> handler) {
        handler.accept(new Registrar());
    }

    public static void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    public static void sendTo(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendTo(Iterable<ServerPlayer> players, CustomPacketPayload payload) {
        for (final ServerPlayer player : players) {
            sendTo(player, payload);
        }
    }

    public static boolean canServerReceive(CustomPacketPayload.Type<?> type) {
        return ClientPlayNetworking.canSend(type);
    }

    public static boolean canPlayerReceive(ServerPlayer player, CustomPacketPayload.Type<?> type) {
        return ServerPlayNetworking.canSend(player, type);
    }

    public static void finishTask(Context context, ConfigurationTask.Type type) {
        if (!(context.packetListener instanceof ServerConfigurationPacketListenerImpl packetListener)) {
            throw new IllegalStateException("finishTask can only be called during the configuration phase");
        }
        packetListener.completeTask(type);
    }

    public static final class Registrar {
        private Registrar() {
        }

        @SuppressWarnings("unchecked")
        public <P extends CustomPacketPayload> void register(
            ConnectionProtocol protocol,
            @Nullable PacketFlow flow,
            CustomPacketPayload.Type<P> type,
            StreamCodec<? super RegistryFriendlyByteBuf, P> codec,
            BiConsumer<P, Context> handler
        ) {
            if (flow == null || flow == PacketFlow.CLIENTBOUND) {
                switch (protocol) {
                    case PLAY -> PayloadTypeRegistry.clientboundPlay().register(type, codec);
                    case CONFIGURATION -> PayloadTypeRegistry.clientboundConfiguration().register(type, (StreamCodec<? super FriendlyByteBuf, P>) codec);
                    default -> throw new IllegalArgumentException("Cannot register for connection state: " + protocol);
                }
                if (BingoPlatform.isClient()) {
                    ClientReceiverRegistrar.register(protocol, type, handler);
                }
            }
            if (flow == null || flow == PacketFlow.SERVERBOUND) {
                switch (protocol) {
                    case PLAY -> {
                        PayloadTypeRegistry.serverboundPlay().register(type, codec);
                        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) ->
                            handler.accept(payload, new Context(
                                context.player(),
                                context.responseSender()::sendPacket,
                                context.player().connection,
                                PacketFlow.SERVERBOUND
                            ))
                        );
                    }
                    case CONFIGURATION -> {
                        PayloadTypeRegistry.serverboundConfiguration().register(type, (StreamCodec<? super FriendlyByteBuf, P>) codec);
                        ServerConfigurationNetworking.registerGlobalReceiver(type, (payload, context) ->
                            handler.accept(payload, new Context(
                                null,
                                context.responseSender()::sendPacket,
                                context.packetListener(),
                                PacketFlow.SERVERBOUND
                            ))
                        );
                    }
                    default -> throw new IllegalArgumentException("Cannot register for connection state: " + protocol);
                }
            }
        }

        public <P extends AbstractCustomPayload> void register(
            @Nullable PacketFlow flow,
            CustomPacketPayload.Type<P> type,
            StreamCodec<? super RegistryFriendlyByteBuf, P> codec
        ) {
            register(ConnectionProtocol.PLAY, flow, type, codec, AbstractCustomPayload::handle);
        }

        private static class ClientReceiverRegistrar {
            public static <P extends CustomPacketPayload> void register(
                ConnectionProtocol protocol, CustomPacketPayload.Type<P> type, BiConsumer<P, Context> handler
            ) {
                switch (protocol) {
                    case PLAY -> ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) ->
                        handler.accept(payload, new Context(
                            context.player(),
                            context.responseSender()::sendPacket,
                            context.player().connection,
                            PacketFlow.CLIENTBOUND
                        ))
                    );
                    case CONFIGURATION -> ClientConfigurationNetworking.registerGlobalReceiver(type, (payload, context) ->
                        handler.accept(payload, new Context(
                            null,
                            context.responseSender()::sendPacket,
                            context.packetListener(),
                            PacketFlow.CLIENTBOUND
                        ))
                    );
                    default -> throw new IllegalArgumentException("Cannot register for connection state: " + protocol);
                }

            }
        }
    }

    public static final class Context {
        @Nullable
        private final Player player;
        private final Consumer<CustomPacketPayload> reply;
        private final PacketListener packetListener;
        private final PacketFlow flow;

        public Context(@Nullable Player player, Consumer<CustomPacketPayload> reply, PacketListener packetListener, PacketFlow flow) {
            this.player = player;
            this.reply = reply;
            this.packetListener = packetListener;
            this.flow = flow;
        }

        @Nullable
        public Player player() {
            return player;
        }

        public void reply(CustomPacketPayload payload) {
            reply.accept(payload);
        }

        public PacketFlow flow() {
            return flow;
        }

        public Level level() {
            if (player == null) {
                return null;
            }
            return player.level();
        }

        public void disconnect(Component reason) {
            if (packetListener instanceof ServerCommonPacketListenerImpl serverListener) {
                serverListener.disconnect(reason);
            } else if (!BingoPlatform.isClient() || !ClientDisconnecter.disconnect(packetListener, reason)) {
                throw new IllegalStateException("Cannot disconnect with listener " + packetListener.getClass().getName());
            }
        }

        private static final class ClientDisconnecter {
            static boolean disconnect(PacketListener packetListener, Component reason) {
                if (packetListener instanceof ClientCommonPacketListenerImpl clientListener) {
                    clientListener.handleDisconnect(new ClientboundDisconnectPacket(reason));
                    return true;
                }

                return false;
            }
        }
    }
}
