package io.github.gaming32.bingo.data.subs;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.valueproviders.IntProvider;

public record IntBingoSub(IntProvider provider) implements BingoSub {
    public static final MapCodec<IntBingoSub> CODEC = IntProvider.CODEC
        .fieldOf("value")
        .xmap(IntBingoSub::new, IntBingoSub::provider);

    @Override
    public Dynamic<?> substitute(SubstitutionContext context) {
        return context.getFactoryDynamic().createInt(provider.sample(context.rand()));
    }

    @Override
    public BingoSubType<IntBingoSub> type() {
        return BingoSubType.INT.get();
    }
}
