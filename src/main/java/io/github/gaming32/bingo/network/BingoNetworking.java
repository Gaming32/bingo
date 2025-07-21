package io.github.gaming32.bingo.network;

import io.github.gaming32.bingo.platform.BingoPlatform;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.ConnectionProtocol;
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

public abstract class BingoNetworking {
    public static final int PROTOCOL_VERSION = 10;

    public static BingoNetworking instance() {
        return BingoPlatform.platform.getNetworking();
    }

    public abstract void onRegister(Consumer<Registrar> handler);

    public abstract void sendToServer(CustomPacketPayload payload);

    public abstract void sendTo(ServerPlayer player, CustomPacketPayload payload);

    public void sendTo(Iterable<ServerPlayer> players, CustomPacketPayload payload) {
        for (final ServerPlayer player : players) {
            sendTo(player, payload);
        }
    }

    public abstract boolean canServerReceive(CustomPacketPayload.Type<?> type);

    public abstract boolean canPlayerReceive(ServerPlayer player, CustomPacketPayload.Type<?> type);

    protected abstract void finishTask(ServerConfigurationPacketListenerImpl packetListener, ConfigurationTask.Type type);

    public final void finishTask(Context context, ConfigurationTask.Type type) {
        if (!(context.packetListener instanceof ServerConfigurationPacketListenerImpl packetListener)) {
            throw new IllegalStateException("finishTask can only be called during the configuration phase");
        }
        finishTask(packetListener, type);
    }

    public abstract static class Registrar {
        public abstract <P extends CustomPacketPayload> void register(
            ConnectionProtocol protocol,
            @Nullable PacketFlow flow,
            CustomPacketPayload.Type<P> type,
            StreamCodec<? super RegistryFriendlyByteBuf, P> codec,
            BiConsumer<P, Context> handler
        );

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
            } else if (!BingoPlatform.platform.isClient() || !ClientDisconnecter.disconnect(packetListener, reason)) {
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
