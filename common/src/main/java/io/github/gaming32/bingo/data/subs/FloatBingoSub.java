package io.github.gaming32.bingo.data.subs;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.valueproviders.FloatProvider;

public record FloatBingoSub(FloatProvider provider) implements BingoSub {
    public static final MapCodec<FloatBingoSub> CODEC = FloatProvider.CODEC
        .fieldOf("value")
        .xmap(FloatBingoSub::new, FloatBingoSub::provider);

    @Override
    public Dynamic<?> substitute(SubstitutionContext context) {
        return context.getFactoryDynamic().createFloat(provider.sample(context.rand()));
    }

    @Override
    public BingoSubType<FloatBingoSub> type() {
        return BingoSubType.FLOAT.get();
    }
}
