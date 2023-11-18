package io.github.gaming32.bingo.data.subs;

import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import io.github.gaming32.bingo.Bingo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public interface BingoSubType<S extends BingoSub> {
    ResourceKey<Registry<BingoSubType<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(
        new ResourceLocation("bingo:bingo_sub_type")
    );
    Registrar<BingoSubType<?>> REGISTRAR = Bingo.REGISTRAR_MANAGER
        .<BingoSubType<?>>builder(REGISTRY_KEY.location())
        .build();

    RegistrySupplier<BingoSubType<SubBingoSub>> SUB = register("sub", SubBingoSub.CODEC);
    RegistrySupplier<BingoSubType<IntBingoSub>> INT = register("int", IntBingoSub.CODEC);
    RegistrySupplier<BingoSubType<WrapBingoSub>> WRAP = register("wrap", WrapBingoSub.CODEC);
    RegistrySupplier<BingoSubType<CompoundBingoSub>> COMPOUND = register("compound", CompoundBingoSub.CODEC);

    Codec<S> codec();

    static <S extends BingoSub> RegistrySupplier<BingoSubType<S>> register(String id, Codec<S> codec) {
        if (id.indexOf(':') < 0) {
            id = "bingo:" + id;
        }
        final ResourceLocation location = new ResourceLocation(id);
        return REGISTRAR.register(location, () -> new BingoSubType<>() {
            @Override
            public Codec<S> codec() {
                return codec;
            }

            @Override
            public String toString() {
                return "BingoSubType[" + location + "]";
            }
        });
    }

    static void load() {
    }
}
