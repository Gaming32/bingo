package io.github.gaming32.bingo.data.subs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.Dynamic;
import io.github.gaming32.bingo.util.BingoCodecs;
import net.minecraft.util.RandomSource;

import java.util.Map;
import java.util.Set;

public record SubstitutionContext(Map<String, Dynamic<?>> referable, RandomSource rand) {
    public static SubstitutionContext createValidationContext(Set<String> keys) {
        return new SubstitutionContext(
            Maps.asMap(keys, k -> BingoCodecs.EMPTY_DYNAMIC),
            RandomSource.createNewThreadLocalInstance()
        );
    }

    public Dynamic<?> getFactoryDynamic() {
        return referable.isEmpty() ? BingoCodecs.EMPTY_DYNAMIC : referable.values().iterator().next();
    }

    public SubstitutionContext harden() {
        return new SubstitutionContext(ImmutableMap.copyOf(referable), rand);
    }
}
