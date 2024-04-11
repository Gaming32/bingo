package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class DestroyVehicleTrigger extends SimpleCriterionTrigger<DestroyVehicleTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, VehicleEntity vehicle, DamageSource destroyingBlow) {
        final LootContext vehicleContext = EntityPredicate.createContext(player, vehicle);
        trigger(player, instance -> instance.matches(player, vehicleContext, destroyingBlow));
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<ContextAwarePredicate> vehicle,
        Optional<DamageSourcePredicate> destroyingBlow
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "vehicle").forGetter(TriggerInstance::vehicle),
                ExtraCodecs.strictOptionalField(DamageSourcePredicate.CODEC, "destroying_blow").forGetter(TriggerInstance::destroyingBlow)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(ServerPlayer player, LootContext vehicle, DamageSource destroyingBlow) {
            if (this.vehicle.isPresent() && !this.vehicle.get().matches(vehicle)) {
                return false;
            }
            if (this.destroyingBlow.isPresent() && !this.destroyingBlow.get().matches(player, destroyingBlow)) {
                return false;
            }
            return true;
        }

        @Override
        public void validate(CriterionValidator criterionValidator) {
            SimpleInstance.super.validate(criterionValidator);
            criterionValidator.validateEntity(vehicle, ".vehicle");
        }
    }
}
