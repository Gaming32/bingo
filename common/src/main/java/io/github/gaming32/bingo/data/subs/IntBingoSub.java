package io.github.gaming32.bingo.data.subs;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;

import java.util.Map;

public record IntBingoSub(IntProvider provider) implements BingoSub {
    public static final Codec<IntBingoSub> CODEC = IntProvider.CODEC.xmap(IntBingoSub::new, IntBingoSub::provider);

    @Override
    public JsonElement substitute(Map<String, JsonElement> referable, RandomSource rand) {
        return new JsonPrimitive(provider.sample(rand));
    }

    @Override
    public BingoSubType<IntBingoSub> type() {
        return BingoSubType.INT.get();
    }
}
