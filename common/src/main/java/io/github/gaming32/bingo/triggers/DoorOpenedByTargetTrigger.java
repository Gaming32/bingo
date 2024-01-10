package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class DoorOpenedByTargetTrigger extends SimpleCriterionTrigger<DoorOpenedByTargetTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(
        ServerPlayer player,
        Projectile projectile,
        BlockPos targetBlock,
        int targetBlockPower,
        BlockPos poweredBlock
    ) {
        final LootContext projectileContext = EntityPredicate.createContext(player, projectile);
        trigger(player, instance -> instance.matches(projectileContext, targetBlock, targetBlockPower, poweredBlock));
    }

    public static Builder builder() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<ContextAwarePredicate> projectile,
        Optional<LocationPredicate> targetBlock,
        MinMaxBounds.Ints targetBlockPower,
        Optional<LocationPredicate> door
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "projectile").forGetter(TriggerInstance::projectile),
                ExtraCodecs.strictOptionalField(LocationPredicate.CODEC, "target_block").forGetter(TriggerInstance::targetBlock),
                ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "target_block_power", MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::targetBlockPower),
                ExtraCodecs.strictOptionalField(LocationPredicate.CODEC, "door").forGetter(TriggerInstance::door)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(
            LootContext projectile,
            BlockPos targetBlock,
            int targetBlockPower,
            BlockPos door
        ) {
            if (this.projectile.isPresent() && !this.projectile.get().matches(projectile)) {
                return false;
            }
            if (this.targetBlock.isPresent() && !this.targetBlock.get().matches(projectile.getLevel(), targetBlock.getX(), targetBlock.getY(), targetBlock.getZ())) {
                return false;
            }
            if (!this.targetBlockPower.matches(targetBlockPower)) {
                return false;
            }
            if (this.door.isPresent() && !this.door.get().matches(projectile.getLevel(), door.getX(), door.getY(), door.getZ())) {
                return false;
            }
            return true;
        }
    }

    public static class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private Optional<ContextAwarePredicate> projectile = Optional.empty();
        private Optional<LocationPredicate> targetBlock = Optional.empty();
        private MinMaxBounds.Ints targetBlockPower = MinMaxBounds.Ints.ANY;
        private Optional<LocationPredicate> door = Optional.empty();

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.of(player);
            return this;
        }

        public Builder projectile(ContextAwarePredicate projectile) {
            this.projectile = Optional.of(projectile);
            return this;
        }

        public Builder projectile(EntityPredicate.Builder projectile) {
            return projectile(EntityPredicate.wrap(projectile));
        }

        public Builder targetBlock(LocationPredicate targetBlock) {
            this.targetBlock = Optional.of(targetBlock);
            return this;
        }

        public Builder targetBlockPower(MinMaxBounds.Ints targetBlockPower) {
            this.targetBlockPower = targetBlockPower;
            return this;
        }

        public Builder door(LocationPredicate door) {
            this.door = Optional.of(door);
            return this;
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.DOOR_OPENED_BY_TARGET.get().createCriterion(
                new TriggerInstance(player, projectile, targetBlock, targetBlockPower, door)
            );
        }
    }
}
