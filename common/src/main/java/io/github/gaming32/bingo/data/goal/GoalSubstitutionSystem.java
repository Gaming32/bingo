package io.github.gaming32.bingo.data.goal;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import io.github.gaming32.bingo.data.subs.BingoSub;
import io.github.gaming32.bingo.util.BingoUtil;
import net.minecraft.util.RandomSource;

import java.util.Map;

public class GoalSubstitutionSystem {
    private static final String TYPE_FIELD = "bingo_type";

    public static Dynamic<?> performSubstitutions(
        Dynamic<?> value,
        Map<String, Dynamic<?>> referable,
        RandomSource rand
    ) {
        final var asList = value.asStreamOpt();
        if (asList.result().isPresent()) {
            return value.createList(
                asList.result().get().map(d -> performSubstitutions(d, referable, rand).convert(d.getOps()))
            );
        }

        if (value.get(TYPE_FIELD).result().isPresent()) {
            return BingoUtil.fromDynamic(BingoSub.INNER_CODEC, value).substitute(referable, rand);
        }

        final var asMap = value.asMapOpt();
        if (asMap.result().isPresent()) {
            return value.createMap(asMap.result().get()
                .collect(ImmutableMap.toImmutableMap(
                    Pair::getFirst,
                    e -> performSubstitutions(e.getSecond(), referable, rand).convert(e.getSecond().getOps())
                ))
            );
        }

        return value;
    }
}
