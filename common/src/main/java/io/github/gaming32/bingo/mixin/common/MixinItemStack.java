package io.github.gaming32.bingo.mixin.common;

import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class MixinItemStack {
    @Inject(
        method = "lambda$hurtAndBreak$15",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"
        )
    )
    private void itemBrokenTrigger(LivingEntity livingEntity, EquipmentSlot equipmentSlot, CallbackInfo ci) {
        if (livingEntity instanceof ServerPlayer serverPlayer) {
            BingoTriggers.ITEM_BROKEN.get().trigger(serverPlayer, (ItemStack)(Object)this);
        }
    }
}
