package io.github.gaming32.bingo.network.messages.configuration;

import io.github.gaming32.bingo.network.AbstractCustomPayload;
import io.github.gaming32.bingo.network.BingoNetworking;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ProtocolVersionPayload(int protocolVersion) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ProtocolVersionPayload> TYPE = AbstractCustomPayload.type("version");
    public static final StreamCodec<ByteBuf, ProtocolVersionPayload> CODEC = ByteBufCodecs.VAR_INT.map(ProtocolVersionPayload::new, ProtocolVersionPayload::protocolVersion);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleClientbound(BingoNetworking.Context context) {
        context.reply().accept(new ProtocolVersionPayload(BingoNetworking.PROTOCOL_VERSION));
    }

    public void handleServerbound(BingoNetworking.Context context) {
        BingoNetworking.instance().finishTask(context, ProtocolVersionConfigurationTask.TYPE);

        if (protocolVersion != BingoNetworking.PROTOCOL_VERSION) {
            if (protocolVersion < BingoNetworking.PROTOCOL_VERSION) {
                context.disconnect(Component.translatable("bingo.outdated_client", protocolVersion, BingoNetworking.PROTOCOL_VERSION));
            } else {
                context.disconnect(Component.translatable("bingo.outdated_server", BingoNetworking.PROTOCOL_VERSION, protocolVersion));
            }
        }
    }
}
