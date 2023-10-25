package io.github.gaming32.bingo.mixin.common;

import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.SlimeBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({BedBlock.class, SlimeBlock.class})
public class BounceOnBlockMixin {
    @Inject(
        method = {"bounceUp", "method_21847", "method_21838"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(DDD)V",
            remap = true
        ),
        remap = false
    )
    @SuppressWarnings("deprecation")
    private void bounceUpTrigger(Entity entity, CallbackInfo ci) {
        if (entity instanceof ServerPlayer player && player.getDeltaMovement().y <= -0.1) {
            BingoTriggers.BOUNCE_ON_BLOCK.trigger(player, player.getOnPosLegacy());
        }
    }
}
