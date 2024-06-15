package io.github.gaming32.bingo.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.KeyCompressor;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntImmutableList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Array;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public final class BingoCodecs {
    /**
     * {@link DynamicOps} to use when you don't care about the type
     */
    public static final DynamicOps<?> DEFAULT_OPS = JsonOps.INSTANCE;
    /**
     * Empty {@link Dynamic} to use when you don't care about the ops
     */
    public static final Dynamic<?> EMPTY_DYNAMIC = new Dynamic<>(DEFAULT_OPS);

    public static final Codec<Character> CHAR = Codec.STRING.comapFlatMap(
        s -> s.length() == 1 ? DataResult.success(s.charAt(0)) : DataResult.error(() -> "String must be exactly one char, not " + s.length()),
        c -> Character.toString(c)
    );
    public static final Codec<Integer> INT_AS_STRING = Codec.STRING.comapFlatMap(
        s -> {
            try {
                return DataResult.success(Integer.valueOf(s));
            } catch (NumberFormatException e) {
                return DataResult.error(e::getMessage);
            }
        },
        Object::toString
    );
    public static final Codec<Int2IntMap> INT_2_INT_MAP = Codec.unboundedMap(INT_AS_STRING, Codec.INT)
        .xmap(Int2IntOpenHashMap::new, Function.identity());
    public static final Codec<IntList> INT_LIST = Codec.INT.listOf().xmap(IntImmutableList::new, Function.identity());
    public static final Codec<ItemStack> UNBOUNDED_ITEM_STACK = RecordCodecBuilder.create(
        instance -> instance.group(
            ItemStack.ITEM_NON_AIR_CODEC.fieldOf("id").forGetter(ItemStack::getItemHolder),
            ExtraCodecs.POSITIVE_INT.fieldOf("count").orElse(1).forGetter(ItemStack::getCount),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStack::getComponentsPatch)
        ).apply(instance, ItemStack::new)
    );
    public static final Codec<ItemStack> ITEM_STACK = Codec.withAlternative(UNBOUNDED_ITEM_STACK, ItemStack.SIMPLE_ITEM_CODEC);

    private BingoCodecs() {
    }

    public static <T> Codec<Optional<T>> optional(Codec<T> codec) {
        return Codec.either(Codec.EMPTY.codec(), codec).xmap(
            either -> either.map(u -> Optional.empty(), Optional::of),
            value -> value.<Either<Unit, T>>map(Either::right).orElseGet(() -> Either.left(Unit.INSTANCE))
        );
    }

    public static Codec<Integer> atLeast(int minInclusive) {
        return Codec.INT.validate(
            value -> value >= minInclusive
                ? DataResult.success(value)
                : DataResult.error(() -> "Value must be greater than " + minInclusive + ": " + value)
        );
    }

    public static <A> Codec<A> catchIAE(Codec<A> codec) {
        return Codec.of(codec, new Decoder<>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
                try {
                    return codec.decode(ops, input);
                } catch (IllegalArgumentException e) {
                    return DataResult.error(e::getMessage);
                }
            }
        }, "CatchIAE[" + codec + "]");
    }

    public static <A> MapCodec<A> catchIAE(MapCodec<A> codec) {
        return MapCodec.of(codec, new MapDecoder<>() {
            @Override
            public <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input) {
                try {
                    return codec.decode(ops, input);
                } catch (IllegalArgumentException e) {
                    return DataResult.error(e::getMessage);
                }
            }

            @Override
            public <T> KeyCompressor<T> compressor(DynamicOps<T> ops) {
                return codec.compressor(ops);
            }

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return codec.keys(ops);
            }
        }, () -> "CatchIAE[" + codec + "]");
    }

    public static <A> Codec<Set<A>> setOf(Codec<A> elementCodec) {
        return elementCodec.listOf().xmap(ImmutableSet::copyOf, ImmutableList::copyOf);
    }

    public static <A extends Enum<A>> Codec<Set<A>> enumSetOf(Codec<A> elementCodec) {
        return elementCodec.listOf().xmap(Sets::immutableEnumSet, ImmutableList::copyOf);
    }

    public static <A> Codec<A> exactly(A value, Codec<A> codec) {
        return codec.validate(
            a -> Objects.equals(a, value)
                ? DataResult.success(a)
                : DataResult.error(() -> "Value must equal " + value + ". Got " + a));
    }

    public static Codec<Integer> exactly(int value) {
        return exactly(value, Codec.INT);
    }

    /**
     * {@link Codec#withAlternative(Codec, Codec)} will always encode with {@code first}, which will trip any
     * {@link Codec#validate(Function)}. This codec will try to encode with both, and will return the
     * first successful encoding.
     */
    public static <A> Codec<A> firstValid(Codec<A> first, Codec<A> second) {
        return new FirstValidCodec<>(first, second);
    }

    public static <A> Codec<Set<A>> minifiedSet(Codec<A> elementCodec) {
        return Codec.either(setOf(elementCodec), elementCodec).xmap(
            either -> either.map(Function.identity(), ImmutableSet::of),
            set -> set.size() == 1 ? Either.right(set.iterator().next()) : Either.left(set)
        );
    }

    public static <A> MapCodec<Set<A>> minifiedSetField(Codec<A> elementCodec, String name) {
        return minifiedSet(elementCodec).optionalFieldOf(name, ImmutableSet.of());
    }

    public static MapCodec<OptionalInt> optionalInt(String name) {
        return Codec.INT.optionalFieldOf(name).xmap(
            opt -> opt.map(OptionalInt::of).orElseGet(OptionalInt::empty),
            opt -> opt.isPresent() ? Optional.of(opt.getAsInt()) : Optional.empty()
        );
    }

    public static MapCodec<Dynamic<?>> optionalDynamicField(String name) {
        return Codec.PASSTHROUGH.optionalFieldOf(name).xmap(
            opt -> opt.orElse(EMPTY_DYNAMIC),
            dyn -> dyn.getValue() == dyn.getOps().empty() ? Optional.empty() : Optional.of(dyn)
        );
    }

    public static MapCodec<Dynamic<?>> optionalDynamicField(String name, Dynamic<?> defaultValue) {
        return Codec.PASSTHROUGH.optionalFieldOf(name).xmap(
            opt -> opt.orElse(defaultValue),
            dyn -> dyn.convert(defaultValue.getOps()).getValue().equals(defaultValue.getValue()) ? Optional.empty() : Optional.of(dyn)
        );
    }

    @SuppressWarnings("unchecked")
    public static <A> Codec<A[]> array(Codec<A> elementCodec, Class<A> aClass) {
        return elementCodec.listOf().xmap(
            list -> list.toArray((A[])Array.newInstance(aClass, list.size())),
            ImmutableList::copyOf
        );
    }

    public static <V> Codec<Int2ObjectMap<V>> int2ObjectMap(Codec<V> valueCodec) {
        return Codec.unboundedMap(INT_AS_STRING, valueCodec).xmap(Int2ObjectOpenHashMap::new, Function.identity());
    }

    public static <K> Codec<Object2IntMap<K>> object2IntMap(Codec<K> keyCodec) {
        return Codec.unboundedMap(keyCodec, Codec.INT).xmap(Object2IntOpenHashMap::new, Function.identity());
    }

    public static final class FirstValidCodec<A> implements Codec<A> {
        private final Codec<A> first;
        private final Codec<A> second;

        public FirstValidCodec(Codec<A> first, Codec<A> second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
            final DataResult<Pair<A, T>> firstResult = first.decode(ops, input);
            if (firstResult.error().isEmpty()) {
                return firstResult;
            }
            final DataResult<Pair<A, T>> secondResult = second.decode(ops, input);
            if (secondResult.error().isEmpty()) {
                return secondResult;
            }
            return firstResult.apply2((a, b) -> b, secondResult);
        }

        @Override
        public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
            final DataResult<T> firstResult = first.encode(input, ops, prefix);
            if (firstResult.error().isEmpty()) {
                return firstResult;
            }
            final DataResult<T> secondResult = second.encode(input, ops, prefix);
            if (secondResult.error().isEmpty()) {
                return secondResult;
            }
            return firstResult.apply2((a, b) -> b, secondResult);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FirstValidCodec<?> that = (FirstValidCodec<?>)o;
            return Objects.equals(first, that.first) && Objects.equals(second, that.second);
        }

        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }

        @Override
        public String toString() {
            return "FirstValid[" + first + ", " + second + "]";
        }
    }
}
