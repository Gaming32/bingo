package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContextSource;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public class ChickenHatchTrigger extends SimpleCriterionTrigger<ChickenHatchTrigger.TriggerInstance> {
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
        Optional<Holder<LootItemCondition>> player,
        Optional<Holder<LootItemCondition>> projectile,
        MinMaxBounds.Ints numChickens
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                LootItemCondition.CODEC.optionalFieldOf("projectile").forGetter(TriggerInstance::projectile),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("num_chickens", MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::numChickens)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(LootContext projectileContext, int numChickens) {
            if (this.projectile.isPresent() && !this.projectile.get().value().test(projectileContext)) {
                return false;
            }
            return this.numChickens.matches(numChickens);
        }

        @Override
        public void validate(ValidationContextSource validator) {
            SimpleInstance.super.validate(validator);
            Validatable.validateHolder(validator.entityContext(), "projectile", projectile);
        }
    }

    public static class Builder {
        private Optional<Holder<LootItemCondition>> player = Optional.empty();
        private Optional<Holder<LootItemCondition>> projectile = Optional.empty();
        private MinMaxBounds.Ints numChickens = MinMaxBounds.Ints.ANY;

        private Builder() {
        }

        public Builder player(Holder<LootItemCondition> player) {
            this.player = Optional.of(player);
            return this;
        }

        public Builder projectile(EntityPredicate projectile) {
            return projectile(EntityPredicate.wrap(projectile));
        }

        public Builder projectile(Holder<LootItemCondition> projectile) {
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
