package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ChickenHatchTrigger extends SimpleCriterionTrigger<ChickenHatchTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ThrownEgg projectile, int numChickens) {
        LootContext projectileContext = EntityPredicate.createContext(player, projectile);
        trigger(player, instance -> instance.matches(projectileContext, numChickens));
    }

    public static Builder builder() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<ContextAwarePredicate> projectile,
        MinMaxBounds.Ints numChickens
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "projectile").forGetter(TriggerInstance::projectile),
                ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "num_chickens", MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::numChickens)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(LootContext projectileContext, int numChickens) {
            if (this.projectile.isPresent() && !this.projectile.get().matches(projectileContext)) {
                return false;
            }
            return this.numChickens.matches(numChickens);
        }

        @Override
        public void validate(CriterionValidator criterionValidator) {
            SimpleInstance.super.validate(criterionValidator);
            criterionValidator.validateEntity(projectile, ".projectile");
        }
    }

    public static class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private Optional<ContextAwarePredicate> projectile = Optional.empty();
        private MinMaxBounds.Ints numChickens = MinMaxBounds.Ints.ANY;

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.of(player);
            return this;
        }

        public Builder projectile(EntityPredicate projectile) {
            return projectile(EntityPredicate.wrap(projectile));
        }

        public Builder projectile(ContextAwarePredicate projectile) {
            this.projectile = Optional.of(projectile);
            return this;
        }

        public Builder numChickens(MinMaxBounds.Ints numChickens) {
            this.numChickens = numChickens;
            return this;
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.CHICKEN_HATCH.get().createCriterion(new TriggerInstance(player, projectile, numChickens));
        }
    }
}
