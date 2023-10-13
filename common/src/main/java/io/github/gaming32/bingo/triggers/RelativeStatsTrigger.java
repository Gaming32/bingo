package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.game.BingoGame;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.Util;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RelativeStatsTrigger extends SimpleCriterionTrigger<RelativeStatsTrigger.TriggerInstance> {
    private static final Logger LOGGER = LogUtils.getLogger();

    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(
            player,
            PlayerPredicate.StatMatcher.CODEC.listOf().parse(JsonOps.INSTANCE, json.get("stats")).getOrThrow(false, LOGGER::error)
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
            result.add("stats", Util.getOrThrow(PlayerPredicate.StatMatcher.CODEC.listOf().encodeStart(JsonOps.INSTANCE, stats), IllegalStateException::new));
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
                            matcher.range().min().ifPresent(min -> {
                                setProgress(Math.min(value, min), min);
                            });
                            return false;
                        }
                    }

                    if (!stats.isEmpty()) {
                        stats.get(0).range().min().ifPresent(min -> {
                            setProgress(min, min);
                        });
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

        public <T> Builder stat(StatType<T> statType, Holder.Reference<T> stat, int minValue) {
            return stat(statType, stat, MinMaxBounds.Ints.atLeast(minValue));
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.RELATIVE_STATS.createCriterion(new TriggerInstance(player, stats));
        }
    }
}
