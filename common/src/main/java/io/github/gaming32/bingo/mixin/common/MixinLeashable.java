package io.github.gaming32.bingo.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Leashable.class)
public interface MixinLeashable {
    @WrapOperation(
        method = "legacyElasticRangeLeashBehaviour",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"
        )
    )
    private static void pulledByLeashTrigger(
        Entity entity, Vec3 deltaMovement, Operation<Void> original,
        @Local(ordinal = 1, argsOnly = true) Entity leashHolder
    ) {
        BingoTriggers.PULLED_BY_LEASH.get().tryTrigger(entity, deltaMovement, original, leashHolder);
    }
}
