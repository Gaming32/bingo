package io.github.gaming32.bingo.conditions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class DistanceFromSpawnCondition implements LootItemCondition {
    private final DistancePredicate distance;

    public DistanceFromSpawnCondition(DistancePredicate distance) {
        this.distance = distance;
    }

    @NotNull
    @Override
    public LootItemConditionType getType() {
        return BingoConditions.DISTANCE_FROM_SPAWN.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        final Entity entity = lootContext.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (entity == null) {
            return false;
        }
        final Vec3 origin = lootContext.getParamOrNull(LootContextParams.ORIGIN);
        if (origin == null) {
            return false;
        }
        final GlobalPos spawnPoint = CompassItem.getSpawnPosition(entity.level());
        if (spawnPoint == null || spawnPoint.dimension() != entity.level().dimension()) { // Maybe some mod does something interesting?
            return false;
        }
        final Vec3 spawnVec = Vec3.atCenterOf(spawnPoint.pos());
        return distance.matches(
            origin.x, origin.y, origin.z,
            spawnVec.x, spawnVec.y, spawnVec.z
        );
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<DistanceFromSpawnCondition> {
        @Override
        public void serialize(JsonObject json, DistanceFromSpawnCondition value, JsonSerializationContext serializationContext) {
            json.add("distance", value.distance.serializeToJson());
        }

        @NotNull
        @Override
        public DistanceFromSpawnCondition deserialize(JsonObject json, JsonDeserializationContext serializationContext) {
            return new DistanceFromSpawnCondition(DistancePredicate.fromJson(json.get("distance")));
        }
    }
}
