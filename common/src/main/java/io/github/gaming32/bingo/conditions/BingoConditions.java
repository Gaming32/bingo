package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.gaming32.bingo.Bingo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.function.Supplier;

public final class BingoConditions {
    private BingoConditions() {
    }

    private static final Registrar<LootItemConditionType> REGISTRAR = Bingo.REGISTRAR_MANAGER.get(Registries.LOOT_CONDITION_TYPE);

    public static final RegistrySupplier<LootItemConditionType> BLOCK_PATTERN = register("block_pattern", () -> BlockPatternCondition.CODEC);
    public static final RegistrySupplier<LootItemConditionType> DISTANCE_FROM_SPAWN = register("distance_from_spawn", () -> DistanceFromSpawnCondition.CODEC);
    public static final RegistrySupplier<LootItemConditionType> FLAMMABLE = register("flammable", () -> FlammableCondition.CODEC);
    public static final RegistrySupplier<LootItemConditionType> HAS_ANY_EFFECT = register("has_any_effect", () -> HasAnyEffectCondition.CODEC);
    public static final RegistrySupplier<LootItemConditionType> HAS_ONLY_BEEN_DAMAGED_BY = register("has_only_been_damaged_by", () -> HasOnlyBeenDamagedByCondition.CODEC);
    public static final RegistrySupplier<LootItemConditionType> IN_STRUCTURE = register("in_structure", () -> InStructureCondition.CODEC);
    public static final RegistrySupplier<LootItemConditionType> ONE_BY_ONE_HOLE = register("one_by_one_hole", () -> OneByOneHoleCondition.CODEC);
    public static final RegistrySupplier<LootItemConditionType> PASSENGERS = register("passengers", () -> PassengersCondition.CODEC);
    public static final RegistrySupplier<LootItemConditionType> PILLAR = register("pillar", () -> PillarCondition.CODEC);
    public static final RegistrySupplier<LootItemConditionType> STAIRWAY_TO_HEAVEN = register("stairway_to_heaven", () -> StairwayToHeavenCondition.CODEC);
    public static final RegistrySupplier<LootItemConditionType> TOOL_DAMAGE = register("tool_damage", () -> ToolDamageCondition.CODEC);
    public static final RegistrySupplier<LootItemConditionType> TOOL_IS_ENCHANTED = register("tool_is_enchanted", () -> ToolIsEnchantedCondition.CODEC);
    public static final RegistrySupplier<LootItemConditionType> VILLAGER_OWNERSHIP = register("villager_ownership", () -> VillagerOwnershipCondition.CODEC);
    public static final RegistrySupplier<LootItemConditionType> WEARING_DIFFERENT_ARMOR = register("wearing_different_armor", () -> WearingDifferentArmorCondition.CODEC);

    public static void load() {
    }

    private static RegistrySupplier<LootItemConditionType> register(String registryName, Supplier<Codec<? extends LootItemCondition>> codec) {
        return REGISTRAR.register(new ResourceLocation(Bingo.MOD_ID, registryName), () -> new LootItemConditionType(codec.get()));
    }
}
