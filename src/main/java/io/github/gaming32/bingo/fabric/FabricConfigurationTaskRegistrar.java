package io.github.gaming32.bingo.fabric;

import io.github.gaming32.bingo.platform.event.ConfigurationTaskRegistrar;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

public record FabricConfigurationTaskRegistrar(ServerConfigurationPacketListenerImpl packetListener) implements ConfigurationTaskRegistrar {
    @Override
    public boolean canSend(CustomPacketPayload.Type<?> packetType) {
        return ServerConfigurationNetworking.canSend(packetListener, packetType);
    }

    @Override
    public void addTask(ConfigurationTask task) {
        packetListener.addTask(task);
    }
}
