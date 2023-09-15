package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;

public class DeathTrigger extends SimpleCriterionTrigger<DeathTrigger.TriggerInstance> {
    public static final ResourceLocation ID = new ResourceLocation("bingo:death");

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        return new TriggerInstance(player, DamageSourcePredicate.fromJson(json.get("source")));
    }

    public void trigger(ServerPlayer player, DamageSource source) {
        trigger(player, instance -> instance.matches(player, source));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final DamageSourcePredicate source;

        public TriggerInstance(ContextAwarePredicate player, DamageSourcePredicate source) {
            super(ID, player);
            this.source = source;
        }

        public static TriggerInstance death(DamageSourcePredicate source) {
            return new TriggerInstance(ContextAwarePredicate.ANY, source);
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            final JsonObject result = super.serializeToJson(context);
            result.add("source", source.serializeToJson());
            return result;
        }

        public boolean matches(ServerPlayer player, DamageSource source) {
            return this.source.matches(player, source);
        }
    }
}
