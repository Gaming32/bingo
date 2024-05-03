package io.github.gaming32.bingo.subpredicates.item;

import com.mojang.serialization.Codec;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.platform.BingoPlatform;
import io.github.gaming32.bingo.platform.registry.DeferredRegister;
import io.github.gaming32.bingo.platform.registry.RegistryValue;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class BingoItemSubPredicates {
    private static final DeferredRegister<ItemSubPredicate.Type<?>> REGISTER =
        BingoPlatform.platform.createDeferredRegister(BuiltInRegistries.ITEM_SUB_PREDICATE_TYPE);

    public static void load() {
    }

    private static <P extends ItemSubPredicate> RegistryValue<ItemSubPredicate.Type<P>> register(String name, Codec<P> codec) {
        return REGISTER.register(new ResourceLocation(Bingo.MOD_ID, name), () -> new ItemSubPredicate.Type<>(codec));
    }
}
