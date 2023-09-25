package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class KillSelfTrigger extends SimpleCriterionTrigger<KillSelfTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(player, DamageSourcePredicate.fromJson(json.get("killing_blow")));
    }

    public void trigger(ServerPlayer player, DamageSource killingBlow) {
        trigger(player, instance -> instance.matches(player, killingBlow));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<DamageSourcePredicate> killingBlow;

        public TriggerInstance(Optional<ContextAwarePredicate> player, Optional<DamageSourcePredicate> killingBlow) {
            super(player);
            this.killingBlow = killingBlow;
        }

        public static TriggerInstance killSelf() {
            return new TriggerInstance(Optional.empty(), Optional.empty());
        }

        public static TriggerInstance killSelf(DamageSourcePredicate killingBlow) {
            return new TriggerInstance(Optional.empty(), Optional.ofNullable(killingBlow));
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            killingBlow.ifPresent(p -> result.add("killing_blow", p.serializeToJson()));
            return result;
        }

        public boolean matches(ServerPlayer player, DamageSource killingBlow) {
            return this.killingBlow.isEmpty() || this.killingBlow.get().matches(player, killingBlow);
        }
    }
}
