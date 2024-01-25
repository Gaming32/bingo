package io.github.gaming32.bingo.fabric;

import io.github.gaming32.bingo.network.BingoNetworking;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

// This will be rewritten in 1.20.5, as Fabric's networking API will be much closer to Neo's.
// https://hackmd.io/@KpoDyi5oQ2K9LWpmVPJnAg/HJ5vjHAta
public class BingoNetworkingImpl extends BingoNetworking {
    BingoNetworkingImpl() {
    }

    @Override
    public void onRegister(Consumer<Registrar> handler) {
        handler.accept(new RegistrarImpl());
    }

    @Override
    public void sendToServer(CustomPacketPayload packet) {
        ClientPlayNetworking.send(packet.id(), toBuffer(packet));
    }

    @Override
    public void sendTo(ServerPlayer player, CustomPacketPayload packet) {
        ServerPlayNetworking.send(player, packet.id(), toBuffer(packet));
    }

    @Override
    public boolean canServerReceive(ResourceLocation id) {
        return ClientPlayNetworking.canSend(id);
    }

    @Override
    public boolean canPlayerReceive(ServerPlayer player, ResourceLocation id) {
        return ServerPlayNetworking.canSend(player, id);
    }

    private static FriendlyByteBuf toBuffer(CustomPacketPayload packet) {
        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.write(buf);
        return buf;
    }

    public static final class RegistrarImpl extends Registrar {
        private RegistrarImpl() {
        }

        @Override
        public <P extends CustomPacketPayload> void register(
            @Nullable PacketFlow flow, ResourceLocation id, FriendlyByteBuf.Reader<P> reader, BiConsumer<P, Context> handler
        ) {
            if (flow == null || flow == PacketFlow.CLIENTBOUND) {
                ClientPlayNetworking.registerGlobalReceiver(id, (client, listener, buf, responseSender) -> {
                    final P packet = reader.apply(buf);
                    client.execute(() -> handler.accept(packet, new Context(client.player, responseSender::sendPacket)));
                });
            }
            if (flow == null || flow == PacketFlow.SERVERBOUND) {
                ServerPlayNetworking.registerGlobalReceiver(id, (server, player, listener, buf, responseSender) -> {
                    final P packet = reader.apply(buf);
                    server.execute(() -> handler.accept(packet, new Context(player, responseSender::sendPacket)));
                });
            }
        }
    }
}
