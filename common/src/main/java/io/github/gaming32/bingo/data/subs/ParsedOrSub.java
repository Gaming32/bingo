package io.github.gaming32.bingo.data.subs;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import io.github.gaming32.bingo.util.BingoCodecs;
import io.github.gaming32.bingo.util.BingoUtil;

import java.util.function.Function;

public record ParsedOrSub<T>(Dynamic<?> serialized, Either<DataResult<T>, Codec<T>> valueOrCodec) {
    public static <T> MapCodec<ParsedOrSub<T>> optionalCodec(Codec<T> valueCodec, String key, T defaultValue) {
        return codec(valueCodec).optionalFieldOf(key, fromParsed(valueCodec, defaultValue));
    }

    public static <T> Codec<ParsedOrSub<T>> codec(Codec<T> valueCodec) {
        return Codec.PASSTHROUGH.xmap(data -> parse(valueCodec, data), ParsedOrSub::serialized);
    }

    public static <T> ParsedOrSub<T> parse(Codec<T> valueCodec, Dynamic<?> data) {
        if (SubstitutionEngine.hasSubstitutions(data)) {
            return new ParsedOrSub<>(data, Either.right(valueCodec));
        }
        return new ParsedOrSub<>(data, Either.left(valueCodec.parse(data)));
    }

    public static <T> ParsedOrSub<T> fromParsed(Codec<T> valueCodec, T value) {
        return fromParsed(valueCodec, value, BingoCodecs.DEFAULT_OPS);
    }

    public static <T> ParsedOrSub<T> fromParsed(Codec<T> valueCodec, T value, DynamicOps<?> ops) {
        return new ParsedOrSub<>(BingoUtil.toDynamic(valueCodec, value, ops), Either.left(DataResult.success(value)));
    }

    public static <T> ParsedOrSub<T> fromSub(BingoSub sub, Codec<T> valueCodec) {
        return fromSub(sub, valueCodec, BingoCodecs.DEFAULT_OPS);
    }

    public static <T> ParsedOrSub<T> fromSub(BingoSub sub, Codec<T> valueCodec, DynamicOps<?> ops) {
        return new ParsedOrSub<>(BingoUtil.toDynamic(BingoSub.INNER_CODEC, sub, ops), Either.right(valueCodec));
    }

    public DataResult<ParsedOrSub<T>> validate(SubstitutionContext context) {
        return valueOrCodec.map(
            Function.identity(),
            r -> SubstitutionEngine.validateSubstitutions(serialized, context)
        ).map(x -> this);
    }

    public DataResult<T> substitute(SubstitutionContext context) {
        return valueOrCodec.map(
            Function.identity(),
            r -> r.parse(SubstitutionEngine.performSubstitutions(serialized, context))
        );
    }

    public T substituteOrThrow(SubstitutionContext context) {
        return substitute(context).getOrThrow(IllegalArgumentException::new);
    }
}
