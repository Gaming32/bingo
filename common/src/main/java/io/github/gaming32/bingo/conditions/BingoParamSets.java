package io.github.gaming32.bingo.conditions;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.Optional;

public final class BingoParamSets {
    private BingoParamSets() {
    }

    public static final LootContextParamSet TOOL = LootContextParamSets.register("bingo:tool", builder -> builder.required(LootContextParams.TOOL));

    public static void load() {
    }

    public static LootContext wrapTool(ServerPlayer player, ItemStack tool) {
        return new LootContext.Builder(
            new LootParams.Builder(player.serverLevel())
                .withParameter(LootContextParams.TOOL, tool)
                .create(TOOL)
        ).create(Optional.empty());
    }
}
