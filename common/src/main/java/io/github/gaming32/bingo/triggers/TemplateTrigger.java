package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class TemplateTrigger extends SimpleCriterionTrigger<TemplateTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(
            player
        );
    }

    public void trigger(ServerPlayer player) {
        trigger(player, instance -> instance.matches());
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        public TriggerInstance(Optional<ContextAwarePredicate> player) {
            super(player);
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            return result;
        }

        public boolean matches() {
            return true;
        }
    }
}
