package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public class BeaconEffectTrigger extends SimpleCriterionTrigger<BeaconEffectTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(
            player,
            MobEffectsPredicate.fromJson(json.get("effects")),
            MobEffectsPredicate.fromJson(json.get("total_effects"))
        );
    }

    public void trigger(ServerPlayer player, MobEffectInstance newEffect) {
        trigger(player, instance -> instance.matches(player, newEffect));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<MobEffectsPredicate> effect;
        private final Optional<MobEffectsPredicate> totalEffects;

        public TriggerInstance(
            Optional<ContextAwarePredicate> predicate,
            Optional<MobEffectsPredicate> effect,
            Optional<MobEffectsPredicate> totalEffects
        ) {
            super(predicate);
            this.effect = effect;
            this.totalEffects = totalEffects;
        }

        public static Criterion<TriggerInstance> effectApplied(MobEffect effect) {
            return BingoTriggers.BEACON_EFFECT.createCriterion(
                new TriggerInstance(
                    Optional.empty(),
                    MobEffectsPredicate.Builder.effects().and(effect).build(),
                    Optional.empty()
                )
            );
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            effect.ifPresent(p -> result.add("effect", p.serializeToJson()));
            totalEffects.ifPresent(p -> result.add("total_effects", p.serializeToJson()));
            return result;
        }

        public boolean matches(ServerPlayer player, MobEffectInstance newEffect) {
            if (this.effect.isPresent() && !this.effect.get().matches(Map.of(newEffect.getEffect(), newEffect))) {
                return false;
            }
            if (this.totalEffects.isPresent() && !this.totalEffects.get().matches(player)) {
                return false;
            }
            return true;
        }
    }
}
