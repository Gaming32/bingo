package io.github.gaming32.bingo.data.subs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.RandomSource;

import java.util.Map;

public record SubBingoSub(String key) implements BingoSub {
    public static final Codec<SubBingoSub> CODEC = Codec.STRING.xmap(SubBingoSub::new, SubBingoSub::key);

    @Override
    public Dynamic<?> substitute(Map<String, Dynamic<?>> referable, RandomSource rand) {
        final Dynamic<?> value = referable.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Unresolved reference in bingo goal: " + key);
        }
        return value;
    }

    @Override
    public BingoSubType<SubBingoSub> type() {
        return BingoSubType.SUB.get();
    }
}
