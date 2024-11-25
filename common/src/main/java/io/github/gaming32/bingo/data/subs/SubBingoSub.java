package io.github.gaming32.bingo.data.subs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.MapCodec;

public record SubBingoSub(String key) implements BingoSub {
    public static final MapCodec<SubBingoSub> CODEC = Codec.STRING
        .fieldOf("key")
        .xmap(SubBingoSub::new, SubBingoSub::key);

    @Override
    public Dynamic<?> substitute(SubstitutionContext context) {
        final Dynamic<?> value = context.referable().get(key);
        if (value == null) {
            throw new IllegalArgumentException("Unresolved reference in bingo goal: " + key);
        }
        return value;
    }

    @Override
    public DataResult<BingoSub> validate(SubstitutionContext context) {
        if (!context.referable().containsKey(key)) {
            return DataResult.error(() -> "Unresolved reference in bingo goal: " + key);
        }
        return DataResult.success(this);
    }

    @Override
    public BingoSubType<SubBingoSub> type() {
        return BingoSubType.SUB.get();
    }
}
