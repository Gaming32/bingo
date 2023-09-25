package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BeaconEffectTrigger extends SimpleCriterionTrigger<BeaconEffectTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext context) {
        return new TriggerInstance(
            predicate,
            MobEffectsPredicate.fromJson(json.get("effect")),
            MobEffectsPredicate.fromJson(json.get("total_effects"))
        );
    }

    public void trigger(ServerPlayer player, MobEffectInstance newEffect) {
        trigger(player, instance -> instance.matches(player, newEffect));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final MobEffectsPredicate effect;
        private final MobEffectsPredicate totalEffects;

        public TriggerInstance(
            ContextAwarePredicate predicate,
            MobEffectsPredicate effect,
            MobEffectsPredicate totalEffects
        ) {
            super(ID, predicate);
            this.effect = effect;
            this.totalEffects = totalEffects;
        }

        public static TriggerInstance effectApplied(MobEffect effect) {
            return new TriggerInstance(
                ContextAwarePredicate.ANY,
                MobEffectsPredicate.effects().and(effect),
                MobEffectsPredicate.ANY
            );
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            final JsonObject result = super.serializeToJson(context);
            result.add("effect", effect.serializeToJson());
            result.add("total_effects", totalEffects.serializeToJson());
            return result;
        }

        public boolean matches(ServerPlayer player, MobEffectInstance newEffect) {
            if (!this.effect.matches(Map.of(newEffect.getEffect(), newEffect))) {
                return false;
            }
            if (!this.totalEffects.matches(player)) {
                return false;
            }
            return true;
        }
    }
}
