package io.github.gaming32.bingo.subpredicates;

import com.mojang.serialization.Codec;
import io.github.gaming32.bingo.platform.registry.DeferredRegister;
import io.github.gaming32.bingo.platform.registry.RegistryValue;
import io.github.gaming32.bingo.util.Identifiers;
import net.minecraft.advancements.predicates.entity.EntitySubPredicate;
import net.minecraft.core.registries.BuiltInRegistries;

public class BingoEntitySubPredicates {
    private static final DeferredRegister<Codec<? extends EntitySubPredicate>> REGISTER =
        DeferredRegister.create(BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE);

    public static final RegistryValue<Codec<BingoPlayerPredicate>> PLAYER = register("type_specific/player", BingoPlayerPredicate.CODEC);
    public static final RegistryValue<Codec<ItemEntityPredicate>> ITEM = register("type_specific/item", ItemEntityPredicate.CODEC);
    public static final RegistryValue<Codec<PaintingPredicate>> PAINTING = register("type_specific/painting", PaintingPredicate.CODEC);

    public static void load() {
    }

    private static <P extends EntitySubPredicate> RegistryValue<Codec<P>> register(String name, Codec<P> codec) {
        return REGISTER.register(Identifiers.bingo(name), () -> codec);
    }
}
