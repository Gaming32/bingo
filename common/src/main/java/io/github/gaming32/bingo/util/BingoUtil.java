package io.github.gaming32.bingo.util;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;

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

    public static CompoundTag compound(Map<String, ? extends Tag> nbt) {
        final CompoundTag result = new CompoundTag();
        nbt.forEach(result::put);
        return result;
    }

    public static ListTag list(List<? extends Tag> nbt) {
        final ListTag result = new ListTag();
        result.addAll(nbt);
        return result;
    }

    public static <T> JsonElement toJsonElement(Codec<T> codec, T obj) {
        return Util.getOrThrow(codec.encodeStart(JsonOps.INSTANCE, obj), IllegalStateException::new);
    }

    public static <T> JsonObject toJsonObject(Codec<T> codec, T obj) {
        return toJsonElement(codec, obj).getAsJsonObject();
    }

    public static <T> T fromJsonElement(Codec<T> codec, JsonElement element) throws JsonParseException {
        return Util.getOrThrow(codec.parse(JsonOps.INSTANCE, element), JsonParseException::new);
    }

    public static <T> Dynamic<?> toDynamic(Codec<T> codec, T obj) {
        return toDynamic(codec, obj, BingoCodecs.DEFAULT_OPS);
    }

    public static <T, O> Dynamic<O> toDynamic(Codec<T> codec, T obj, DynamicOps<O> ops) {
        return new Dynamic<>(ops, Util.getOrThrow(codec.encodeStart(ops, obj), IllegalStateException::new));
    }

    public static <T> T fromDynamic(Codec<T> codec, Dynamic<?> dynamic) throws IllegalArgumentException {
        return Util.getOrThrow(codec.parse(dynamic), IllegalArgumentException::new);
    }

    public static <T> List<T> addToList(List<T> a, T b) {
        return ImmutableList.<T>builderWithExpectedSize(a.size() + 1).addAll(a).add(b).build();
    }

    public static <T> Optional<T> fromOptionalJsonElement(Codec<T> codec, JsonElement element) {
        return element == null || element.isJsonNull() ? Optional.empty() : Optional.of(fromJsonElement(codec, element));
    }

    public static <T extends Enum<T>> T valueOf(String name, @NotNull T defaultValue) {
        try {
            return Enum.valueOf(defaultValue.getDeclaringClass(), name);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }
}
