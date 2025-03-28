package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.DamagePredicate;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class EntityDieNearPlayerTrigger extends SimpleCriterionTrigger<EntityDieNearPlayerTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
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

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<ContextAwarePredicate> entity,
        Optional<DamagePredicate> killingBlow,
        Optional<DistancePredicate> distance
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(TriggerInstance::entity),
                DamagePredicate.CODEC.optionalFieldOf("killing_blow").forGetter(TriggerInstance::killingBlow),
                DistancePredicate.CODEC.optionalFieldOf("distance").forGetter(TriggerInstance::distance)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(ServerPlayer player, LivingEntity entity, DamageSource source, float damageDealt, float damageTaken, boolean blocked) {
            if (this.entity.isPresent() && !this.entity.get().matches(EntityPredicate.createContext(player, entity))) {
                return false;
            }
            if (this.killingBlow.isPresent() && !killingBlow.get().matches(player, source, damageDealt, damageTaken, blocked)) {
                return false;
            }
            if (distance.isPresent() && !distance.get().matches(player.getX(), player.getY(), player.getZ(), entity.getX(), entity.getY(), entity.getZ())) {
                return false;
            }
            return true;
        }

        @Override
        public void validate(CriterionValidator criterionValidator) {
            SimpleInstance.super.validate(criterionValidator);
            criterionValidator.validateEntity(entity, ".entity");
        }
    }

    public static final class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private Optional<ContextAwarePredicate> entity = Optional.empty();
        private Optional<DamagePredicate> killingBlow = Optional.empty();
        private Optional<DistancePredicate> distance = Optional.empty();

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.ofNullable(player);
            return this;
        }

        public Builder entity(ContextAwarePredicate entity) {
            this.entity = Optional.ofNullable(entity);
            return this;
        }

        public Builder entity(EntityPredicate entity) {
            return entity(EntityPredicate.wrap(entity));
        }

        public Builder killingBlow(DamagePredicate killingBlow) {
            this.killingBlow = Optional.ofNullable(killingBlow);
            return this;
        }

        public Builder distance(DistancePredicate distance) {
            this.distance = Optional.ofNullable(distance);
            return this;
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.ENTITY_DIE_NEAR_PLAYER.get().createCriterion(
                new TriggerInstance(player, entity, killingBlow, distance)
            );
        }
    }
}
