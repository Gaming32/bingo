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

public class EnchantedItemTrigger extends SimpleCriterionTrigger<EnchantedItemTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, int levelsSpent, int levelsRequired) {
        trigger(player, instance -> instance.matches(levelsSpent, levelsRequired));
    }

    public static Builder builder() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        MinMaxBounds.Ints levelsSpent,
        MinMaxBounds.Ints requiredLevels
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "levels_spent", MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::levelsSpent),
                ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "required_levels", MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::requiredLevels)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(int levelsSpent, int levelsRequired) {
            if (!this.levelsSpent.matches(levelsSpent)) {
                return false;
            }
            if (!this.requiredLevels.matches(levelsRequired)) {
                return false;
            }
            return true;
        }
    }

    public static final class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private MinMaxBounds.Ints levelsSpent = MinMaxBounds.Ints.ANY;
        private MinMaxBounds.Ints requiredLevels = MinMaxBounds.Ints.ANY;

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.ofNullable(player);
            return this;
        }

        public Builder levelsSpent(MinMaxBounds.Ints levelsSpent) {
            this.levelsSpent = levelsSpent;
            return this;
        }

        public Builder requiredLevels(MinMaxBounds.Ints requiredLevels) {
            this.requiredLevels = requiredLevels;
            return this;
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.ENCHANTED_ITEM.createCriterion(
                new TriggerInstance(player, levelsSpent, requiredLevels)
            );
        }
    }
}
