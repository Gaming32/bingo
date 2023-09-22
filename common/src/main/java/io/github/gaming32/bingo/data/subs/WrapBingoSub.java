package io.github.gaming32.bingo.data.subs;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.JsonSubber;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;

import java.util.Map;
import java.util.function.Consumer;

public record WrapBingoSub(JsonElement value) implements BingoSub {
    public static final Codec<WrapBingoSub> CODEC = ExtraCodecs.JSON.xmap(WrapBingoSub::new, WrapBingoSub::value);

    public WrapBingoSub(JsonElement value, Consumer<JsonSubber> subber) {
        this(Util.make(new JsonSubber(value), subber).json());
    }

    @Override
    public JsonElement substitute(Map<String, JsonElement> referable, RandomSource rand) {
        return BingoGoal.performSubstitutions(value, referable, rand);
    }

    @Override
    public BingoSubType<WrapBingoSub> getType() {
        return BingoSubType.WRAP.get();
    }
}
