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

    @Shadow @Final
    private MinecraftServer server;
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
    private void itemPickedUpTrigger(ItemEntity entity, CallbackInfo ci) {
        BingoTriggers.ITEM_PICKED_UP.get().trigger((ServerPlayer)(Object)this, entity);
    }

    @Inject(method = "awardKillScore", at = @At("HEAD"))
    @SuppressWarnings("UnreachableCode")
    private void killSelfTrigger(Entity victim, DamageSource killingBlow, CallbackInfo ci) {
        if (victim == (Object)this) {
            BingoTriggers.KILL_SELF.get().trigger((ServerPlayer) victim, killingBlow);
        }
    }

    @Inject(method = "die", at = @At("RETURN"))
    private void deathTrigger(DamageSource source, CallbackInfo ci) {
        BingoTriggers.DEATH.get().trigger((ServerPlayer)(Object)this, source);
    }

    @Inject(
        method = "checkMovementStatistics",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/stats/Stats;CROUCH_ONE_CM:Lnet/minecraft/resources/Identifier;"
        )
    )  // todo: still don't know how to use opcodes here
    @SuppressWarnings("UnreachableCode")
    private void sneakingTrigger(double dx, double dy, double dz, CallbackInfo ci) {
        if ((Object)this instanceof ServerPlayer serverPlayer) {
            if (bingo$startSneakingPos == null) {
                Bingo.LOGGER.warn("bingo$startSneakingPos was null but player was sneaking");
            } else {
                BingoTriggers.CROUCH.get().trigger(serverPlayer, bingo$startSneakingPos);
            }
        }
    }

    @WrapMethod(method = "setGameMode")
    private boolean onSetGameMode(GameType mode, Operation<Boolean> original) {
        final var oldMode = gameMode.getGameModeForPlayer();
        if (!original.call(mode)) {
            return false;
        }
        final var game = ((MinecraftServerExt) server).bingo$getGame();
        if (game != null && (mode == GameType.SPECTATOR || oldMode == GameType.SPECTATOR)) {
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
