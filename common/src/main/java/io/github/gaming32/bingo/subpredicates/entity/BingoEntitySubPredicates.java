package io.github.gaming32.bingo.subpredicates.entity;

import com.mojang.serialization.MapCodec;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.platform.BingoPlatform;
import io.github.gaming32.bingo.platform.registry.DeferredRegister;
import io.github.gaming32.bingo.platform.registry.RegistryValue;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class BingoEntitySubPredicates {
    private static final DeferredRegister<MapCodec<? extends EntitySubPredicate>> REGISTER =
        BingoPlatform.platform.createDeferredRegister(BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE);

    public static final RegistryValue<MapCodec<BingoPlayerPredicate>> PLAYER = register("player", BingoPlayerPredicate.CODEC);
    public static final RegistryValue<MapCodec<ItemEntityPredicate>> ITEM = register("item", ItemEntityPredicate.CODEC);

    public static void load() {
    }

    private static <P extends EntitySubPredicate> RegistryValue<MapCodec<P>> register(String name, MapCodec<P> codec) {
        return REGISTER.register(new ResourceLocation(Bingo.MOD_ID, name), () -> codec);
    }
}
