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
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collector;

public class BingoUtil {
    private static final Hash.Strategy<Holder<?>> HOLDER_STRATEGY = new Hash.Strategy<>() {
        @Override
        public int hashCode(Holder<?> holder) {
            if (holder == null) {
                return 0;
            }

            var key = holder.unwrapKey();
            // use ternary here to avoid boxing Integer
            //noinspection OptionalIsPresent
            return key.isPresent() ? System.identityHashCode(key.get()) : holder.hashCode();
        }

        @Override
        public boolean equals(Holder<?> a, Holder<?> b) {
            if (a == b) {
                return true;
            }
            if (a == null || b == null) {
                return false;
            }

            if (a.getClass() != b.getClass()) {
                return false;
            }
            var keyA = a.unwrapKey();
            if (keyA.isPresent() && keyA.equals(b.unwrapKey())) {
                return true;
            }
            return a.equals(b);
        }
    };

    @SuppressWarnings("unchecked")
    public static <T> Hash.Strategy<Holder<T>> holderStrategy() {
        return (Hash.Strategy<Holder<T>>) (Hash.Strategy<?>) HOLDER_STRATEGY;
    }

    public static <T> Holder<T> getBuiltInHolder(Registry<T> registry, T value) {
        ResourceKey<T> key = registry.getResourceKey(value).orElseThrow();
        return registry.getHolderOrThrow(key);
    }

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

    public static <T> Tag toTag(Codec<T> codec, T obj) {
        return Util.getOrThrow(codec.encodeStart(NbtOps.INSTANCE, obj), IllegalStateException::new);
    }

    public static <T> CompoundTag toCompoundTag(Codec<T> codec, T obj) {
        final Tag result = toTag(codec, obj);
        if (!(result instanceof CompoundTag compound)) {
            throw new IllegalStateException("Obj " + obj + " didn't serialize to CompoundTag");
        }
        return compound;
    }

    public static <T> T fromTag(Codec<T> codec, Tag tag) {
        return Util.getOrThrow(codec.parse(NbtOps.INSTANCE, tag), IllegalArgumentException::new);
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

    public static Component getDisplayName(PlayerTeam team, PlayerList playerList) {
        final Iterator<ServerPlayer> players = team.getPlayers()
            .stream()
            .map(playerList::getPlayerByName)
            .filter(Objects::nonNull)
            .iterator();
        if (players.hasNext()) {
            final ServerPlayer player = players.next();
            if (!players.hasNext()) {
                return player.getName();
            }
        }
        return team.getDisplayName();
    }

    public static boolean isDyeableArmor(Item item) {
        return item instanceof ArmorItem armor && armor.getMaterial()
            .value()
            .layers()
            .stream()
            .anyMatch(ArmorMaterial.Layer::dyeable);
    }

    public static boolean collidesWithProjectedBox(Vec3 entityOrigin, Vec3 boxNormal, double boxWidth) {
        // Distance from origin to the closest point on the boxNormal line
        final double pointDistance = entityOrigin.dot(boxNormal) / boxNormal.lengthSqr();

        // Entity is behind box
        if (pointDistance < 0) {
            return false;
        }

        final Vec3 closestPoint = boxNormal.scale(pointDistance);
        final double angle = vectorAngle(closestPoint, entityOrigin);
        final double maxDistance = distanceToSquareEdge(angle, boxWidth / 2);
        return closestPoint.distanceToSqr(entityOrigin) <= maxDistance * maxDistance;
    }

    public static double vectorAngle(Vec3 a, Vec3 b) {
        return Math.atan2(a.dot(b), a.length() * b.length());
    }

    public static double distanceToSquareEdge(double angle, double squareRadius) {
        final double sin = Math.abs(Math.sin(angle));
        final double cos = Math.abs(Math.cos(angle));
        return squareRadius * sin <= squareRadius * cos
            ? squareRadius / cos
            : squareRadius / sin;
    }
}
