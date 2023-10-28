package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class DestroyVehicleTrigger extends SimpleCriterionTrigger<DestroyVehicleTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(
            player,
            EntityPredicate.fromJson(json, "vehicle", context),
            DamageSourcePredicate.fromJson(json.get("destroying_blow"))
        );
    }

    // TODO: Use VehicleEntity
    public void trigger(ServerPlayer player, Entity vehicle, DamageSource destroyingBlow) {
        final LootContext vehicleContext = EntityPredicate.createContext(player, vehicle);
        trigger(player, instance -> instance.matches(player, vehicleContext, destroyingBlow));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ContextAwarePredicate> vehicle;
        private final Optional<DamageSourcePredicate> destroyingBlow;

        public TriggerInstance(
            Optional<ContextAwarePredicate> player,
            Optional<ContextAwarePredicate> vehicle,
            Optional<DamageSourcePredicate> destroyingBlow
        ) {
            super(player);
            this.vehicle = vehicle;
            this.destroyingBlow = destroyingBlow;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            vehicle.ifPresent(p -> result.add("vehicle", p.toJson()));
            destroyingBlow.ifPresent(p -> result.add("destroying_blow", p.serializeToJson()));
            return result;
        }

        public boolean matches(ServerPlayer player, LootContext vehicle, DamageSource destroyingBlow) {
            if (this.vehicle.isPresent() && !this.vehicle.get().matches(vehicle)) {
                return false;
            }
            if (this.destroyingBlow.isPresent() && !this.destroyingBlow.get().matches(player, destroyingBlow)) {
                return false;
            }
            return true;
        }
    }
}
