package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PowerConduitTrigger extends SimpleCriterionTrigger<PowerConduitTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(player, MinMaxBounds.Ints.fromJson(json.get("level")));
    }

    public void trigger(ServerPlayer player, int level) {
        trigger(player, instance -> instance.matches(level));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final MinMaxBounds.Ints level;

        public TriggerInstance(Optional<ContextAwarePredicate> predicate, MinMaxBounds.Ints level) {
            super(predicate);
            this.level = level;
        }

        public static TriggerInstance powerConduit() {
            return new TriggerInstance(Optional.empty(), MinMaxBounds.Ints.ANY);
        }

        public static TriggerInstance powerConduit(MinMaxBounds.Ints level) {
            return new TriggerInstance(Optional.empty(), level);
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            result.add("level", level.serializeToJson());
            return result;
        }

        public boolean matches(int level) {
            return this.level.matches(level);
        }
    }
}
