package io.github.gaming32.bingo.conditions;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.gaming32.bingo.Bingo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import java.util.function.Supplier;

public final class BingoConditions {
    private BingoConditions() {
    }

    private static final Registrar<LootItemConditionType> REGISTRAR = Bingo.REGISTRAR_MANAGER.get(Registries.LOOT_CONDITION_TYPE);

    public static final RegistrySupplier<LootItemConditionType> BLOCK_PATTERN = register("block_pattern", BlockPatternCondition.Serializer::new);
    public static final RegistrySupplier<LootItemConditionType> ENDERMAN_HAS_ONLY_BEEN_DAMAGED_BY_ENDERMITE = register("enderman_has_only_been_damaged_by_endermite", EndermanHasOnlyBeenDamagedByEndermiteCondition.Serializer::new);

    public static void load() {
    }

    private static RegistrySupplier<LootItemConditionType> register(String registryName, Supplier<Serializer<? extends LootItemCondition>> serializer) {
        return REGISTRAR.register(new ResourceLocation(Bingo.MOD_ID, registryName), () -> new LootItemConditionType(serializer.get()));
    }
}
