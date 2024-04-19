package io.github.gaming32.bingo.subpredicates;

import com.mojang.serialization.MapCodec;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.platform.BingoPlatform;
import io.github.gaming32.bingo.platform.registry.DeferredRegister;
import io.github.gaming32.bingo.platform.registry.RegistryValue;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class BingoSubPredicates {
    private static final DeferredRegister<MapCodec<? extends EntitySubPredicate>> ENTITY_REGISTER =
        BingoPlatform.platform.createDeferredRegister(BuiltInRegistries.ENTITY_SUB_PREDICATE_TYPE);

    public static final RegistryValue<MapCodec<BingoPlayerPredicate>> PLAYER = registerEntity("player", BingoPlayerPredicate.CODEC);
    public static final RegistryValue<MapCodec<ItemEntityPredicate>> ITEM = registerEntity("item", ItemEntityPredicate.CODEC);

    public static void load() {
    }

    private static <P extends EntitySubPredicate> RegistryValue<MapCodec<P>> registerEntity(String name, MapCodec<P> codec) {
        return ENTITY_REGISTER.register(new ResourceLocation(Bingo.MOD_ID, name), () -> codec);
    }
}
