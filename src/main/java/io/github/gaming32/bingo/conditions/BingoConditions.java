package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.MapCodec;
import io.github.gaming32.bingo.platform.BingoPlatform;
import io.github.gaming32.bingo.platform.registry.DeferredRegister;
import io.github.gaming32.bingo.platform.registry.RegistryValue;
import io.github.gaming32.bingo.util.ResourceLocations;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public final class BingoConditions {
    private BingoConditions() {
    }

    private static final DeferredRegister<LootItemConditionType> REGISTER =
        BingoPlatform.platform.createDeferredRegister(BuiltInRegistries.LOOT_CONDITION_TYPE);

    public static final RegistryValue<LootItemConditionType> BLOCK = register("block", BlockCondition.CODEC);
    public static final RegistryValue<LootItemConditionType> BLOCK_PATTERN = register("block_pattern", BlockPatternCondition.CODEC);
    public static final RegistryValue<LootItemConditionType> DISTANCE_FROM_SPAWN = register("distance_from_spawn", DistanceFromSpawnCondition.CODEC);
    public static final RegistryValue<LootItemConditionType> FLAMMABLE = register("flammable", FlammableCondition.CODEC);
    public static final RegistryValue<LootItemConditionType> HAS_ANY_EFFECT = register("has_any_effect", HasAnyEffectCondition.CODEC);
    public static final RegistryValue<LootItemConditionType> HAS_ONLY_BEEN_DAMAGED_BY = register("has_only_been_damaged_by", HasOnlyBeenDamagedByCondition.CODEC);
    public static final RegistryValue<LootItemConditionType> IN_STRUCTURE = register("in_structure", InStructureCondition.CODEC);
    public static final RegistryValue<LootItemConditionType> ONE_BY_ONE_HOLE = register("one_by_one_hole", OneByOneHoleCondition.CODEC);
    public static final RegistryValue<LootItemConditionType> PASSENGERS = register("passengers", PassengersCondition.CODEC);
    public static final RegistryValue<LootItemConditionType> PILLAR = register("pillar", PillarCondition.CODEC);
    public static final RegistryValue<LootItemConditionType> STAIRWAY_TO_HEAVEN = register("stairway_to_heaven", StairwayToHeavenCondition.CODEC);
    public static final RegistryValue<LootItemConditionType> TOOL_DAMAGE = register("tool_damage", ToolDamageCondition.CODEC);
    public static final RegistryValue<LootItemConditionType> TOOL_IS_ENCHANTED = register("tool_is_enchanted", ToolIsEnchantedCondition.CODEC);
    public static final RegistryValue<LootItemConditionType> VILLAGER_OWNERSHIP = register("villager_ownership", VillagerOwnershipCondition.CODEC);
    public static final RegistryValue<LootItemConditionType> WEARING_DIFFERENT_ARMOR = register("wearing_different_armor", WearingDifferentArmorCondition.CODEC);

    public static void load() {
    }

    private static RegistryValue<LootItemConditionType> register(String registryName, MapCodec<? extends LootItemCondition> codec) {
        return REGISTER.register(ResourceLocations.bingo(registryName), () -> new LootItemConditionType(codec));
    }
}
