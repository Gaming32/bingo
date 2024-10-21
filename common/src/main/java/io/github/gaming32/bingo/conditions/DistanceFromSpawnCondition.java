package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.core.GlobalPos;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public record DistanceFromSpawnCondition(Optional<DistancePredicate> distance) implements LootItemCondition {
    public static final MapCodec<DistanceFromSpawnCondition> CODEC = DistancePredicate.CODEC
        .optionalFieldOf("distance")
        .xmap(DistanceFromSpawnCondition::new, DistanceFromSpawnCondition::distance);

    @NotNull
    @Override
    public LootItemConditionType getType() {
        return BingoConditions.DISTANCE_FROM_SPAWN.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        final Vec3 origin = lootContext.getParameter(LootContextParams.ORIGIN);
        final GlobalPos spawnPoint = CompassItem.getSpawnPosition(lootContext.getLevel());
        if (spawnPoint == null || spawnPoint.dimension() != lootContext.getLevel().dimension()) { // Maybe some mod does something interesting?
            return false;
        }
        final Vec3 spawnVec = Vec3.atCenterOf(spawnPoint.pos());
        return distance.isEmpty() || distance.get().matches(
            origin.x, origin.y, origin.z,
            spawnVec.x, spawnVec.y, spawnVec.z
        );
    }

    @NotNull
    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.ORIGIN);
    }
}
