package io.github.gaming32.bingo.data.subs;

import com.mojang.serialization.MapCodec;
import io.github.gaming32.bingo.data.BingoRegistries;
import io.github.gaming32.bingo.platform.registry.DeferredRegister;
import io.github.gaming32.bingo.platform.registry.RegistryBuilder;
import io.github.gaming32.bingo.platform.registry.RegistryValue;

public interface BingoSubType<S extends BingoSub> {
    DeferredRegister<BingoSubType<?>> REGISTER = new RegistryBuilder<>(BingoRegistries.SUB_TYPE)
        .build();

    RegistryValue<BingoSubType<CompoundBingoSub>> COMPOUND = register("compound", CompoundBingoSub.CODEC);
    RegistryValue<BingoSubType<IntBingoSub>> INT = register("int", IntBingoSub.CODEC);
    RegistryValue<BingoSubType<SubBingoSub>> SUB = register("sub", SubBingoSub.CODEC);
    RegistryValue<BingoSubType<WrapBingoSub>> WRAP = register("wrap", WrapBingoSub.CODEC);

    MapCodec<S> codec();

    private static <S extends BingoSub> RegistryValue<BingoSubType<S>> register(String id, MapCodec<S> codec) {
        return REGISTER.register(id, () -> new BingoSubType<>() {
            @Override
            public MapCodec<S> codec() {
                return codec;
            }

            @Override
            public String toString() {
                return "BingoSubType[" + id + "]";
            }
        });
    }

    static void load() {
    }
}
