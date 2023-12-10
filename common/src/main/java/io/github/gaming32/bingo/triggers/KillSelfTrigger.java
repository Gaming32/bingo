package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class KillSelfTrigger extends SimpleCriterionTrigger<KillSelfTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, DamageSource killingBlow) {
        trigger(player, instance -> instance.matches(player, killingBlow));
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<DamageSourcePredicate> killingBlow
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.strictOptionalField(DamageSourcePredicate.CODEC, "killing_blow").forGetter(TriggerInstance::killingBlow)
            ).apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> killSelf() {
            return BingoTriggers.KILL_SELF.createCriterion(
                new TriggerInstance(Optional.empty(), Optional.empty())
            );
        }

        public static Criterion<TriggerInstance> killSelf(DamageSourcePredicate killingBlow) {
            return BingoTriggers.KILL_SELF.createCriterion(
                new TriggerInstance(Optional.empty(), Optional.ofNullable(killingBlow))
            );
        }

        public boolean matches(ServerPlayer player, DamageSource killingBlow) {
            return this.killingBlow.isEmpty() || this.killingBlow.get().matches(player, killingBlow);
        }
    }
}
