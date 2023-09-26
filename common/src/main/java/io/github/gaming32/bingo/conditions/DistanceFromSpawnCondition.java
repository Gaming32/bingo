package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.core.GlobalPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public record DistanceFromSpawnCondition(Optional<DistancePredicate> distance) implements LootItemCondition {
    public static final Codec<DistanceFromSpawnCondition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ExtraCodecs.strictOptionalField(DistancePredicate.CODEC, "distance").forGetter(DistanceFromSpawnCondition::distance)
        ).apply(instance, DistanceFromSpawnCondition::new)
    );

    @NotNull
    @Override
    public LootItemConditionType getType() {
        return BingoConditions.DISTANCE_FROM_SPAWN.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        final Entity entity = lootContext.getParam(LootContextParams.THIS_ENTITY);
        final Vec3 origin = lootContext.getParam(LootContextParams.ORIGIN);
        final GlobalPos spawnPoint = CompassItem.getSpawnPosition(entity.level());
        if (spawnPoint == null || spawnPoint.dimension() != entity.level().dimension()) { // Maybe some mod does something interesting?
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
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.THIS_ENTITY, LootContextParams.ORIGIN);
    }
}
