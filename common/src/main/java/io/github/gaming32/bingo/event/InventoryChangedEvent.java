package io.github.gaming32.bingo.event;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;

public interface InventoryChangedEvent {
    Event<InventoryChangedEvent> EVENT = EventFactory.createLoop();

    void inventoryChanged(ServerPlayer player, Inventory inventory);
}
