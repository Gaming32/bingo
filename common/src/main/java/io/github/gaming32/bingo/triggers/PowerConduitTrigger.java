package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class PowerConduitTrigger extends SimpleCriterionTrigger<PowerConduitTrigger.TriggerInstance> {
    public static final ResourceLocation ID = new ResourceLocation("bingo:power_conduit");

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext context) {
        return new TriggerInstance(predicate, MinMaxBounds.Ints.fromJson(json.get("level")));
    }

    public void trigger(ServerPlayer player, int level) {
        trigger(player, instance -> instance.matches(level));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final MinMaxBounds.Ints level;

        public TriggerInstance(ContextAwarePredicate predicate, MinMaxBounds.Ints level) {
            super(ID, predicate);
            this.level = level;
        }

        public static TriggerInstance powerConduit() {
            return new TriggerInstance(ContextAwarePredicate.ANY, MinMaxBounds.Ints.ANY);
        }

        public static TriggerInstance powerConduit(MinMaxBounds.Ints level) {
            return new TriggerInstance(ContextAwarePredicate.ANY, level);
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            final JsonObject result = super.serializeToJson(context);
            result.add("level", level.serializeToJson());
            return result;
        }

        public boolean matches(int level) {
            return this.level.matches(level);
        }
    }
}
