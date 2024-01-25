package io.github.gaming32.bingo.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class BingoNetworking {
    public static final int PROTOCOL_VERSION = 9;

    private static BingoNetworking instance;

    public static BingoNetworking instance() {
        return instance;
    }

    public static void init(BingoNetworking impl) {
        if (instance != null) {
            throw new IllegalStateException("Cannot override BingoNetworking impl");
        }
        instance = impl;
    }

    public abstract void onRegister(Consumer<Registrar> handler);

    public abstract void sendToServer(CustomPacketPayload packet);

    public abstract void sendTo(ServerPlayer player, CustomPacketPayload packet);

    public void sendTo(Iterable<ServerPlayer> players, CustomPacketPayload packet) {
        for (final ServerPlayer player : players) {
            sendTo(player, packet);
        }
    }

    public abstract boolean canServerReceive(ResourceLocation id);

    public abstract boolean canPlayerReceive(ServerPlayer player, ResourceLocation id);

    public abstract static class Registrar {
        public abstract <P extends CustomPacketPayload> void register(
            @Nullable PacketFlow flow, ResourceLocation id, FriendlyByteBuf.Reader<P> reader, BiConsumer<P, Context> handler
        );

        public <P extends AbstractCustomPayload> void register(@Nullable PacketFlow flow, ResourceLocation id, FriendlyByteBuf.Reader<P> reader) {
            register(flow, id, reader, AbstractCustomPayload::handle);
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
