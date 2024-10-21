package io.github.gaming32.bingo.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.util.Optional;
import java.util.stream.Stream;

public class StatCodecs {
    @SuppressWarnings("unchecked")
    public static final Codec<Stat<?>> CODEC = (Codec<Stat<?>>)(Codec<?>)createCodec(); // Nice cast, thanks Java
    public static final Codec<Stat<?>> STRING_CODEC = createStringCodec();

    private static <S> Codec<Stat<S>> createCodec() {
        return new MapCodec<Stat<S>>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.of(ops.createString("type"), ops.createString("stat"));
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> DataResult<Stat<S>> decode(DynamicOps<T> ops, MapLike<T> input) {
                final DataResult<ResourceLocation> typeKey = get(ops, input, "type");
                if (typeKey.result().isEmpty()) {
                    return DataResult.error(typeKey.error().orElseThrow()::message);
                }
                final StatType<S> type = (StatType<S>)BuiltInRegistries.STAT_TYPE.getValue(typeKey.result().get());
                if (type == null) {
                    return DataResult.error(() -> "Unknown stat_type " + typeKey.result().orElseThrow());
                }
                final DataResult<ResourceLocation> statKey = get(ops, input, "stat");
                if (statKey.result().isEmpty()) {
                    return DataResult.error(statKey.error().orElseThrow()::message);
                }
                final S statId = type.getRegistry().getValue(statKey.result().get());
                if (statId == null) {
                    return DataResult.error(() -> "Unknown " + typeKey.result().orElseThrow() + " " + statKey.result().orElseThrow());
                }
                return DataResult.success(type.get(statId));
            }

            @Override
            public <T> RecordBuilder<T> encode(Stat<S> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                add(ops, prefix, "type", BuiltInRegistries.STAT_TYPE, input.getType());
                add(ops, prefix, "stat", input.getType().getRegistry(), input.getValue());
                return prefix;
            }

            private static <T> DataResult<ResourceLocation> get(DynamicOps<T> ops, MapLike<T> input, String key) {
                return Optional.ofNullable(input.get(key))
                    .map(ops::getStringValue)
                    .map(v -> v.flatMap(ResourceLocation::read))
                    .orElseGet(() -> DataResult.error(() -> "key missing: " + key + " in " + input));
            }

            private static <T, V> void add(
                DynamicOps<T> ops, RecordBuilder<T> prefix,
                String name, Registry<V> registry, V value
            ) {
                final ResourceLocation key = registry.getKey(value);
                if (key == null) {
                    prefix.withErrorsFrom(
                        DataResult.error(() -> "Unregistered value " + value + " in " + registry.key().location())
                    );
                    return;
                }
                prefix.add(name, ops.createString(key.toString()));
            }
        }.codec();
    }

    private static Codec<Stat<?>> createStringCodec() {
        return Codec.STRING.comapFlatMap(
            name -> ObjectiveCriteria.byName(name)
                .map(criteria -> {
                    if (!(criteria instanceof Stat<?> stat)) {
                        return DataResult.<Stat<?>>error(() -> criteria.getName() + " is not a stat");
                    }
                    return DataResult.success(stat);
                })
                .orElseGet(() -> DataResult.error(() -> "Unknown stat " + name)),
            Stat::getName
        );
    }
}
