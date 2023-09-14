package io.github.gaming32.bingo.mixin.common;

import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
    @Inject(
        method = "onEquipItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;doesEmitEquipEvent(Lnet/minecraft/world/entity/EquipmentSlot;)Z"
        )
    )
    private void onEquipItem(EquipmentSlot slot, ItemStack oldItem, ItemStack newItem, CallbackInfo ci) {
        //noinspection ConstantValue
        if ((Object)this instanceof ServerPlayer player) {
            BingoTriggers.EQUIP_ITEM.trigger(player, oldItem, newItem, slot);
        }
    }
    @Inject(
        method = "hurt",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;die(Lnet/minecraft/world/damagesource/DamageSource;)V"
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void onDeathFromDamageSource(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir, float originalAmount, boolean blocked) {
        BingoTriggers.ENTITY_DIE_NEAR_PLAYER.trigger((LivingEntity) (Object) this, source, originalAmount, amount, blocked);
    }
}
