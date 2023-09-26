package io.github.gaming32.bingo.mixin.common;

import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MilkBucketItem.class)
public class MixinMilkBucketItem {
    @Inject(method = "finishUsingItem", at = @At("HEAD"))
    private void triggerMilkBucketFinishBecauseForge(ItemStack stack, Level level, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir) {
        // Forge moved the curing to before the trigger, so we need our own trigger to go before *that*
        // https://github.com/MinecraftForge/MinecraftForge/blob/1.20.x/patches/minecraft/net/minecraft/world/item/MilkBucketItem.java.patch
        // https://github.com/neoforged/NeoForge/issues/151
        if (livingEntity instanceof ServerPlayer serverPlayer) {
            BingoTriggers.CONSUME_MILK_BUCKET.trigger(serverPlayer, stack);
        }
    }
}
