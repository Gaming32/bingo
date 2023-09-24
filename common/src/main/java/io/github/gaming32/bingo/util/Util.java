package io.github.gaming32.bingo.util;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.github.gaming32.bingo.mixin.common.LocationCheckAccessor;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Collector;

public class Util {
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
        return net.minecraft.Util.getOrThrow(codec.encodeStart(JsonOps.INSTANCE, obj), IllegalArgumentException::new).getAsJsonObject();
    }

    public static <T> T fromJsonElement(Codec<T> codec, JsonElement element) {
        return net.minecraft.Util.getOrThrow(codec.parse(JsonOps.INSTANCE, element), JsonSyntaxException::new);
    }

    public static <T> List<T> addToList(List<T> a, T b) {
        //noinspection UnstableApiUsage
        return ImmutableList.<T>builderWithExpectedSize(a.size() + 1).addAll(a).add(b).build();
    }
}
