package io.github.gaming32.bingo.neoforge;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.network.BingoNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
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
        modEventBus.addListener(RegisterPayloadHandlerEvent.class, event -> handler.accept(
            new RegistrarImpl(
                event.registrar(Bingo.MOD_ID)
                    .versioned(Integer.toString(BingoNetworking.PROTOCOL_VERSION))
                    .optional()
            )
        ));
    }

    @Override
    public void sendToServer(CustomPacketPayload packet) {
        final ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            throw new IllegalStateException("Not connected!");
        }
        connection.send(packet);
    }

    @Override
    public void sendTo(ServerPlayer player, CustomPacketPayload packet) {
        player.connection.send(packet);
    }

    @Override
    public boolean canServerReceive(CustomPacketPayload.Type<?> type) {
        final ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return false;
        }
        return connection.isConnected(type);
    }

    @Override
    public boolean canPlayerReceive(ServerPlayer player, CustomPacketPayload.Type<?> type) {
        return player.connection.isConnected(type);
    }

    private static Context convertContext(PlayPayloadContext neoforge) {
        return new Context(neoforge.player().orElse(null), neoforge.replyHandler()::send);
    }

    public static final class RegistrarImpl extends Registrar {
        private final IPayloadRegistrar inner;

        private RegistrarImpl(IPayloadRegistrar inner) {
            this.inner = inner;
        }

        @Override
        public <P extends CustomPacketPayload> void register(
            @Nullable PacketFlow flow,
            CustomPacketPayload.Type<P> type,
            StreamCodec<? super RegistryFriendlyByteBuf, P> codec,
            BiConsumer<P, Context> handler
        ) {
            final IPlayPayloadHandler<P> neoHandler = (payload, context) -> handler.accept(payload, convertContext(context));
            if (flow == null) {
                inner.play(type, codec, neoHandler);
            } else {
                inner.play(type, codec, builder -> {
                    switch (flow) {
                        case CLIENTBOUND -> builder.client(neoHandler);
                        case SERVERBOUND -> builder.server(neoHandler);
                    }
                });
            }
        }
    }
}
