package io.github.gaming32.bingo.mixin.common;

import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class MixinServerPlayer {
    @Inject(
        method = "doTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/game/ClientboundSetExperiencePacket;<init>(FII)V"
        )
    )
    private void experienceChanged(CallbackInfo ci) {
        BingoTriggers.EXPERIENCE_CHANGED.trigger((ServerPlayer)(Object)this);
    }

    @Inject(method = "onItemPickup", at = @At("TAIL"))
    private void itemPickedUpTrigger(ItemEntity itemEntity, CallbackInfo ci) {
        BingoTriggers.ITEM_PICKED_UP.trigger((ServerPlayer)(Object)this, itemEntity);
    }

    @Inject(method = "awardKillScore", at = @At("HEAD"))
    private void killSelfTrigger(Entity killed, int scoreValue, DamageSource source, CallbackInfo ci) {
        if (killed == (Object)this) {
            BingoTriggers.KILL_SELF.trigger((ServerPlayer)killed, source);
        }
    }

    @Inject(method = "die", at = @At("RETURN"))
    private void deathTrigger(DamageSource damageSource, CallbackInfo ci) {
        BingoTriggers.DEATH.trigger((ServerPlayer)(Object)this, damageSource);
    }
}
