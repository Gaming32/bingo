package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class ExperienceChangeTrigger extends SimpleCriterionTrigger<ExperienceChangeTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext context) {
        return new TriggerInstance(
            predicate,
            MinMaxBounds.Ints.fromJson(json.get("levels")),
            MinMaxBounds.Doubles.fromJson(json.get("progress")),
            MinMaxBounds.Ints.fromJson(json.get("total_experience"))
        );
    }

    public void trigger(ServerPlayer player) {
        trigger(player, i -> i.matches(player));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final MinMaxBounds.Ints levels;
        private final MinMaxBounds.Doubles progress;
        private final MinMaxBounds.Ints totalExperience;

        public TriggerInstance(
            ContextAwarePredicate player,
            MinMaxBounds.Ints levels,
            MinMaxBounds.Doubles progress,
            MinMaxBounds.Ints totalExperience
        ) {
            super(ID, player);
            this.levels = levels;
            this.progress = progress;
            this.totalExperience = totalExperience;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            final JsonObject result = super.serializeToJson(context);
            result.add("levels", levels.serializeToJson());
            result.add("progress", progress.serializeToJson());
            result.add("total_experience", totalExperience.serializeToJson());
            return result;
        }

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
        private ContextAwarePredicate player = ContextAwarePredicate.ANY;
        private MinMaxBounds.Ints levels = MinMaxBounds.Ints.ANY;
        private MinMaxBounds.Doubles progress = MinMaxBounds.Doubles.ANY;
        private MinMaxBounds.Ints totalExperience = MinMaxBounds.Ints.ANY;

        private Builder() {
        }

        private Builder player(ContextAwarePredicate player) {
            this.player = player;
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

        public TriggerInstance build() {
            return new TriggerInstance(player, levels, progress, totalExperience);
        }
    }
}
