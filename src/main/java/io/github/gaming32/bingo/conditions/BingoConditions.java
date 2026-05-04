package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.MapCodec;
import io.github.gaming32.bingo.platform.registry.DeferredRegister;
import io.github.gaming32.bingo.platform.registry.RegistryValue;
import io.github.gaming32.bingo.util.Identifiers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public final class BingoConditions {
    private BingoConditions() {
    }

    private static final DeferredRegister<MapCodec<? extends LootItemCondition>> REGISTER =
        DeferredRegister.create(BuiltInRegistries.LOOT_CONDITION_TYPE);

    public static final RegistryValue<MapCodec<BlockCondition>> BLOCK = register("block", BlockCondition.CODEC);
    public static final RegistryValue<MapCodec<BlockPatternCondition>> BLOCK_PATTERN = register("block_pattern", BlockPatternCondition.CODEC);
    public static final RegistryValue<MapCodec<DistanceFromSpawnCondition>> DISTANCE_FROM_SPAWN = register("distance_from_spawn", DistanceFromSpawnCondition.CODEC);
    public static final RegistryValue<MapCodec<FlammableCondition>> FLAMMABLE = register("flammable", FlammableCondition.CODEC);
    public static final RegistryValue<MapCodec<HasAnyEffectCondition>> HAS_ANY_EFFECT = register("has_any_effect", HasAnyEffectCondition.CODEC);
    public static final RegistryValue<MapCodec<HasOnlyBeenDamagedByCondition>> HAS_ONLY_BEEN_DAMAGED_BY = register("has_only_been_damaged_by", HasOnlyBeenDamagedByCondition.CODEC);
    public static final RegistryValue<MapCodec<InStructureCondition>> IN_STRUCTURE = register("in_structure", InStructureCondition.CODEC);
    public static final RegistryValue<MapCodec<OneByOneHoleCondition>> ONE_BY_ONE_HOLE = register("one_by_one_hole", OneByOneHoleCondition.CODEC);
    public static final RegistryValue<MapCodec<PassengersCondition>> PASSENGERS = register("passengers", PassengersCondition.CODEC);
    public static final RegistryValue<MapCodec<PillarCondition>> PILLAR = register("pillar", PillarCondition.CODEC);
    public static final RegistryValue<MapCodec<StairwayToHeavenCondition>> STAIRWAY_TO_HEAVEN = register("stairway_to_heaven", StairwayToHeavenCondition.CODEC);
    public static final RegistryValue<MapCodec<ToolDamageCondition>> TOOL_DAMAGE = register("tool_damage", ToolDamageCondition.CODEC);
    public static final RegistryValue<MapCodec<ToolIsEnchantedCondition>> TOOL_IS_ENCHANTED = register("tool_is_enchanted", ToolIsEnchantedCondition.CODEC);
    public static final RegistryValue<MapCodec<VillagerOwnershipCondition>> VILLAGER_OWNERSHIP = register("villager_ownership", VillagerOwnershipCondition.CODEC);
    public static final RegistryValue<MapCodec<WearingDifferentArmorCondition>> WEARING_DIFFERENT_ARMOR = register("wearing_different_armor", WearingDifferentArmorCondition.CODEC);

    public static void load() {
    }

    private static <T extends LootItemCondition> RegistryValue<MapCodec<T>> register(String registryName, MapCodec<T> codec) {
        return REGISTER.register(Identifiers.bingo(registryName), () -> codec);
    }
}
