package io.github.gaming32.bingo.data.subs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import io.github.gaming32.bingo.util.BingoCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;

import java.util.Map;

public record IntBingoSub(IntProvider provider) implements BingoSub {
    public static final Codec<IntBingoSub> CODEC = IntProvider.CODEC.xmap(IntBingoSub::new, IntBingoSub::provider);

    @Override
    public Dynamic<?> substitute(Map<String, Dynamic<?>> referable, RandomSource rand) {
        return referable.isEmpty()
            ? BingoCodecs.EMPTY_DYNAMIC.createInt(provider.sample(rand))
            : referable.values().iterator().next().createInt(provider.sample(rand));
    }

    @Override
    public BingoSubType<IntBingoSub> type() {
        return BingoSubType.INT.get();
    }
}
