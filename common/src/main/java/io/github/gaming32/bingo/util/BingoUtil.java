package io.github.gaming32.bingo.util;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.github.gaming32.bingo.Bingo;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.locale.Language;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
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
        return codec.encodeStart(JsonOps.INSTANCE, obj).getOrThrow();
    }

    public static <T> Dynamic<?> toDynamic(Codec<T> codec, T obj) {
        return toDynamic(codec, obj, BingoCodecs.DEFAULT_OPS);
    }

    public static <T, O> Dynamic<O> toDynamic(Codec<T> codec, T obj, DynamicOps<O> ops) {
        return new Dynamic<>(ops, codec.encodeStart(ops, obj).getOrThrow());
    }

    public static <T> T fromDynamic(Codec<T> codec, Dynamic<?> dynamic) throws IllegalArgumentException {
        return codec.parse(dynamic).getOrThrow(IllegalArgumentException::new);
    }

    public static <T extends Enum<T>> T valueOf(String name, @NotNull T defaultValue) {
        try {
            return Enum.valueOf(defaultValue.getDeclaringClass(), name);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    public static MutableComponent ordinal(int n) {
        if (n >= 1 && n <= 16) {
            return Bingo.translatable("bingo.ordinal." + n);
        } else {
            return Bingo.translatable("bingo.ordinal.generic", n);
        }
    }

    public static MutableComponent ensureHasFallback(MutableComponent component) {
        if (component.getContents() instanceof TranslatableContents translatable && translatable.getFallback() == null) {
            final String fallbackText = Language.getInstance().getOrDefault(translatable.getKey(), null);

            Object[] args = translatable.getArgs();
            if (args.length > 0) {
                args = args.clone();
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof MutableComponent subComponent) {
                        args[i] = ensureHasFallback(subComponent);
                    }
                }
            }

            Style style = component.getStyle();
            if (style.getHoverEvent() != null) {
                if (style.getHoverEvent().getAction() == HoverEvent.Action.SHOW_TEXT) {
                    final Component hoverText = style.getHoverEvent().getValue(HoverEvent.Action.SHOW_TEXT);
                    if (hoverText instanceof MutableComponent mutableComponent) {
                        style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, mutableComponent));
                    }
                } else if (style.getHoverEvent().getAction() == HoverEvent.Action.SHOW_ENTITY) {
                    final HoverEvent.EntityTooltipInfo info = style.getHoverEvent().getValue(HoverEvent.Action.SHOW_ENTITY);
                    assert info != null;
                    if (info.name.orElse(null) instanceof MutableComponent mutableComponent) {
                        style = style.withHoverEvent(new HoverEvent(
                            HoverEvent.Action.SHOW_ENTITY,
                            new HoverEvent.EntityTooltipInfo(info.type, info.id, ensureHasFallback(mutableComponent))
                        ));
                    }
                }
            }

            List<Component> siblings = component.getSiblings();
            if (!siblings.isEmpty()) {
                siblings = new ArrayList<>(siblings);
                for (int i = 0; i < siblings.size(); i++) {
                    if (siblings.get(i) instanceof MutableComponent subComponent) {
                        siblings.set(i, ensureHasFallback(subComponent));
                    }
                }
            }

            final MutableComponent result = Component.translatableWithFallback(
                translatable.getKey(), fallbackText, args
            ).setStyle(style);
            result.getSiblings().addAll(siblings);
            return result;
        }
        return component;
    }

    /**
     * @return Left is single player name, right is team name
     */
    public static Either<Component, Component> getDisplayName(PlayerTeam team, PlayerList playerList) {
        final Iterator<ServerPlayer> players = team.getPlayers()
            .stream()
            .map(playerList::getPlayerByName)
            .filter(Objects::nonNull)
            .iterator();
        if (players.hasNext()) {
            final ServerPlayer player = players.next();
            if (!players.hasNext()) {
                return Either.left(player.getName());
            }
        }
        return Either.right(team.getDisplayName());
    }

    public static String formatRemainingTime(long remainingTime) {
        if (remainingTime < 0)
            remainingTime = 0;
        int hours = (int) (remainingTime / 1000 / 60 / 60);
        int minutes = (int) (remainingTime / 1000 / 60 % 60);
        int seconds = (int) (remainingTime / 1000 % 60);
        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours);
            sb.append(":");
        }
        sb.append(String.format("%02d:%02d", minutes, seconds));
        return sb.toString();
    }

    public static <T, R> Either<R, R> mapEither(Either<? extends T, ? extends T> either, Function<? super T, ? extends R> mapper) {
        return either.mapBoth(mapper, mapper);
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
        return Math.acos(a.dot(b) / (a.length() * b.length()));
    }

    public static double distanceToSquareEdge(double angle, double squareRadius) {
        final double sin = Math.abs(Math.sin(angle));
        final double cos = Math.abs(Math.cos(angle));
        return squareRadius * sin <= squareRadius * cos
            ? squareRadius / cos
            : squareRadius / sin;
    }

    public static <T> HolderSet<T> toHolderSet(Registry<T> registry, ResourceOrTagKeyArgument.Result<T> result) {
        return result.unwrap().map(
            resource -> HolderSet.direct(registry.getOrThrow(resource)),
            registry::getOrThrow
        );
    }

    public static <T> Set<T> copyAndAdd(Set<T> set, T value) {
        return ImmutableSet.<T>builderWithExpectedSize(set.size() + 1).addAll(set).add(value).build();
    }

    public static <K, V> Multimap<K, V> copyAndPut(Multimap<K, V> map, K k, V v) {
        return ImmutableMultimap.<K, V>builder().putAll(map).put(k, v).build();
    }

    public static <A> DataResult<A> combineError(DataResult<A> current, DataResult<?> other) {
        return current.apply2((a, b) -> a, other);
    }

    public static <A> DataResult<A> combineError(DataResult<A> current, Supplier<String> error) {
        return combineError(current, DataResult.error(error));
    }
}
