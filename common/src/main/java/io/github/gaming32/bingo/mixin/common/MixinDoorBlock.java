package io.github.gaming32.bingo.mixin.common;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.gaming32.bingo.ext.GlobalVars;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TargetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(DoorBlock.class)
public class MixinDoorBlock {
    @Shadow @Final public static BooleanProperty OPEN;

    @ModifyArg(
        method = "neighborChanged",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
        )
    )
    private BlockState onDoorOpened(BlockState state, @Local(argsOnly = true) Level level, @Local(ordinal = 0, argsOnly = true) BlockPos doorPos) {
        if (state.getValue(OPEN) && GlobalVars.CURRENT_PLAYER.get() instanceof ServerPlayer player) {
            final Projectile projectile = GlobalVars.CURRENT_PROJECTILE.get();
            final BlockPos targetPos = GlobalVars.CURRENT_BLOCK_POS.get();
            final Integer targetPower = GlobalVars.CURRENT_REDSTONE_OUTPUT.get();
            if (
                projectile != null &&
                    targetPos != null &&
                    targetPower != null &&
                    level.getBlockState(targetPos).getBlock() instanceof TargetBlock
            ) {
                BingoTriggers.DOOR_OPENED_BY_TARGET.get().trigger(player, projectile, targetPos, targetPower, doorPos);
            }
        }
        return state;
    }
}
