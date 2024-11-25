package io.github.gaming32.bingo.data.subs;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import io.github.gaming32.bingo.data.JsonSubber;
import io.github.gaming32.bingo.data.goal.GoalSubstitutionSystem;
import net.minecraft.Util;
import net.minecraft.util.RandomSource;

import java.util.Map;
import java.util.function.Consumer;

public record WrapBingoSub(Dynamic<?> value) implements BingoSub {
    public static final MapCodec<WrapBingoSub> CODEC = Codec.PASSTHROUGH
        .fieldOf("value")
        .xmap(WrapBingoSub::new, WrapBingoSub::value);

    public WrapBingoSub(JsonElement value, Consumer<JsonSubber> subber) {
        this(new Dynamic<>(JsonOps.INSTANCE, Util.make(new JsonSubber(value), subber).json()));
    }

    @Override
    public Dynamic<?> substitute(Map<String, Dynamic<?>> referable, RandomSource rand) {
        return GoalSubstitutionSystem.performSubstitutions(value, referable, rand);
    }

    @Override
    public BingoSubType<WrapBingoSub> type() {
        return BingoSubType.WRAP.get();
    }
}
