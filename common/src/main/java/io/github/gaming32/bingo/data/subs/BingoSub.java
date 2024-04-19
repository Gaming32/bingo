package io.github.gaming32.bingo.data.subs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import io.github.gaming32.bingo.util.BingoCodecs;
import io.github.gaming32.bingo.util.BingoUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;

import java.util.Map;
import java.util.stream.Stream;

public interface BingoSub {
    Codec<BingoSub> CODEC = BingoSubType.REGISTER
        .registry()
        .byNameCodec()
        .dispatch(BingoSub::type, BingoSubType::codec);
    Codec<BingoSub> INNER_CODEC = BingoSubType.REGISTER
        .registry()
        .byNameCodec()
        .dispatch("bingo_type", BingoSub::type, BingoSubType::codec);

    Dynamic<?> substitute(Map<String, Dynamic<?>> referable, RandomSource rand);

    BingoSubType<?> type();

    static BingoSub random(int min, int max) {
        return new IntBingoSub(UniformInt.of(min, max));
    }

    static BingoSub literal(int value) {
        return new IntBingoSub(ConstantInt.of(value));
    }

    static BingoSub literal(String value) {
        return new WrapBingoSub(BingoCodecs.EMPTY_DYNAMIC.createString(value));
    }

    static BingoSub wrapInArray(BingoSub sub) {
        return new WrapBingoSub(BingoCodecs.EMPTY_DYNAMIC.createList(Stream.of(BingoUtil.toDynamic(INNER_CODEC, sub))));
    }
}
