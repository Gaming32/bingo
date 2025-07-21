package io.github.gaming32.bingo.util;

import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

public class CustomEnumCodec<E extends Enum<E>> {
    private final ImmutableBiMap<E, String> names;
    private final Codec<E> codec;

    private CustomEnumCodec(Class<E> clazz, Function<E, String> nameMapper) {
        if (!clazz.isEnum()) {
            throw new IllegalArgumentException(clazz + " is not an enum!");
        }
        final E[] constants = clazz.getEnumConstants();

        final ImmutableBiMap.Builder<E, String> namesBuilder = ImmutableBiMap.builderWithExpectedSize(constants.length);
        for (final E constant : constants) {
            namesBuilder.put(constant, nameMapper.apply(constant));
        }
        names = namesBuilder.build();

        codec = Codec.STRING.comapFlatMap(
            name -> Optional.ofNullable(names.inverse().get(name))
                .map(DataResult::success)
                .orElseGet(() -> DataResult.error(() -> "Unknown value: " + name)),
            names::get
        );
    }

    public static <E extends Enum<E>> CustomEnumCodec<E> of(Class<E> clazz, Function<E, String> nameMapper) {
        return new CustomEnumCodec<>(clazz, nameMapper);
    }

    public static <E extends Enum<E>> CustomEnumCodec<E> of(Class<E> clazz) {
        return new CustomEnumCodec<>(clazz, e -> e.name().toLowerCase(Locale.ROOT));
    }

    public ImmutableBiMap<E, String> names() {
        return names;
    }

    public Codec<E> codec() {
        return codec;
    }
}
