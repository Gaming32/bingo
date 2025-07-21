package io.github.gaming32.bingo.mixin.common;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.ext.MinecraftServerExt;
import io.github.gaming32.bingo.ext.ServerPlayerExt;
import io.github.gaming32.bingo.game.BingoBoard;
import io.github.gaming32.bingo.network.messages.s2c.ResyncStatesPayload;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends MixinEntity implements ServerPlayerExt {
    @Shadow @Final public ServerPlayerGameMode gameMode;

    @Shadow @Final public MinecraftServer server;
    @Unique
    private boolean bingo$advancementsNeedClearing;

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
    private void killSelfTrigger(Entity killed, DamageSource source, CallbackInfo ci) {
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

    @WrapMethod(method = "setGameMode")
    private boolean onSetGameMode(GameType newMode, Operation<Boolean> original) {
        final var oldMode = gameMode.getGameModeForPlayer();
        if (!original.call(newMode)) {
            return false;
        }
        final var game = ((MinecraftServerExt) server).bingo$getGame();
        if (game != null && (newMode == GameType.SPECTATOR || oldMode == GameType.SPECTATOR)) {
            final ServerPlayer player = (ServerPlayer)(Object)this;
            final BingoBoard.Teams team = game.getTeam(player);
            new ResyncStatesPayload(game.obfuscateTeam(team, player)).sendTo(player);
        }
        return true;
    }

    @Override
    public void bingo$markAdvancementsNeedClearing() {
        bingo$advancementsNeedClearing = true;
    }

    @Override
    public boolean bingo$clearAdvancementsNeedClearing() {
        final var result = bingo$advancementsNeedClearing;
        bingo$advancementsNeedClearing = false;
        return result;
    }

    @Override
    public void bingo$copyAdvancementsNeedClearingTo(ServerPlayer toPlayer) {
        ((MixinServerPlayer)(ServerPlayerExt)toPlayer).bingo$advancementsNeedClearing = bingo$advancementsNeedClearing;
    }
}
