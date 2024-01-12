package io.github.gaming32.bingo.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.gaming32.bingo.ext.GlobalVars;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.TargetBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TargetBlock.class)
public class MixinTargetBlock {
    @WrapOperation(
        method = "updateRedstoneOutput",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/TargetBlock;setOutputPower(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/level/block/state/BlockState;ILnet/minecraft/core/BlockPos;I)V"
        )
    )
    private static void preOutputPower(
        LevelAccessor level, BlockState state, int power, BlockPos pos, int waitTime, Operation<Void> original,
        @Local Entity projectileEntity
    ) {
        if (!(projectileEntity instanceof Projectile projectile) || !(projectile.getOwner() instanceof Player player)) {
            original.call(level, state, power, pos, waitTime);
            return;
        }
        try (
            final var ignored = GlobalVars.CURRENT_PLAYER.push(player);
            final var ignored1 = GlobalVars.CURRENT_PROJECTILE.push(projectile);
            final var ignored2 = GlobalVars.CURRENT_BLOCK_POS.push(pos);
            final var ignored3 = GlobalVars.CURRENT_REDSTONE_OUTPUT.push(power)
        ) {
            original.call(level, state, power, pos, waitTime);
        }
    }
}
