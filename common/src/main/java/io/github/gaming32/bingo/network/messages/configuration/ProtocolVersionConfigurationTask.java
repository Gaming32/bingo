package io.github.gaming32.bingo.network.messages.configuration;

import io.github.gaming32.bingo.network.BingoNetworking;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.network.ConfigurationTask;

import java.util.function.Consumer;

public enum ProtocolVersionConfigurationTask implements ConfigurationTask {
    INSTANCE;

    public static final Type TYPE = new Type("bingo:protocol_version");

    @Override
    public void start(Consumer<Packet<?>> task) {
        task.accept(new ClientboundCustomPayloadPacket(new ProtocolVersionPayload(BingoNetworking.PROTOCOL_VERSION)));
    }

    @Override
    public Type type() {
        return TYPE;
    }
}
