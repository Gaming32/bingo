package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.predicates.LocationPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContextSource;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class LeashedEntityTrigger extends SimpleCriterionTrigger<LeashedEntityTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Entity mob, Entity knot, BlockPos fencePos, ItemStack tool) {
        final ServerLevel level = player.level();
        final LootContext mobContext = EntityPredicate.createContext(player, mob);
        final LootContext knotContext = EntityPredicate.createContext(player, knot);
        final LootParams fenceParams = new LootParams.Builder(level)
            .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(fencePos))
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
        Optional<Holder<LootItemCondition>> player,
        Optional<Holder<LootItemCondition>> mob,
        Optional<Holder<LootItemCondition>> knot,
        Optional<Holder<LootItemCondition>> fence
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                LootItemCondition.CODEC.optionalFieldOf("mob").forGetter(TriggerInstance::mob),
                LootItemCondition.CODEC.optionalFieldOf("knot").forGetter(TriggerInstance::knot),
                LootItemCondition.CODEC.optionalFieldOf("fence").forGetter(TriggerInstance::fence)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(LootContext mob, LootContext knot, LootContext fence) {
            if (this.mob.isPresent() && !this.mob.get().value().test(mob)) {
                return false;
            }
            if (this.knot.isPresent() && !this.knot.get().value().test(knot)) {
                return false;
            }
            if (this.fence.isPresent() && !this.fence.get().value().test(fence)) {
                return false;
            }
            return true;
        }

        @Override
        public void validate(ValidationContextSource validator) {
            SimpleInstance.super.validate(validator);
            Validatable.validateHolder(validator.entityContext(), "mob", mob);
            Validatable.validateHolder(validator.entityContext(), "knot", knot);
            fence.ifPresent(p -> Validatable.validateHolder(validator.context(LootContextParamSets.ADVANCEMENT_LOCATION), "fence", p));
        }
    }

    public static final class Builder {
        private Optional<Holder<LootItemCondition>> player = Optional.empty();
        private Optional<Holder<LootItemCondition>> mob = Optional.empty();
        private Optional<Holder<LootItemCondition>> knot = Optional.empty();
        private Optional<Holder<LootItemCondition>> fence = Optional.empty();

        private Builder() {
        }

        public Builder player(Holder<LootItemCondition> player) {
            this.player = Optional.ofNullable(player);
            return this;
        }

        @SafeVarargs
        public final Builder player(Holder<LootItemCondition>... conditions) {
            return player(Holder.direct(AllOfCondition.allOf(HolderSet.direct(conditions))));
        }

        public Builder mob(Holder<LootItemCondition> mob) {
            this.mob = Optional.ofNullable(mob);
            return this;
        }

        public Builder mob(EntityPredicate mob) {
            return mob(EntityPredicate.wrap(mob));
        }

        public Builder knot(Holder<LootItemCondition> knot) {
            this.knot = Optional.ofNullable(knot);
            return this;
        }

        public Builder knot(EntityPredicate knot) {
            return knot(EntityPredicate.wrap(knot));
        }

        public Builder fence(Holder<LootItemCondition> fence) {
            this.fence = Optional.ofNullable(fence);
            return this;
        }

        @SafeVarargs
        public final Builder fence(Holder<LootItemCondition>... conditions) {
            return fence(Holder.direct(AllOfCondition.allOf(HolderSet.direct(conditions))));
        }

        public Builder fence(LocationPredicate fence) {
            return fence(Holder.direct(new LocationCheck(Optional.ofNullable(fence), BlockPos.ZERO)));
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.LEASHED_ENTITY.get().createCriterion(new TriggerInstance(player, mob, knot, fence));
        }
    }
}
