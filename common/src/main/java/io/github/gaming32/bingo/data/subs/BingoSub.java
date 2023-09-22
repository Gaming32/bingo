package io.github.gaming32.bingo.data.subs;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.github.gaming32.bingo.util.BingoCodecs;
import net.minecraft.Util;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;

import java.util.Map;

public interface BingoSub {
    Codec<BingoSub> CODEC = BingoCodecs.registrarByName(BingoSubType.REGISTRAR)
        .dispatch(BingoSub::getType, BingoSubType::codec);
    Codec<BingoSub> INNER_CODEC = BingoCodecs.registrarByName(BingoSubType.REGISTRAR)
        .dispatch("bingo_type", BingoSub::getType, BingoSubType::codec);

    JsonElement substitute(Map<String, JsonElement> referable, RandomSource rand);

    BingoSubType<?> getType();

    default JsonObject serializeToJson() {
        return Util.getOrThrow(CODEC.encodeStart(JsonOps.INSTANCE, this), IllegalArgumentException::new).getAsJsonObject();
    }

    default JsonObject serializeInnerToJson() {
        return Util.getOrThrow(INNER_CODEC.encodeStart(JsonOps.INSTANCE, this), IllegalArgumentException::new).getAsJsonObject();
    }

    static BingoSub deserialize(JsonElement element) {
        return Util.getOrThrow(CODEC.parse(JsonOps.INSTANCE, element), JsonSyntaxException::new);
    }

    static BingoSub deserializeInner(JsonElement element) {
        return Util.getOrThrow(INNER_CODEC.parse(JsonOps.INSTANCE, element), JsonSyntaxException::new);
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
