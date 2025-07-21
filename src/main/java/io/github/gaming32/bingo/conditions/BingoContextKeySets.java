package io.github.gaming32.bingo.conditions;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.Optional;

public final class BingoContextKeySets {
    private BingoContextKeySets() {
    }

    public static final ContextKeySet TOOL_ONLY = new ContextKeySet.Builder()
        .required(LootContextParams.TOOL)
        .build();

    public static void load() {
    }

    public static LootContext wrapTool(ServerPlayer player, ItemStack tool) {
        return new LootContext.Builder(
            new LootParams.Builder(player.level())
                .withParameter(LootContextParams.TOOL, tool)
                .create(TOOL_ONLY)
        ).create(Optional.empty());
    }
}
