package io.github.gaming32.bingo.mixin.common;

import io.github.gaming32.bingo.ext.ItemEntityExt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow public abstract Vec3 position();

    @Shadow public abstract boolean hasPose(Pose pose);

    @Unique
    @Nullable
    protected Vec3 bingo$startSneakingPos;

    @Inject(method = "setPose", at = @At("HEAD"))
    private void trackSneakingStart(Pose pose, CallbackInfo ci) {
        if (pose == Pose.CROUCHING) {
            if (!hasPose(Pose.CROUCHING)) {
                bingo$startSneakingPos = position();
            }
        } else {
            bingo$startSneakingPos = null;
        }
    }

    @Inject(
        method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At("RETURN")
    )
    private void setDroppedBy(ItemStack stack, float offsetY, CallbackInfoReturnable<ItemEntity> cir) {
        if (cir.getReturnValue() instanceof ItemEntityExt itemEntity) {
            itemEntity.bingo$setDroppedBy((Entity)(Object)this);
        }
    }
}
