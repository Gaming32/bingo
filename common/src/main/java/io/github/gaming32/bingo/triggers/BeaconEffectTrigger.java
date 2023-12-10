package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public class BeaconEffectTrigger extends SimpleCriterionTrigger<BeaconEffectTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, MobEffectInstance newEffect) {
        trigger(player, instance -> instance.matches(player, newEffect));
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<MobEffectsPredicate> effect,
        Optional<MobEffectsPredicate> totalEffects
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.strictOptionalField(MobEffectsPredicate.CODEC, "effect").forGetter(TriggerInstance::effect),
                ExtraCodecs.strictOptionalField(MobEffectsPredicate.CODEC, "total_effects").forGetter(TriggerInstance::totalEffects)
            ).apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> effectApplied(MobEffect effect) {
            return BingoTriggers.BEACON_EFFECT.get().createCriterion(
                new TriggerInstance(
                    Optional.empty(),
                    MobEffectsPredicate.Builder.effects().and(effect).build(),
                    Optional.empty()
                )
            );
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
