package io.github.gaming32.bingo.conditions;

import io.github.gaming32.bingo.Bingo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public final class BingoConditions {
    private BingoConditions() {
    }

    public static final LootItemConditionType BLOCK_PATTERN = register("block_pattern", new BlockPatternCondition.Serializer());

    public static void load() {
    }

    private static LootItemConditionType register(String registryName, Serializer<? extends LootItemCondition> serializer) {
        return Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, new ResourceLocation(Bingo.MOD_ID, registryName), new LootItemConditionType(serializer));
    }
}
