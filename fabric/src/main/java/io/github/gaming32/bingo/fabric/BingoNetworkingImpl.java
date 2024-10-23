package io.github.gaming32.bingo.fabric;

import io.github.gaming32.bingo.network.BingoNetworking;
import io.github.gaming32.bingo.platform.BingoPlatform;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BingoNetworkingImpl extends BingoNetworking {
    BingoNetworkingImpl() {
    }

    @Override
    public void onRegister(Consumer<Registrar> handler) {
        handler.accept(new RegistrarImpl());
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    @Override
    public void sendTo(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public boolean canServerReceive(CustomPacketPayload.Type<?> type) {
        return ClientPlayNetworking.canSend(type);
    }

    @Override
    public boolean canPlayerReceive(ServerPlayer player, CustomPacketPayload.Type<?> type) {
        return ServerPlayNetworking.canSend(player, type);
    }

    @Override
    protected void finishTask(ServerConfigurationPacketListenerImpl packetListener, ConfigurationTask.Type type) {
        packetListener.completeTask(type);
    }

    public static final class RegistrarImpl extends Registrar {
        private RegistrarImpl() {
        }

        @SuppressWarnings("unchecked")
        @Override
        public <P extends CustomPacketPayload> void register(
            ConnectionProtocol protocol,
            @Nullable PacketFlow flow,
            CustomPacketPayload.Type<P> type,
            StreamCodec<? super RegistryFriendlyByteBuf, P> codec,
            BiConsumer<P, Context> handler
        ) {
            if (flow == null || flow == PacketFlow.CLIENTBOUND) {
                switch (protocol) {
                    case PLAY -> PayloadTypeRegistry.playS2C().register(type, codec);
                    case CONFIGURATION -> PayloadTypeRegistry.configurationS2C().register(type, (StreamCodec<? super FriendlyByteBuf, P>) codec);
                    default -> throw new IllegalArgumentException("Cannot register for connection state: " + protocol);
                }
                if (BingoPlatform.platform.isClient()) {
                    ClientReceiverRegistrar.register(protocol, type, handler);
                }
            }
            if (flow == null || flow == PacketFlow.SERVERBOUND) {
                switch (protocol) {
                    case PLAY -> {
                        PayloadTypeRegistry.playC2S().register(type, codec);
                        ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) ->
                            handler.accept(payload, new Context(context.player(), context.responseSender()::sendPacket, context.player().connection))
                        );
                    }
                    case CONFIGURATION -> {
                        PayloadTypeRegistry.configurationC2S().register(type, (StreamCodec<? super FriendlyByteBuf, P>) codec);
                        ServerConfigurationNetworking.registerGlobalReceiver(type, (payload, context) ->
                            handler.accept(payload, new Context(null, context.responseSender()::sendPacket, context.networkHandler()))
                        );
                    }
                    default -> throw new IllegalArgumentException("Cannot register for connection state: " + protocol);
                }
            }
        }

        private static class ClientReceiverRegistrar {
            public static <P extends CustomPacketPayload> void register(
                ConnectionProtocol protocol, CustomPacketPayload.Type<P> type, BiConsumer<P, Context> handler
            ) {
                switch (protocol) {
                    case PLAY -> ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) ->
                        handler.accept(payload, new Context(context.player(), context.responseSender()::sendPacket, context.player().connection))
                    );
                    case CONFIGURATION -> ClientConfigurationNetworking.registerGlobalReceiver(type, (payload, context) ->
                        handler.accept(payload, new Context(null, context.responseSender()::sendPacket, context.networkHandler()))
                    );
                    default -> throw new IllegalArgumentException("Cannot register for connection state: " + protocol);
                }

            }
        }
    }
}
