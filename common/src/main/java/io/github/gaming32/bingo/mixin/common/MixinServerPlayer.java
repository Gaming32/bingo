package io.github.gaming32.bingo.mixin.common;

import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerPlayer;
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
}
