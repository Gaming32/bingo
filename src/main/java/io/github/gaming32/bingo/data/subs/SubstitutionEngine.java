package io.github.gaming32.bingo.data.subs;

import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import io.github.gaming32.bingo.util.BingoUtil;

import java.util.function.Function;
import java.util.stream.Stream;

public final class SubstitutionEngine {
    private static final String TYPE_FIELD = "bingo_type";

    private SubstitutionEngine() {
    }

    public static boolean hasSubstitutions(Dynamic<?> value) {
        if (value.get(TYPE_FIELD).result().isPresent()) {
            return true;
        }

        final var asList = value.asStreamOpt();
        if (asList.result().isPresent()) {
            return asList.result().get().anyMatch(SubstitutionEngine::hasSubstitutions);
        }

        return value.asMapOpt()
            .result()
            .stream()
            .flatMap(Function.identity())
            .map(Pair::getSecond)
            .anyMatch(SubstitutionEngine::hasSubstitutions);
    }

    public static Dynamic<?> performSubstitutions(Dynamic<?> value, SubstitutionContext context) {
        if (value.get(TYPE_FIELD).result().isPresent()) {
            return BingoUtil.fromDynamic(BingoSub.INNER_CODEC, value).substitute(context);
        }

        final var asList = value.asStreamOpt();
        if (asList.result().isPresent()) {
            return value.createList(
                asList.result().get().map(d -> performSubstitutions(d, context).convert(d.getOps()))
            );
        }

        return value.updateMapValues(pair -> pair.mapSecond(subValue ->
            performSubstitutions(subValue, context).convert(subValue.getOps())
        ));
    }

    public static DataResult<Dynamic<?>> validateSubstitutions(Dynamic<?> value, SubstitutionContext context) {
        if (value.get(TYPE_FIELD).result().isPresent()) {
            return BingoSub.INNER_CODEC.parse(value)
                .flatMap(sub -> sub.validate(context))
                .map(x -> value);
        }

        final var asList = value.asStreamOpt();
        if (asList.result().isPresent()) {
            return validateStream(asList.result().get(), context).map(x -> value);
        }

        final var asMap = value.asMapOpt();
        if (asMap.result().isPresent()) {
            return validateStream(asMap.result().get().map(Pair::getSecond), context).map(x -> value);
        }

        return DataResult.success(value);
    }

    private static DataResult<Unit> validateStream(Stream<? extends Dynamic<?>> stream, SubstitutionContext context) {
        return stream
            .map(d -> validateSubstitutions(d, context))
            .reduce(DataResult.success(Unit.INSTANCE), BingoUtil::combineError, (a, b) -> a);
    }
}
