package io.github.gaming32.bingo.conditions;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public final class BingoContextKeySets {
    private BingoContextKeySets() {
    }

    public static final ContextKeySet TOOL_ONLY = new ContextKeySet.Builder()
        .required(LootContextParams.TOOL)
        .build();

    public static final ContextKeySet ADVANCEMENT_OPTIONAL_ENTITY = new ContextKeySet.Builder()
        .optional(LootContextParams.THIS_ENTITY)
        .required(LootContextParams.ORIGIN)
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

    // Like EntityPredicate.createContext, but allows a null entity
    public static LootContext wrapOptionalEntity(ServerPlayer player, @Nullable Entity entity) {
        return new LootContext.Builder(
            new LootParams.Builder(player.level())
                .withOptionalParameter(LootContextParams.THIS_ENTITY, entity)
                .withParameter(LootContextParams.ORIGIN, player.position())
                .create(ADVANCEMENT_OPTIONAL_ENTITY)
        ).create(Optional.empty());
    }
}
