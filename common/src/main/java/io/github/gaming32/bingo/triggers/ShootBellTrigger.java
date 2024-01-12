package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ShootBellTrigger extends SimpleCriterionTrigger<ShootBellTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, BlockPos pos, Projectile projectile) {
        final ServerLevel level = player.serverLevel();
        final BlockState state = level.getBlockState(pos);
        final LootParams locationParams = new LootParams.Builder(level)
            .withParameter(LootContextParams.ORIGIN, pos.getCenter())
            .withParameter(LootContextParams.THIS_ENTITY, player)
            .withParameter(LootContextParams.BLOCK_STATE, state)
            .withParameter(LootContextParams.TOOL, player.getMainHandItem())
            .create(LootContextParamSets.ADVANCEMENT_LOCATION);
        final LootContext projectileContext = EntityPredicate.createContext(player, projectile);
        final LootContext bell = new LootContext.Builder(locationParams).create(Optional.empty());
        trigger(player, triggerInstance -> triggerInstance.matches(player, pos, bell, projectileContext));
    }

    public static Builder builder() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<ContextAwarePredicate> bell,
        Optional<ContextAwarePredicate> projectile,
        Optional<DistancePredicate> distance
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.strictOptionalField(ContextAwarePredicate.CODEC, "bell").forGetter(TriggerInstance::bell),
                ExtraCodecs.strictOptionalField(ContextAwarePredicate.CODEC, "projectile").forGetter(TriggerInstance::projectile),
                ExtraCodecs.strictOptionalField(DistancePredicate.CODEC, "distance").forGetter(TriggerInstance::distance)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(ServerPlayer player, BlockPos bellLocation, LootContext bell, LootContext projectile) {
            if (this.bell.isPresent() && !this.bell.get().matches(bell)) {
                return false;
            }
            if (this.projectile.isPresent() && !this.projectile.get().matches(projectile)) {
                return false;
            }
            if (this.distance.isPresent() && !this.distance.get().matches(player.getX(), player.getY(), player.getZ(), bellLocation.getX() + 0.5, bellLocation.getY() + 0.5, bellLocation.getZ() + 0.5)) {
                return false;
            }
            return true;
        }

        @Override
        public void validate(CriterionValidator criterionValidator) {
            SimpleInstance.super.validate(criterionValidator);
            bell.ifPresent(p -> criterionValidator.validate(p, LootContextParamSets.ADVANCEMENT_LOCATION, ".location"));
            projectile.ifPresent(p -> criterionValidator.validate(p, LootContextParamSets.ADVANCEMENT_ENTITY, ".projectile"));
        }
    }

    public static final class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private Optional<ContextAwarePredicate> bell = Optional.empty();
        private Optional<ContextAwarePredicate> projectile = Optional.empty();
        private Optional<DistancePredicate> distance = Optional.empty();

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.of(player);
            return this;
        }

        public Builder bell(ContextAwarePredicate bell) {
            this.bell = Optional.of(bell);
            return this;
        }

        public Builder bell(LootItemCondition... conditions) {
            return bell(ContextAwarePredicate.create(conditions));
        }

        public Builder bell(LocationPredicate bell) {
            return bell(new LocationCheck(Optional.ofNullable(bell), BlockPos.ZERO));
        }

        public Builder projectile(ContextAwarePredicate projectile) {
            this.projectile = Optional.of(projectile);
            return this;
        }

        public Builder projectile(EntityPredicate projectile) {
            return projectile(EntityPredicate.wrap(projectile));
        }

        public Builder distance(DistancePredicate distance) {
            this.distance = Optional.of(distance);
            return this;
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.SHOOT_BELL.get().createCriterion(new TriggerInstance(player, bell, projectile, distance));
        }
    }
}
