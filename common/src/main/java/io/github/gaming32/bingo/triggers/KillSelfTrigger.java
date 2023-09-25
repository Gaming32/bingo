package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;

public class KillSelfTrigger extends SimpleCriterionTrigger<KillSelfTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        return new TriggerInstance(player, DamageSourcePredicate.fromJson(json.get("killing_blow")));
    }

    public void trigger(ServerPlayer player, DamageSource killingBlow) {
        trigger(player, instance -> instance.matches(player, killingBlow));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final DamageSourcePredicate killingBlow;

        public TriggerInstance(ContextAwarePredicate player, DamageSourcePredicate killingBlow) {
            super(ID, player);
            this.killingBlow = killingBlow;
        }

        public static TriggerInstance killSelf() {
            return new TriggerInstance(ContextAwarePredicate.ANY, DamageSourcePredicate.ANY);
        }

        public static TriggerInstance killSelf(DamageSourcePredicate killingBlow) {
            return new TriggerInstance(ContextAwarePredicate.ANY, killingBlow);
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            final JsonObject result = super.serializeToJson(context);
            result.add("killing_blow", killingBlow.serializeToJson());
            return result;
        }

        public boolean matches(ServerPlayer player, DamageSource killingBlow) {
            return this.killingBlow.matches(player, killingBlow);
        }
    }
}
