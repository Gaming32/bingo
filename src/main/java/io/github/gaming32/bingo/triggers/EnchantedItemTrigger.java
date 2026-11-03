package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public class EnchantedItemTrigger extends SimpleCriterionTrigger<EnchantedItemTrigger.TriggerInstance> {
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
        Optional<Holder<LootItemCondition>> player,
        MinMaxBounds.Ints levelsSpent,
        MinMaxBounds.Ints requiredLevels
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("levels_spent", MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::levelsSpent),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("required_levels", MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::requiredLevels)
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
        private Optional<Holder<LootItemCondition>> player = Optional.empty();
        private MinMaxBounds.Ints levelsSpent = MinMaxBounds.Ints.ANY;
        private MinMaxBounds.Ints requiredLevels = MinMaxBounds.Ints.ANY;

        private Builder() {
        }

        public Builder player(Holder<LootItemCondition> player) {
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
            return BingoTriggers.ENCHANTED_ITEM.get().createCriterion(
                new TriggerInstance(player, levelsSpent, requiredLevels)
            );
        }
    }
}
