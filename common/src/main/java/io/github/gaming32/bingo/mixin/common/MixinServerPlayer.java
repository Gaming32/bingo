package io.github.gaming32.bingo.mixin.common;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.network.messages.s2c.ResyncStatesPayload;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends MixinPlayer {
    @Shadow @Final public ServerPlayerGameMode gameMode;

    @Inject(
        method = "doTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/game/ClientboundSetExperiencePacket;<init>(FII)V"
        )
    )
    private void experienceChanged(CallbackInfo ci) {
        BingoTriggers.EXPERIENCE_CHANGED.get().trigger((ServerPlayer)(Object)this);
    }

    @Inject(method = "onItemPickup", at = @At("TAIL"))
    private void itemPickedUpTrigger(ItemEntity itemEntity, CallbackInfo ci) {
        BingoTriggers.ITEM_PICKED_UP.get().trigger((ServerPlayer)(Object)this, itemEntity);
    }

    @Inject(method = "awardKillScore", at = @At("HEAD"))
    @SuppressWarnings("UnreachableCode")
    private void killSelfTrigger(Entity killed, int scoreValue, DamageSource source, CallbackInfo ci) {
        if (killed == (Object)this) {
            BingoTriggers.KILL_SELF.get().trigger((ServerPlayer)killed, source);
        }
    }

    @Inject(method = "die", at = @At("RETURN"))
    private void deathTrigger(DamageSource damageSource, CallbackInfo ci) {
        BingoTriggers.DEATH.get().trigger((ServerPlayer)(Object)this, damageSource);
    }

    @Inject(
        method = "checkMovementStatistics",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/stats/Stats;CROUCH_ONE_CM:Lnet/minecraft/resources/ResourceLocation;"
        )
    )
    @SuppressWarnings("UnreachableCode")
    private void sneakingTrigger(double distanceX, double distanceY, double distanceZ, CallbackInfo ci) {
        if ((Object)this instanceof ServerPlayer serverPlayer) {
            if (bingo$startSneakingPos == null) {
                Bingo.LOGGER.warn("bingo$startSneakingPos was null but player was sneaking");
            } else {
                BingoTriggers.CROUCH.get().trigger(serverPlayer, bingo$startSneakingPos);
            }
        }
    }

    @Inject(method = "setGameMode", at = @At("HEAD"))
    private void storeOldGameMode(
        GameType newMode, CallbackInfoReturnable<Boolean> cir,
        @Share("oldMode") LocalRef<GameType> oldMode
    ) {
        oldMode.set(gameMode.getGameModeForPlayer());
    }

    @Inject(method = "setGameMode", at = @At("RETURN"))
    private void onSetGameMode(
        GameType newMode, CallbackInfoReturnable<Boolean> cir,
        @Share("oldMode") LocalRef<GameType> oldMode
    ) {
        if (!cir.getReturnValueZ() || Bingo.activeGame == null) return;
        if (newMode == GameType.SPECTATOR || oldMode.get() == GameType.SPECTATOR) {
            final ServerPlayer player = (ServerPlayer)(Object)this;
            final BingoBoard.Teams team = Bingo.activeGame.getTeam(player);
            new ResyncStatesPayload(Bingo.activeGame.obfuscateTeam(team, player)).sendTo(player);
        }
    }
}
