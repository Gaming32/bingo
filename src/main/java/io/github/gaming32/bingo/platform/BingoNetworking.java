package io.github.gaming32.bingo.platform;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.network.AbstractCustomPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
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
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class BingoNetworking {
    public static final int PROTOCOL_VERSION = 12;

    public static void onRegister(Consumer<Registrar> handler) {
        BingoPlatform.getModEventBus().addListener(RegisterPayloadHandlersEvent.class, event -> handler.accept(
            new Registrar(
                event.registrar(Bingo.MOD_ID)
                    .versioned(Integer.toString(BingoNetworking.PROTOCOL_VERSION))
                    .optional()
            )
        ));
    }

    public static void sendToServer(CustomPacketPayload payload) {
        final ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            throw new IllegalStateException("Not connected!");
        }
        connection.send(payload);
    }

    public static void sendTo(ServerPlayer player, CustomPacketPayload payload) {
        player.connection.send(payload);
    }

    public static void sendTo(Iterable<ServerPlayer> players, CustomPacketPayload payload) {
        for (final ServerPlayer player : players) {
            sendTo(player, payload);
        }
    }

    public static boolean canServerReceive(CustomPacketPayload.Type<?> type) {
        final ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return false;
        }
        return connection.hasChannel(type);
    }

    public static boolean canPlayerReceive(ServerPlayer player, CustomPacketPayload.Type<?> type) {
        return player.connection.hasChannel(type);
    }

    public static void finishTask(Context context, ConfigurationTask.Type type) {
        if (!(context.packetListener instanceof ServerConfigurationPacketListenerImpl packetListener)) {
            throw new IllegalStateException("finishTask can only be called during the configuration phase");
        }
        packetListener.finishCurrentTask(type);
    }

    private static Context convertContext(IPayloadContext neoforge) {
        return new Context(
            neoforge.protocol() == ConnectionProtocol.PLAY ? neoforge.player() : null,
            neoforge::reply, neoforge.listener(), neoforge.flow()
        );
    }

    public static final class Registrar {
        private final PayloadRegistrar inner;

        private Registrar(PayloadRegistrar inner) {
            this.inner = inner;
        }

        @SuppressWarnings("unchecked")
        public <P extends CustomPacketPayload> void register(
            ConnectionProtocol protocol,
            @Nullable PacketFlow flow,
            CustomPacketPayload.Type<P> type,
            StreamCodec<? super RegistryFriendlyByteBuf, P> codec,
            BiConsumer<P, Context> handler
        ) {
            final IPayloadHandler<P> neoHandler = (payload, context) -> handler.accept(payload, convertContext(context));
            switch (protocol) {
                case PLAY -> {
                    switch (flow) {
                        case null -> inner.playBidirectional(type, codec, neoHandler, neoHandler);
                        case CLIENTBOUND -> inner.playToClient(type, codec, neoHandler);
                        case SERVERBOUND -> inner.playToServer(type, codec, neoHandler);
                    }
                }
                case CONFIGURATION -> {
                    @SuppressWarnings("unchecked")
                    final var castedCodec = (StreamCodec<? super FriendlyByteBuf, P>) codec;
                    switch (flow) {
                        case null -> inner.configurationBidirectional(type, castedCodec, neoHandler, neoHandler);
                        case CLIENTBOUND -> inner.configurationToClient(type, castedCodec, neoHandler);
                        case SERVERBOUND -> inner.configurationToServer(type, castedCodec, neoHandler);
                    }
                }
                default -> throw new IllegalArgumentException("Cannot register for connection state: " + protocol);
            }
        }

        public <P extends AbstractCustomPayload> void register(
            @Nullable PacketFlow flow,
            CustomPacketPayload.Type<P> type,
            StreamCodec<? super RegistryFriendlyByteBuf, P> codec
        ) {
            register(ConnectionProtocol.PLAY, flow, type, codec, AbstractCustomPayload::handle);
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
