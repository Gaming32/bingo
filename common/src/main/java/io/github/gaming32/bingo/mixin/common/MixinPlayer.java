package io.github.gaming32.bingo.mixin.common;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.ext.ItemEntityExt;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class MixinPlayer extends MixinEntity {
    @Inject(
        method = "checkMovementStatistics",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/stats/Stats;CROUCH_ONE_CM:Lnet/minecraft/resources/ResourceLocation;"
        )
    )
    private void sneakingTrigger(double distanceX, double distanceY, double distanceZ, CallbackInfo ci) {
        //noinspection ConstantValue
        if ((Object)this instanceof ServerPlayer serverPlayer) {
            if (bingo$startSneakingPos == null) {
                Bingo.LOGGER.warn("bingo$startSneakingPos was null but player was sneaking");
            } else {
                BingoTriggers.CROUCH.trigger(serverPlayer, bingo$startSneakingPos);
            }
        }
    }

    @Inject(
        method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At("RETURN")
    )
    private void setDroppedBy(ItemStack droppedItem, boolean dropAround, boolean includeThrowerName, CallbackInfoReturnable<ItemEntity> cir) {
        if (cir.getReturnValue() instanceof ItemEntityExt itemEntity) {
            itemEntity.bingo$setDroppedBy((Entity)(Object)this);
        }
    }
}
