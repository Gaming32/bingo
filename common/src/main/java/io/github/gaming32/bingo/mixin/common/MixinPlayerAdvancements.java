package io.github.gaming32.bingo.mixin.common;

import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerAdvancements.class)
public class MixinPlayerAdvancements {
    @Shadow private boolean isFirstPacket;

    @Inject(
        method = "flushDirty",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V",
            shift = At.Shift.AFTER
        )
    )
    private void syncBingoAdvancements(ServerPlayer serverPlayer, CallbackInfo ci) {
        if (!isFirstPacket) return;
        final var game = serverPlayer.server.bingo$getGame();
        if (game != null) {
            game.syncAdvancementsTo(serverPlayer);
        }
    }
}
