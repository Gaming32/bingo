package io.github.gaming32.bingo.util;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.architectury.registry.registries.Registrar;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

import java.util.Optional;

public final class BingoCodecs {
    public static final Codec<Character> CHAR = Codec.STRING.comapFlatMap(
        s -> s.length() == 1 ? DataResult.success(s.charAt(0)) : DataResult.error(() -> "String must be exactly one char, not " + s.length()),
        c -> Character.toString(c)
    );

    private BingoCodecs() {
    }

    // Registry.byNameCodec, but adapted for Registrar
    public static <T> Codec<T> registrarByName(Registrar<T> registrar) {
        final Codec<T> uncompressed = ResourceLocation.CODEC.flatXmap(
            location -> Optional.ofNullable(registrar.get(location))
                .map(DataResult::success)
                .orElseGet(() -> DataResult.error(() -> "Unknown registry key in " + registrar.key() + ": " + location)),
            obj -> registrar.getKey(obj)
                .map(ResourceKey::location)
                .map(DataResult::success)
                .orElseGet(() -> DataResult.error(() -> "Unknown registry element in " + registrar.key() + ": " + obj))
        );
        final Codec<T> compressed = ExtraCodecs.idResolverCodec(
            obj -> registrar.getKey(obj).isPresent() ? registrar.getRawId(obj) : -1,
            registrar::byRawId,
            -1
        );
        return ExtraCodecs.orCompressed(uncompressed, compressed);
    }

    public static <T> Codec<Optional<T>> optional(Codec<T> codec) {
        return Codec.either(Codec.EMPTY.codec(), codec).xmap(
            either -> either.map(u -> Optional.empty(), Optional::of),
            value -> value.<Either<Unit, T>>map(Either::right).orElseGet(() -> Either.left(Unit.INSTANCE))
        );
    }
}
