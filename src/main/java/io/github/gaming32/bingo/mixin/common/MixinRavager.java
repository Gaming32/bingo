package io.github.gaming32.bingo.mixin.common;

import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Ravager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Ravager.class)
public class MixinRavager {
    @Inject(
        method = "blockedByItem",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/world/entity/monster/Ravager;stunnedTick:I"
        )
    )
    private void stunnedRavager(LivingEntity defender, CallbackInfo ci) {
        if (defender instanceof ServerPlayer player) {
            BingoTriggers.STUN_RAVAGER.get().trigger(player, (Entity)(Object)this);
        }
    }
}
