package io.github.gaming32.bingo.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.gaming32.bingo.ext.ItemEntityExt;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Opcodes;
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

    @Shadow
    public abstract BlockPos getOnPosLegacy();

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
        method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At("RETURN")
    )
    private void setDroppedBy(ServerLevel level, ItemStack itemStack, float offset, CallbackInfoReturnable<ItemEntity> cir) {
        if (cir.getReturnValue() instanceof ItemEntityExt itemEntity) {
            itemEntity.bingo$setDroppedBy((Entity)(Object)this);
        }
    }

    @Inject(
        method = "awardKillScore",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/advancements/triggers/KilledTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;)V",
            shift = At.Shift.AFTER
        )
    )
    private void customTrigger(Entity victim, DamageSource killingBlow, CallbackInfo ci) {
        BingoTriggers.ENTITY_KILLED_PLAYER.get().trigger((ServerPlayer) victim, (Entity)(Object)this, killingBlow);
    }

    @Inject(method = "restituteMovementAfterCollisions(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;ZZLnet/minecraft/world/phys/Vec3;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/gameevent/GameEvent;BOUNCE:Lnet/minecraft/core/Holder$Reference;", opcode = Opcodes.GETSTATIC))
    private void onBounce(CallbackInfo ci, @Local(argsOnly = true, name = "effectPos") BlockPos effectPos) {
        if ((Object) this instanceof ServerPlayer player) {
            BingoTriggers.BOUNCE_ON_BLOCK.get().trigger(player, effectPos);
        }
    }
}
