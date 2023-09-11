package io.github.gaming32.bingo.mixin.common;

import com.google.common.collect.ImmutableBiMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.gaming32.bingo.subpredicates.ItemEntityPredicate;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntitySubPredicate.Types.class)
public class MixinEntitySubPredicate_Types {
    @WrapOperation(
        method = "<clinit>",
        at = @At(
            value = "INVOKE",
            target = "Lcom/google/common/collect/ImmutableBiMap$Builder;buildOrThrow()Lcom/google/common/collect/ImmutableBiMap;",
            remap = false
        )
    )
    private static ImmutableBiMap<String, EntitySubPredicate.Type> addCustomSubPredicates(
        ImmutableBiMap.Builder<String, EntitySubPredicate.Type> instance,
        Operation<ImmutableBiMap<String, EntitySubPredicate.Type>> original
    ) {
        return original.call(instance
            .put("bingo:item", ItemEntityPredicate.TYPE)
        );
    }
}
