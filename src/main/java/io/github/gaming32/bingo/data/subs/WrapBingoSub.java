package io.github.gaming32.bingo.data.subs;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import io.github.gaming32.bingo.data.JsonSubber;
import net.minecraft.Util;

import java.util.function.Consumer;

public record WrapBingoSub(Dynamic<?> value) implements BingoSub {
    public static final MapCodec<WrapBingoSub> CODEC = Codec.PASSTHROUGH
        .fieldOf("value")
        .xmap(WrapBingoSub::new, WrapBingoSub::value);

    public WrapBingoSub(JsonElement value, Consumer<JsonSubber> subber) {
        this(new Dynamic<>(JsonOps.INSTANCE, Util.make(new JsonSubber(value), subber).json()));
    }

    @Override
    public Dynamic<?> substitute(SubstitutionContext context) {
        return SubstitutionEngine.performSubstitutions(value, context);
    }

    @Override
    public DataResult<BingoSub> validate(SubstitutionContext context) {
        return SubstitutionEngine.validateSubstitutions(value, context).map(x -> this);
    }

    @Override
    public BingoSubType<WrapBingoSub> type() {
        return BingoSubType.WRAP.get();
    }
}
