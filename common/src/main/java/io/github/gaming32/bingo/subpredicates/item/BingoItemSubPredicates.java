package io.github.gaming32.bingo.subpredicates.item;

import com.mojang.serialization.Codec;
import io.github.gaming32.bingo.platform.BingoPlatform;
import io.github.gaming32.bingo.platform.registry.DeferredRegister;
import io.github.gaming32.bingo.platform.registry.RegistryValue;
import io.github.gaming32.bingo.util.ResourceLocations;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.registries.BuiltInRegistries;

public class BingoItemSubPredicates {
    private static final DeferredRegister<ItemSubPredicate.Type<?>> REGISTER =
        BingoPlatform.platform.createDeferredRegister(BuiltInRegistries.ITEM_SUB_PREDICATE_TYPE);

    public static void load() {
    }

    private static <P extends ItemSubPredicate> RegistryValue<ItemSubPredicate.Type<P>> register(String name, Codec<P> codec) {
        return REGISTER.register(ResourceLocations.bingo(name), () -> new ItemSubPredicate.Type<>(codec));
    }
}
