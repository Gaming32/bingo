package io.github.gaming32.bingo.util;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.github.gaming32.bingo.mixin.common.LocationCheckAccessor;
import net.minecraft.Util;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;

public class BingoUtil {
    public static int[] generateIntArray(int length) {
        final int[] result = new int[length];
        for (int i = 1; i < length; i++) {
            result[i] = i;
        }
        return result;
    }

    // Copied from IntArrays, but made to use RandomSource
    public static int[] shuffle(int[] a, RandomSource random) {
        for (int i = a.length; i-- != 0;) {
            final int p = random.nextInt(i + 1);
            final int t = a[i];
            a[i] = a[p];
            a[p] = t;
        }
        return a;
    }

    public static Collector<JsonElement, ?, JsonArray> toJsonArray() {
        return Collector.of(JsonArray::new, JsonArray::add, (a, b) -> {
            a.addAll(b);
            return a;
        });
    }

    public static CompoundTag compound(Map<String, Tag> nbt) {
        final CompoundTag result = new CompoundTag();
        nbt.forEach(result::put);
        return result;
    }

    public static ContextAwarePredicate wrapLocation(LocationPredicate location) {
        return ContextAwarePredicate.create(LocationCheckAccessor.createLocationCheck(location, BlockPos.ZERO));
    }

    public static <T> JsonObject toJsonObject(Codec<T> codec, T obj) {
        return Util.getOrThrow(codec.encodeStart(JsonOps.INSTANCE, obj), IllegalStateException::new).getAsJsonObject();
    }

    public static <T> T fromJsonElement(Codec<T> codec, JsonElement element) {
        return Util.getOrThrow(codec.parse(JsonOps.INSTANCE, element), JsonSyntaxException::new);
    }

    public static <T> List<T> addToList(List<T> a, T b) {
        return ImmutableList.<T>builderWithExpectedSize(a.size() + 1).addAll(a).add(b).build();
    }

    public static <T> Optional<T> fromOptionalJsonElement(Codec<T> codec, JsonElement element) {
        return element == null || element.isJsonNull() ? Optional.empty() : Optional.of(fromJsonElement(codec, element));
    }

    public static Optional<ContextAwarePredicate> getAdvancementLocation(JsonObject json, String key, DeserializationContext context) {
        return parseAdvancementLocation(key, json.get(key), context);
    }

    public static Optional<ContextAwarePredicate> parseAdvancementLocation(String name, JsonElement json, DeserializationContext context) {
        if (json == null) {
            return Optional.empty();
        }
        return ContextAwarePredicate.fromElement(name, context, json, LootContextParamSets.ADVANCEMENT_LOCATION)
            .orElseThrow(() -> new JsonParseException("Unable to parse advancement_location at " + name + ". Value was " + GsonHelper.getType(json)));
    }
}
