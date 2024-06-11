package io.github.gaming32.bingo.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Boat.class)
public class MixinBoat {
    @WrapOperation(
        method = "elasticRangeLeashBehaviour",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/vehicle/Boat;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"
        )
    )
    private void pulledByLeashTrigger(
        Boat instance, Vec3 vec3, Operation<Void> original, @Local(argsOnly = true) Entity leashHolder
    ) {
        BingoTriggers.PULLED_BY_LEASH.get().tryTrigger(instance, vec3, original, leashHolder);
    }
}
