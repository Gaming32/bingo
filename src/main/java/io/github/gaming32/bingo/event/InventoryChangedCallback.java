package io.github.gaming32.bingo.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public interface InventoryChangedCallback {
    List<InventoryChangedCallback> HANDLERS = new ArrayList<>();

    void inventoryChanged(ServerPlayer player, Inventory inventory);
}
