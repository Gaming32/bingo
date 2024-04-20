package io.github.gaming32.bingo.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Either;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedBlock.class)
public class MixinBedBlock {
    @Inject(method = "setPlacedBy", at = @At("RETURN"))
    private void onPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack, CallbackInfo ci) {
        if (placer instanceof ServerPlayer player) {
            BingoTriggers.BED_ROW.get().trigger(player, level, pos);
        }
    }

    @Inject(
        method = "useWithoutItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"
        )
    )
    private void intentionalGameDesignTrigger(
        BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult,
        CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (player instanceof ServerPlayer serverPlayer) {
            BingoTriggers.INTENTIONAL_GAME_DESIGN.get().trigger(serverPlayer, blockPos);
        }
    }

    @WrapOperation(
        method = "useWithoutItem",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;startSleepInBed(Lnet/minecraft/core/BlockPos;)Lcom/mojang/datafixers/util/Either;"
        )
    )
    private Either<Player.BedSleepingProblem, Unit> sleptTrigger(
        Player instance, BlockPos bedPos,
        Operation<Either<Player.BedSleepingProblem, Unit>> original,
        @Local(argsOnly = true) BlockState state
    ) {
        final var result = original.call(instance, bedPos);
        if (!(instance instanceof ServerPlayer serverPlayer)) {
            return result;
        }
        return result.ifRight(unit -> BingoTriggers.SLEPT.get().trigger(
            // useWithoutItem is only ever called with the MAIN_HAND
            serverPlayer, bedPos, instance.getItemInHand(InteractionHand.MAIN_HAND)
        ));
    }
}
