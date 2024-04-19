package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PulledByLeashTrigger extends SimpleCriterionTrigger<PulledByLeashTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, PathfinderMob mob, @Nullable LeashFenceKnotEntity knot, Vec3 force) {
        final LootContext mobContext = EntityPredicate.createContext(player, mob);
        final Optional<LootContext> knotContext = Optional.ofNullable(knot)
            .map(e -> EntityPredicate.createContext(player, e));
        trigger(player, instance -> instance.matches(mobContext, knotContext, force));
    }

    public static Builder builder() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<ContextAwarePredicate> mob,
        Optional<ContextAwarePredicate> knot,
        Optional<Boolean> knotRequired,
        Optional<DistancePredicate> force
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("mob").forGetter(TriggerInstance::mob),
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("knot").forGetter(TriggerInstance::knot),
                Codec.BOOL.optionalFieldOf("knot_required").forGetter(TriggerInstance::knotRequired),
                DistancePredicate.CODEC.optionalFieldOf("force").forGetter(TriggerInstance::force)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(LootContext mob, Optional<LootContext> leash, Vec3 force) {
            if (this.mob.isPresent() && !this.mob.get().matches(mob)) {
                return false;
            }
            if (leash.isPresent()) {
                if (knotRequired.isPresent() && !knotRequired.get()) {
                    return false;
                }
                if (this.knot.isPresent() && !this.knot.get().matches(leash.get())) {
                    return false;
                }
            } else if (knotRequired.isPresent() && knotRequired.get()) {
                return false;
            }
            if (this.force.isPresent() && !this.force.get().matches(0, 0, 0, force.x, force.y, force.z)) {
                return false;
            }
            return true;
        }

        @Override
        public void validate(CriterionValidator criterionValidator) {
            SimpleInstance.super.validate(criterionValidator);
            criterionValidator.validateEntity(mob, ".mob");
            criterionValidator.validateEntity(knot, ".knot");
        }
    }

    public static final class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private Optional<ContextAwarePredicate> mob = Optional.empty();
        private Optional<ContextAwarePredicate> knot = Optional.empty();
        private Optional<Boolean> knotRequired = Optional.empty();
        private Optional<DistancePredicate> force = Optional.empty();

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.of(player);
            return this;
        }

        public Builder player(EntityPredicate.Builder player) {
            return player(EntityPredicate.wrap(player));
        }

        public Builder mob(ContextAwarePredicate mob) {
            this.mob = Optional.of(mob);
            return this;
        }

        public Builder mob(EntityPredicate.Builder mob) {
            return mob(EntityPredicate.wrap(mob));
        }

        public Builder knot(ContextAwarePredicate knot) {
            this.knot = Optional.of(knot);
            return this;
        }

        public Builder knot(EntityPredicate.Builder knot) {
            return knot(EntityPredicate.wrap(knot));
        }

        public Builder knotRequired(boolean knotRequired) {
            this.knotRequired = Optional.of(knotRequired);
            return this;
        }

        public Builder force(DistancePredicate force) {
            this.force = Optional.of(force);
            return this;
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.PULLED_BY_LEASH.get().createCriterion(
                new TriggerInstance(player, mob, knot, knotRequired, force)
            );
        }
    }
}
