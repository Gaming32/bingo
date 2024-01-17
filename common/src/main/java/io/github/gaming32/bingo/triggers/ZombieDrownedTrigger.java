package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.monster.Zombie;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ZombieDrownedTrigger extends SimpleCriterionTrigger<ZombieDrownedTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Zombie zombie) {
        trigger(player, instance -> instance.matches(player, zombie));
    }

    public static Builder builder() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<ContextAwarePredicate> zombie
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "zombie").forGetter(TriggerInstance::zombie)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(ServerPlayer player, Zombie zombie) {
            return this.zombie.isEmpty() || this.zombie.get().matches(EntityPredicate.createContext(player, zombie));
        }

        @Override
        public void validate(CriterionValidator criterionValidator) {
            SimpleInstance.super.validate(criterionValidator);
            criterionValidator.validateEntity(zombie, ".zombie");
        }
    }

    public static final class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private Optional<ContextAwarePredicate> zombie = Optional.empty();

        private Builder() {
        }

        public Builder player(EntityPredicate player) {
            return player(EntityPredicate.wrap(player));
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.of(player);
            return this;
        }

        public Builder zombie(EntityPredicate zombie) {
            return zombie(EntityPredicate.wrap(zombie));
        }

        public Builder zombie(ContextAwarePredicate zombie) {
            this.zombie = Optional.of(zombie);
            return this;
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.ZOMBIE_DROWNED.get().createCriterion(
                new TriggerInstance(player, zombie)
            );
        }
    }
}
