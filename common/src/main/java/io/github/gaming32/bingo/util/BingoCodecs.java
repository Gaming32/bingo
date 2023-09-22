package io.github.gaming32.bingo.util;

import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import dev.architectury.registry.registries.Registrar;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

import java.util.Optional;

public final class BingoCodecs {
    private BingoCodecs() {
    }

    public static final Codec<MinMaxBounds.Ints> INTS = ExtraCodecs.JSON.comapFlatMap(
        json -> {
            try {
                return DataResult.success(MinMaxBounds.Ints.fromJson(json));
            } catch (JsonSyntaxException e) {
                return DataResult.error(e::getMessage);
            }
        },
        MinMaxBounds.Ints::serializeToJson
    );

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
}
