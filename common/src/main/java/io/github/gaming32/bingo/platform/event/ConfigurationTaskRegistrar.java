package io.github.gaming32.bingo.platform.event;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ConfigurationTask;

public interface ConfigurationTaskRegistrar {
    boolean canSend(CustomPacketPayload.Type<?> packetType);

    void addTask(ConfigurationTask task);
}
