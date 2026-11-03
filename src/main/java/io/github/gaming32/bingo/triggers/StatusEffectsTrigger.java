package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.triggers.progress.SimpleProgressibleCriterionTrigger;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public class StatusEffectsTrigger extends SimpleProgressibleCriterionTrigger<StatusEffectsTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player) {
        final ProgressListener<TriggerInstance> progressListener = getProgressListener(player);
        final int effectCount = player.getActiveEffectsMap().size();
        trigger(player, instance -> instance.matches(effectCount, progressListener));
    }

    public record TriggerInstance(
        Optional<Holder<LootItemCondition>> player,
        int minCount
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                ExtraCodecs.POSITIVE_INT.fieldOf("min_count").forGetter(TriggerInstance::minCount)
            ).apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> statusEffects(int minCount) {
            return BingoTriggers.STATUS_EFFECTS.get().createCriterion(
                new TriggerInstance(Optional.empty(), minCount)
            );
        }

        public boolean matches(int effectCount, ProgressListener<TriggerInstance> progressListener) {
            progressListener.update(this, Math.min(effectCount, minCount), minCount);
            return effectCount >= minCount;
        }
    }
}
