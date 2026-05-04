package io.github.gaming32.bingo.mixin;

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
        LevelAccessor level, BlockState state, int outputStrength, BlockPos pos, int duration, Operation<Void> original,
        @Local(argsOnly = true) Entity projectileEntity
    ) {
        if (!(projectileEntity instanceof Projectile projectile) || !(projectile.getOwner() instanceof Player player)) {
            original.call(level, state, outputStrength, pos, duration);
            return;
        }
        try (
            final var _ = GlobalVars.CURRENT_PLAYER.push(player);
            final var _ = GlobalVars.CURRENT_PROJECTILE.push(projectile);
            final var _ = GlobalVars.CURRENT_BLOCK_POS.push(pos);
            final var _ = GlobalVars.CURRENT_REDSTONE_OUTPUT.push(outputStrength)
        ) {
            original.call(level, state, outputStrength, pos, duration);
        }
    }
}
