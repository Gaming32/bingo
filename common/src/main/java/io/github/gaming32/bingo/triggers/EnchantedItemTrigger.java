package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EnchantedItemTrigger extends SimpleCriterionTrigger<EnchantedItemTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(
            player,
            MinMaxBounds.Ints.fromJson(json.get("levels_spent")),
            MinMaxBounds.Ints.fromJson(json.get("required_levels"))
        );
    }

    public void trigger(ServerPlayer player, int levelsSpent, int levelsRequired) {
        trigger(player, instance -> instance.matches(levelsSpent, levelsRequired));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final MinMaxBounds.Ints levelsSpent;
        private final MinMaxBounds.Ints requiredLevels;

        public TriggerInstance(Optional<ContextAwarePredicate> player, MinMaxBounds.Ints levelsSpent, MinMaxBounds.Ints requiredLevels) {
            super(player);
            this.levelsSpent = levelsSpent;
            this.requiredLevels = requiredLevels;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            result.add("levels_spent", levelsSpent.serializeToJson());
            result.add("required_levels", requiredLevels.serializeToJson());
            return result;
        }

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
