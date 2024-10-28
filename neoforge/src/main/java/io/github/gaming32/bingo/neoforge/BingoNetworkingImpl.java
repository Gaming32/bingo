package io.github.gaming32.bingo.neoforge;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.network.BingoNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class BingoNetworkingImpl extends BingoNetworking {
    private final IEventBus modEventBus;

    BingoNetworkingImpl(IEventBus modEventBus) {
        this.modEventBus = modEventBus;
    }

    @Override
    public void onRegister(Consumer<Registrar> handler) {
        modEventBus.addListener(RegisterPayloadHandlersEvent.class, event -> handler.accept(
            new RegistrarImpl(
                event.registrar(Bingo.MOD_ID)
                    .versioned(Integer.toString(BingoNetworking.PROTOCOL_VERSION))
                    .optional()
            )
        ));
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        final ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            throw new IllegalStateException("Not connected!");
        }
        connection.send(payload);
    }

    @Override
    public void sendTo(ServerPlayer player, CustomPacketPayload payload) {
        player.connection.send(payload);
    }

    @Override
    public boolean canServerReceive(CustomPacketPayload.Type<?> type) {
        final ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return false;
        }
        return connection.hasChannel(type);
    }

    @Override
    public boolean canPlayerReceive(ServerPlayer player, CustomPacketPayload.Type<?> type) {
        return player.connection.hasChannel(type);
    }

    @Override
    protected void finishTask(ServerConfigurationPacketListenerImpl packetListener, ConfigurationTask.Type type) {
        packetListener.finishCurrentTask(type);
    }

    private static Context convertContext(IPayloadContext neoforge) {
        return new Context(
            neoforge.protocol() == ConnectionProtocol.PLAY ? neoforge.player() : null,
            neoforge::reply, neoforge.listener(), neoforge.flow()
        );
    }

    public static final class RegistrarImpl extends Registrar {
        private final PayloadRegistrar inner;

        private RegistrarImpl(PayloadRegistrar inner) {
            this.inner = inner;
        }

        @Override
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
                        case null -> inner.playBidirectional(type, codec, neoHandler);
                        case CLIENTBOUND -> inner.playToClient(type, codec, neoHandler);
                        case SERVERBOUND -> inner.playToServer(type, codec, neoHandler);
                    }
                }
                case CONFIGURATION -> {
                    @SuppressWarnings("unchecked")
                    final var castedCodec = (StreamCodec<? super FriendlyByteBuf, P>) codec;
                    switch (flow) {
                        case null -> inner.configurationBidirectional(type, castedCodec, neoHandler);
                        case CLIENTBOUND -> inner.configurationToClient(type, castedCodec, neoHandler);
                        case SERVERBOUND -> inner.configurationToServer(type, castedCodec, neoHandler);
                    }
                }
                default -> throw new IllegalArgumentException("Cannot register for connection state: " + protocol);
            }
        }
    }
}
