package io.github.gaming32.bingo.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Arrays;
import java.util.function.IntFunction;

public class BingoStreamCodecs {
    public static <E extends Enum<E>> StreamCodec<FriendlyByteBuf, E> enum_(Class<E> clazz) {
        return StreamCodec.of(FriendlyByteBuf::writeEnum, buf -> buf.readEnum(clazz));
    }

    public static <B extends ByteBuf, V> StreamCodec.CodecOperation<B, V, V[]> array(IntFunction<V[]> factory) {
        return codec -> codec.apply(ByteBufCodecs.list()).map(l -> l.toArray(factory), Arrays::asList);
    }

    public static <B, V> StreamCodec<B, V> uncheckedUnit(V value) {
        return StreamCodec.of((buf, ignored) -> {}, buf -> value);
    }
}
