package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class LeashedEntityTrigger extends SimpleCriterionTrigger<LeashedEntityTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Entity mob, Entity knot, BlockPos fencePos, ItemStack tool) {
        final ServerLevel level = player.serverLevel();
        final LootContext mobContext = EntityPredicate.createContext(player, mob);
        final LootContext knotContext = EntityPredicate.createContext(player, knot);
        final LootParams fenceParams = new LootParams.Builder(level)
            .withParameter(LootContextParams.ORIGIN, fencePos.getCenter())
            .withParameter(LootContextParams.THIS_ENTITY, player)
            .withParameter(LootContextParams.BLOCK_STATE, level.getBlockState(fencePos))
            .withParameter(LootContextParams.TOOL, tool)
            .create(LootContextParamSets.ADVANCEMENT_LOCATION);
        final LootContext fenceContext = new LootContext.Builder(fenceParams).create(Optional.empty());
        trigger(player, instance -> instance.matches(mobContext, knotContext, fenceContext));
    }

    public static Builder builder() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<ContextAwarePredicate> mob,
        Optional<ContextAwarePredicate> knot,
        Optional<ContextAwarePredicate> fence
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("mob").forGetter(TriggerInstance::mob),
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("knot").forGetter(TriggerInstance::knot),
                ContextAwarePredicate.CODEC.optionalFieldOf("fence").forGetter(TriggerInstance::fence)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(LootContext mob, LootContext knot, LootContext fence) {
            if (this.mob.isPresent() && !this.mob.get().matches(mob)) {
                return false;
            }
            if (this.knot.isPresent() && !this.knot.get().matches(knot)) {
                return false;
            }
            if (this.fence.isPresent() && !this.fence.get().matches(fence)) {
                return false;
            }
            return true;
        }

        @Override
        public void validate(CriterionValidator criterionValidator) {
            SimpleInstance.super.validate(criterionValidator);
            criterionValidator.validateEntity(mob, ".mob");
            criterionValidator.validateEntity(knot, ".knot");
            fence.ifPresent(p -> criterionValidator.validate(p, LootContextParamSets.ADVANCEMENT_LOCATION, ".fence"));
        }
    }

    public static final class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private Optional<ContextAwarePredicate> mob = Optional.empty();
        private Optional<ContextAwarePredicate> knot = Optional.empty();
        private Optional<ContextAwarePredicate> fence = Optional.empty();

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.ofNullable(player);
            return this;
        }

        public Builder player(LootItemCondition... conditions) {
            return player(ContextAwarePredicate.create(conditions));
        }

        public Builder mob(ContextAwarePredicate mob) {
            this.mob = Optional.ofNullable(mob);
            return this;
        }

        public Builder mob(EntityPredicate mob) {
            return mob(EntityPredicate.wrap(mob));
        }

        public Builder knot(ContextAwarePredicate knot) {
            this.knot = Optional.ofNullable(knot);
            return this;
        }

        public Builder knot(EntityPredicate knot) {
            return knot(EntityPredicate.wrap(knot));
        }

        public Builder fence(ContextAwarePredicate fence) {
            this.fence = Optional.ofNullable(fence);
            return this;
        }

        public Builder fence(LootItemCondition... conditions) {
            return fence(ContextAwarePredicate.create(conditions));
        }

        public Builder fence(LocationPredicate fence) {
            return fence(new LocationCheck(Optional.ofNullable(fence), BlockPos.ZERO));
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.LEASHED_ENTITY.get().createCriterion(new TriggerInstance(player, mob, knot, fence));
        }
    }
}
