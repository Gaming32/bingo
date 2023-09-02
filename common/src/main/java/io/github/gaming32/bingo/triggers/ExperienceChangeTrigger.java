package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class ExperienceChangeTrigger extends SimpleCriterionTrigger<ExperienceChangeTrigger.TriggerInstance> {
    public static final ResourceLocation ID = new ResourceLocation("bingo:experience_changed");

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

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

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final MinMaxBounds.Ints levels;
        private final MinMaxBounds.Doubles progress;
        private final MinMaxBounds.Ints totalExperience;

        public TriggerInstance(
            ContextAwarePredicate predicate,
            MinMaxBounds.Ints levels,
            MinMaxBounds.Doubles progress,
            MinMaxBounds.Ints totalExperience
        ) {
            super(ID, predicate);
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
}
