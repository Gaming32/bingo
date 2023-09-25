package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class DeathTrigger extends SimpleCriterionTrigger<DeathTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(player, DamageSourcePredicate.fromJson(json.get("source")));
    }

    public void trigger(ServerPlayer player, DamageSource source) {
        trigger(player, instance -> instance.matches(player, source));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<DamageSourcePredicate> source;

        public TriggerInstance(Optional<ContextAwarePredicate> player, Optional<DamageSourcePredicate> source) {
            super(player);
            this.source = source;
        }

        public static TriggerInstance death(DamageSourcePredicate source) {
            return new TriggerInstance(Optional.empty(), Optional.ofNullable(source));
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            source.ifPresent(p -> result.add("source", p.serializeToJson()));
            return result;
        }

        public boolean matches(ServerPlayer player, DamageSource source) {
            return this.source.isEmpty() || this.source.get().matches(player, source);
        }
    }
}
