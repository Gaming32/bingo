package io.github.gaming32.bingo.neoforge;

import io.github.gaming32.bingo.platform.event.ConfigurationTaskRegistrar;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;

public record NeoForgeConfigurationTaskRegistrar(RegisterConfigurationTasksEvent event) implements ConfigurationTaskRegistrar {
    @Override
    public boolean canSend(CustomPacketPayload.Type<?> packetType) {
        return event.getListener().hasChannel(packetType);
    }

    @Override
    public void addTask(ConfigurationTask task) {
        event.register(task);
    }
}
