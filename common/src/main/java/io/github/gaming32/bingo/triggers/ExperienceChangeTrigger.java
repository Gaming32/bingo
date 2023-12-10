package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ExperienceChangeTrigger extends SimpleCriterionTrigger<ExperienceChangeTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player) {
        trigger(player, i -> i.matches(player));
    }

    public static Builder builder() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        MinMaxBounds.Ints levels,
        MinMaxBounds.Doubles progress,
        MinMaxBounds.Ints totalExperience
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "levels", MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::levels),
                ExtraCodecs.strictOptionalField(MinMaxBounds.Doubles.CODEC, "progress", MinMaxBounds.Doubles.ANY).forGetter(TriggerInstance::progress),
                ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "total_experience", MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::totalExperience)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(ServerPlayer player) {
            if (!levels.matches(player.experienceLevel)) {
                return false;
            }
            if (!progress.matches(player.experienceProgress)) {
                return false;
            }
            if (!totalExperience.matches(player.totalExperience)) {
                return false;
            }
            return true;
        }
    }

    public static final class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private MinMaxBounds.Ints levels = MinMaxBounds.Ints.ANY;
        private MinMaxBounds.Doubles progress = MinMaxBounds.Doubles.ANY;
        private MinMaxBounds.Ints totalExperience = MinMaxBounds.Ints.ANY;

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.ofNullable(player);
            return this;
        }

        public Builder levels(MinMaxBounds.Ints levels) {
            this.levels = levels;
            return this;
        }

        public Builder progress(MinMaxBounds.Doubles progress) {
            this.progress = progress;
            return this;
        }

        public Builder totalExperience(MinMaxBounds.Ints totalExperience) {
            this.totalExperience = totalExperience;
            return this;
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.EXPERIENCE_CHANGED.createCriterion(
                new TriggerInstance(player, levels, progress, totalExperience)
            );
        }
    }
}
