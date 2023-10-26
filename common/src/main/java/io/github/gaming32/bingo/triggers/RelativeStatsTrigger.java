package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.game.BingoGame;
import io.github.gaming32.bingo.util.BingoUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.*;
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

public class RelativeStatsTrigger extends SimpleCriterionTrigger<RelativeStatsTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(
            player,
            BingoUtil.fromJsonElement(PlayerPredicate.StatMatcher.CODEC.listOf(), json.get("stats"))
        );
    }

    public void trigger(ServerPlayer player) {
        trigger(player, instance -> instance.matches(player));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class TriggerInstance extends AbstractProgressibleTriggerInstance {
        private final List<PlayerPredicate.StatMatcher<?>> stats;

        public TriggerInstance(Optional<ContextAwarePredicate> player, List<PlayerPredicate.StatMatcher<?>> stats) {
            super(player);
            this.stats = stats;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            result.add("stats", BingoUtil.toJsonElement(PlayerPredicate.StatMatcher.CODEC.listOf(), stats));
            return result;
        }

        public boolean matches(ServerPlayer player) {
            final BingoGame game = Bingo.activeGame;
            if (game != null) {
                final Object2IntMap<Stat<?>> baseStats = game.getBaseStats(player);
                if (baseStats != null) {
                    final StatsCounter currentStats = player.getStats();
                    for (final PlayerPredicate.StatMatcher<?> matcher : stats) {
                        final Stat<?> stat = matcher.stat().get();
                        final int value = currentStats.getValue(stat) - baseStats.getInt(stat);
                        if (!matcher.range().matches(value)) {
                            matcher.range().min().ifPresent(min -> setProgress(player, Math.min(value, min), min));
                            return false;
                        }
                    }

                    if (!stats.isEmpty()) {
                        stats.get(0).range().min().ifPresent(min -> setProgress(player, min, min));
                    }
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
                BuiltInRegistries.CUSTOM_STAT.getHolderOrThrow(ResourceKey.create(
                    Registries.CUSTOM_STAT, customStat
                )),
                range
            );
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.RELATIVE_STATS.createCriterion(new TriggerInstance(player, stats));
        }
    }
}
