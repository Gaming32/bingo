package io.github.gaming32.bingo.subpredicates;

import com.mojang.serialization.MapCodec;
import io.github.gaming32.bingo.platform.BingoPlatform;
import io.github.gaming32.bingo.platform.registry.DeferredRegister;
import io.github.gaming32.bingo.platform.registry.RegistryValue;
import io.github.gaming32.bingo.util.ResourceLocations;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.registries.BuiltInRegistries;

public class BingoEntitySubPredicates {
    private static final DeferredRegister<MapCodec<? extends EntitySubPredicate>> REGISTER =
        BingoPlatform.platform.createDeferredRegister(BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE);

    public static final RegistryValue<MapCodec<BingoPlayerPredicate>> PLAYER = register("player", BingoPlayerPredicate.CODEC);
    public static final RegistryValue<MapCodec<ItemEntityPredicate>> ITEM = register("item", ItemEntityPredicate.CODEC);
    public static final RegistryValue<MapCodec<PaintingPredicate>> PAINTING = register("painting", PaintingPredicate.CODEC);

    public static void load() {
    }

    private static <P extends EntitySubPredicate> RegistryValue<MapCodec<P>> register(String name, MapCodec<P> codec) {
        return REGISTER.register(ResourceLocations.bingo(name), () -> codec);
    }
}
