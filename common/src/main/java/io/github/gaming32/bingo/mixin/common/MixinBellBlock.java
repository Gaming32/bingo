package io.github.gaming32.bingo.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.gaming32.bingo.ext.GlobalVars;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BellBlock.class)
public class MixinBellBlock {
    @WrapOperation(method = "onProjectileHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/BellBlock;onHit(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/phys/BlockHitResult;Lnet/minecraft/world/entity/player/Player;Z)Z"))
    private boolean onProjectileHit(BellBlock instance, Level level, BlockState state, BlockHitResult hit, Player player, boolean canRingBell, Operation<Boolean> original, @Local(argsOnly = true) Projectile projectile) {
        try (var ignored = GlobalVars.CURRENT_PROJECTILE.push(projectile)) {
            return original.call(instance, level, state, hit, player, canRingBell);
        }
    }

    @Inject(method = "onHit", at = @At(value = "FIELD", target = "Lnet/minecraft/stats/Stats;BELL_RING:Lnet/minecraft/resources/ResourceLocation;"))
    private void onBellRing(Level level, BlockState state, BlockHitResult hit, Player player, boolean canRingBell, CallbackInfoReturnable<Boolean> cir) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        Projectile projectile = GlobalVars.CURRENT_PROJECTILE.get();
        if (projectile != null) {
            BingoTriggers.SHOOT_BELL.get().trigger(serverPlayer, hit.getBlockPos(), projectile);
        }
    }
}
