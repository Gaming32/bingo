package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ArrowPressTrigger extends SimpleCriterionTrigger<ArrowPressTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(AbstractArrow arrow, BlockPos pos) {
        if (!(arrow.getOwner() instanceof ServerPlayer player)) return;
        final LootContext arrowContext = EntityPredicate.createContext(player, arrow);
        trigger(player, instance -> instance.matches(arrowContext, pos));
    }

    public static Builder builder() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<ContextAwarePredicate> arrow,
        Optional<BlockPredicate> buttonOrPlate,
        Optional<LocationPredicate> location
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "arrow").forGetter(TriggerInstance::arrow),
                ExtraCodecs.strictOptionalField(BlockPredicate.CODEC, "button_or_plate").forGetter(TriggerInstance::buttonOrPlate),
                ExtraCodecs.strictOptionalField(LocationPredicate.CODEC, "location").forGetter(TriggerInstance::location)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(LootContext arrow, BlockPos pos) {
            if (this.arrow.isPresent() && !this.arrow.get().matches(arrow)) {
                return false;
            }
            if (this.buttonOrPlate.isPresent() && !this.buttonOrPlate.get().matches(arrow.getLevel(), pos)) {
                return false;
            }
            if (
                this.location.isPresent() &&
                    !this.location.get().matches(arrow.getLevel(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
            ) {
                return false;
            }
            return true;
        }

        @Override
        public void validate(CriterionValidator criterionValidator) {
            SimpleInstance.super.validate(criterionValidator);
            criterionValidator.validateEntity(arrow, ".arrow");
        }
    }

    public static final class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private Optional<ContextAwarePredicate> arrow = Optional.empty();
        private Optional<BlockPredicate> buttonOrPlate = Optional.empty();
        private Optional<LocationPredicate> location = Optional.empty();

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.ofNullable(player);
            return this;
        }

        public Builder arrow(ContextAwarePredicate arrow) {
            this.arrow = Optional.ofNullable(arrow);
            return this;
        }

        public Builder arrow(EntityPredicate arrow) {
            this.arrow = EntityPredicate.wrap(Optional.ofNullable(arrow));
            return this;
        }

        public Builder buttonOrPlate(BlockPredicate buttonOrPlate) {
            this.buttonOrPlate = Optional.ofNullable(buttonOrPlate);
            return this;
        }

        public Builder location(LocationPredicate location) {
            this.location = Optional.ofNullable(location);
            return this;
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.ARROW_PRESS.get().createCriterion(
                new TriggerInstance(player, arrow, buttonOrPlate, location)
            );
        }
    }
}
