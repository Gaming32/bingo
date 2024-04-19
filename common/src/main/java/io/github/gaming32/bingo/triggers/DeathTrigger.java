package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class DeathTrigger extends SimpleCriterionTrigger<DeathTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, DamageSource source) {
        trigger(player, instance -> instance.matches(player, source));
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<DamageSourcePredicate> source
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                DamageSourcePredicate.CODEC.optionalFieldOf("source").forGetter(TriggerInstance::source)
            ).apply(instance, TriggerInstance::new)
        );

        public static TriggerInstance death(DamageSourcePredicate source) {
            return new TriggerInstance(Optional.empty(), Optional.ofNullable(source));
        }

        public boolean matches(ServerPlayer player, DamageSource source) {
            return this.source.isEmpty() || this.source.get().matches(player, source);
        }
    }
}
