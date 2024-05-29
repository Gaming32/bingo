package io.github.gaming32.bingo.fabric;

import io.github.gaming32.bingo.network.BingoNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
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

    public static final class RegistrarImpl extends Registrar {
        private static final List<PayloadTypeAndHandler<?>> queuedClientHandlers = new ArrayList<>();

        private RegistrarImpl() {
        }

        @Override
        public <P extends CustomPacketPayload> void register(
            @Nullable PacketFlow flow,
            CustomPacketPayload.Type<P> type,
            StreamCodec<? super RegistryFriendlyByteBuf, P> codec,
            BiConsumer<P, Context> handler
        ) {
            if (flow == null || flow == PacketFlow.CLIENTBOUND) {
                PayloadTypeRegistry.playS2C().register(type, codec);
                queuedClientHandlers.add(new PayloadTypeAndHandler<>(type, handler));
            }
            if (flow == null || flow == PacketFlow.SERVERBOUND) {
                PayloadTypeRegistry.playC2S().register(type, codec);
                ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.accept(
                    payload, new Context(context.player(), context.responseSender()::sendPacket)
                ));
            }
        }

        public static void initClientHandlers(Consumer<PayloadTypeAndHandler<?>> initializer) {
            queuedClientHandlers.forEach(initializer);
            queuedClientHandlers.clear();
        }

        public record PayloadTypeAndHandler<P extends CustomPacketPayload>(
            CustomPacketPayload.Type<P> type, BiConsumer<P, Context> handler
        ) {
        }
    }
}
