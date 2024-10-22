package io.github.gaming32.bingo.network;

import io.github.gaming32.bingo.platform.BingoPlatform;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
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

    public abstract static class Registrar {
        public abstract <P extends CustomPacketPayload> void register(
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
            register(flow, type, codec, AbstractCustomPayload::handle);
        }
    }

    public record Context(@Nullable Player player, Consumer<CustomPacketPayload> reply) {
        public Level level() {
            if (player == null) {
                return null;
            }
            return player.level();
        }
    }
}
