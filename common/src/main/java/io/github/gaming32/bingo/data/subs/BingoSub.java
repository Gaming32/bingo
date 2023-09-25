package io.github.gaming32.bingo.data.subs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import io.github.gaming32.bingo.util.BingoCodecs;
import io.github.gaming32.bingo.util.BingoUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;

import java.util.Map;

public interface BingoSub {
    Codec<BingoSub> CODEC = BingoCodecs.registrarByName(BingoSubType.REGISTRAR)
        .dispatch(BingoSub::type, BingoSubType::codec);
    Codec<BingoSub> INNER_CODEC = BingoCodecs.registrarByName(BingoSubType.REGISTRAR)
        .dispatch("bingo_type", BingoSub::type, BingoSubType::codec);

    JsonElement substitute(Map<String, JsonElement> referable, RandomSource rand);

    BingoSubType<?> type();

    default JsonObject serializeToJson() {
        return BingoUtil.toJsonObject(CODEC, this);
    }

    default JsonObject serializeInnerToJson() {
        return BingoUtil.toJsonObject(INNER_CODEC, this);
    }

    static BingoSub deserialize(JsonElement element) {
        return BingoUtil.fromJsonElement(CODEC, element);
    }

    static BingoSub deserializeInner(JsonElement element) {
        return BingoUtil.fromJsonElement(INNER_CODEC, element);
    }

    static BingoSub random(int min, int max) {
        return new IntBingoSub(UniformInt.of(min, max));
    }

    static BingoSub literal(int value) {
        return new IntBingoSub(ConstantInt.of(value));
    }

    static BingoSub literal(String value) {
        return new WrapBingoSub(new JsonPrimitive(value));
    }

    static BingoSub wrapInArray(BingoSub sub) {
        final JsonArray array = new JsonArray(1);
        array.add(sub.serializeInnerToJson());
        return new WrapBingoSub(array);
    }
}
