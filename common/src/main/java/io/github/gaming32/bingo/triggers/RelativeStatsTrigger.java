package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.game.BingoGame;
import io.github.gaming32.bingo.triggers.progress.SimpleProgressibleCriterionTrigger;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RelativeStatsTrigger extends SimpleProgressibleCriterionTrigger<RelativeStatsTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player) {
        final ProgressListener<TriggerInstance> progressListener = getProgressListener(player);
        trigger(player, instance -> instance.matches(player, progressListener));
    }

    public static Builder builder() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        List<PlayerPredicate.StatMatcher<?>> stats
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                PlayerPredicate.StatMatcher.CODEC.listOf().fieldOf("stats").forGetter(TriggerInstance::stats)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(ServerPlayer player, ProgressListener<TriggerInstance> progressListener) {
            final BingoGame game = Bingo.activeGame;
            if (game != null) {
                final Object2IntMap<Stat<?>> baseStats = game.getBaseStats(player);
                final StatsCounter currentStats = player.getStats();
                for (final PlayerPredicate.StatMatcher<?> matcher : stats) {
                    final Stat<?> stat = matcher.stat().get();
                    final var currentValue = currentStats.getValue(stat);
                    final int value = currentValue - baseStats.getOrDefault(stat, currentValue);
                    if (!matcher.range().matches(value)) {
                        matcher.range().min().ifPresent(min -> progressListener.update(this, Math.min(value, min), min));
                        return false;
                    }
                }

                if (!stats.isEmpty()) {
                    stats.getFirst().range().min().ifPresent(min -> progressListener.update(this, min, min));
                }
            }
            return true;
        }
    }

    public static final class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private final List<PlayerPredicate.StatMatcher<?>> stats = new ArrayList<>();

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.ofNullable(player);
            return this;
        }

        public <T> Builder stat(StatType<T> statType, Holder.Reference<T> stat, MinMaxBounds.Ints range) {
            stats.add(new PlayerPredicate.StatMatcher<>(statType, stat, range));
            return this;
        }

        public Builder stat(ResourceLocation customStat, MinMaxBounds.Ints range) {
            return stat(
                Stats.CUSTOM,
                BuiltInRegistries.CUSTOM_STAT.getOrThrow(ResourceKey.create(
                    Registries.CUSTOM_STAT, customStat
                )),
                range
            );
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.RELATIVE_STATS.get().createCriterion(new TriggerInstance(player, stats));
        }
    }
}
