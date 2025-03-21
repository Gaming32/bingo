package io.github.gaming32.bingo.subpredicates;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.game.BingoGame;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record BingoPlayerPredicate(
    List<PlayerPredicate.StatMatcher<?>> relativeStats
) implements EntitySubPredicate {
    public static final MapCodec<BingoPlayerPredicate> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            PlayerPredicate.StatMatcher.CODEC.listOf().optionalFieldOf("relative_stats", List.of())
                .forGetter(BingoPlayerPredicate::relativeStats)
        ).apply(instance, BingoPlayerPredicate::new)
    );

    @Override
    public boolean matches(Entity entity, ServerLevel level, @Nullable Vec3 position) {
        if (!(entity instanceof ServerPlayer player)) {
            return false;
        }
        final BingoGame game = level.getServer().bingo$getGame();
        if (game != null) {
            final Object2IntMap<Stat<?>> baseStats = game.getBaseStats(player);
            final StatsCounter currentStats = player.getStats();
            for (final PlayerPredicate.StatMatcher<?> matcher : relativeStats) {
                final Stat<?> stat = matcher.stat().get();
                final var currentValue = currentStats.getValue(stat);
                final int value = currentValue - baseStats.getOrDefault(stat, currentValue);
                if (!matcher.range().matches(value)) {
                    return false;
                }
            }
        }
        return true;
    }

    @NotNull
    @Override
    public MapCodec<? extends EntitySubPredicate> codec() {
        return CODEC;
    }

    public static class Builder {
        private final ImmutableList.Builder<PlayerPredicate.StatMatcher<?>> relativeStats = ImmutableList.builder();

        public static Builder player() {
            return new Builder();
        }

        public <T> Builder addRelativeStat(StatType<T> type, Holder.Reference<T> value, MinMaxBounds.Ints range) {
            relativeStats.add(new PlayerPredicate.StatMatcher<>(type, value, range));
            return this;
        }

        public BingoPlayerPredicate build() {
            return new BingoPlayerPredicate(relativeStats.build());
        }
    }
}
