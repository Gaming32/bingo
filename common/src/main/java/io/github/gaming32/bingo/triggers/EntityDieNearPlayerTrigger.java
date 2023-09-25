package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EntityDieNearPlayerTrigger extends SimpleCriterionTrigger<EntityDieNearPlayerTrigger.TriggerInstance> {
    @Override
    @NotNull
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        return new TriggerInstance(player, EntityPredicate.fromJson(json, "entity", context), DamagePredicate.fromJson(json.get("killing_blow")), DistancePredicate.fromJson(json.get("distance")));
    }

    public static Builder builder() {
        return new Builder();
    }

    public void trigger(LivingEntity entity, DamageSource source, float damageDealt, float damageTaken, boolean blocked) {
        List<Player> players = entity.level().getEntitiesOfClass(Player.class, entity.getBoundingBox().inflate(32));
        for (Player player : players) {
            if (player instanceof ServerPlayer serverPlayer) {
                trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer, entity, source, damageDealt, damageTaken, blocked));
            }
        }
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ContextAwarePredicate entity;
        private final DamagePredicate killingBlow;
        private final DistancePredicate distance;

        public TriggerInstance(ContextAwarePredicate player, ContextAwarePredicate entity, DamagePredicate killingBlow, DistancePredicate distance) {
            super(ID, player);
            this.entity = entity;
            this.killingBlow = killingBlow;
            this.distance = distance;
        }

        @Override
        @NotNull
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.add("entity", entity.toJson(context));
            json.add("killing_blow", killingBlow.serializeToJson());
            json.add("distance", distance.serializeToJson());
            return json;
        }

        public boolean matches(ServerPlayer player, LivingEntity entity, DamageSource source, float damageDealt, float damageTaken, boolean blocked) {
            if (!this.entity.matches(EntityPredicate.createContext(player, entity))) {
                return false;
            }
            if (!killingBlow.matches(player, source, damageDealt, damageTaken, blocked)) {
                return false;
            }
            if (!distance.matches(player.getX(), player.getY(), player.getZ(), entity.getX(), entity.getY(), entity.getZ())) {
                return false;
            }
            return true;
        }
    }

    public static final class Builder {
        private ContextAwarePredicate player = ContextAwarePredicate.ANY;
        private ContextAwarePredicate entity = ContextAwarePredicate.ANY;
        private DamagePredicate killingBlow = DamagePredicate.ANY;
        private DistancePredicate distance = DistancePredicate.ANY;

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = player;
            return this;
        }

        public Builder entity(ContextAwarePredicate entity) {
            this.entity = entity;
            return this;
        }

        public Builder entity(EntityPredicate entity) {
            return entity(EntityPredicate.wrap(entity));
        }

        public Builder killingBlow(DamagePredicate killingBlow) {
            this.killingBlow = killingBlow;
            return this;
        }

        public Builder distance(DistancePredicate distance) {
            this.distance = distance;
            return this;
        }

        public TriggerInstance build() {
            return new TriggerInstance(player, entity, killingBlow, distance);
        }
    }
}
