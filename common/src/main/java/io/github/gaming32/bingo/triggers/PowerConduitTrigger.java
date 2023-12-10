package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PowerConduitTrigger extends SimpleCriterionTrigger<PowerConduitTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, int level) {
        trigger(player, instance -> instance.matches(level));
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        MinMaxBounds.Ints level
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "level", MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::level)
            ).apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> powerConduit() {
            return BingoTriggers.POWER_CONDUIT.get().createCriterion(
                new TriggerInstance(Optional.empty(), MinMaxBounds.Ints.ANY)
            );
        }

        public static Criterion<TriggerInstance> powerConduit(MinMaxBounds.Ints level) {
            return BingoTriggers.POWER_CONDUIT.get().createCriterion(
                new TriggerInstance(Optional.empty(), level)
            );
        }

        public boolean matches(int level) {
            return this.level.matches(level);
        }
    }
}
