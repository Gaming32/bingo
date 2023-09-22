package io.github.gaming32.bingo.data.subs;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;

import java.util.Map;

public record SubBingoSub(String key) implements BingoSub {
    public static final Codec<SubBingoSub> CODEC = Codec.STRING.xmap(SubBingoSub::new, SubBingoSub::key);

    @Override
    public JsonElement substitute(Map<String, JsonElement> referable, RandomSource rand) {
        final JsonElement value = referable.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Unresolved reference in bingo goal: " + key);
        }
        return value;
    }

    @Override
    public BingoSubType<SubBingoSub> getType() {
        return BingoSubType.SUB.get();
    }
}
