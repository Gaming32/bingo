package io.github.gaming32.bingo.mixin.neoforge;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.gaming32.bingo.ext.GlobalVars;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BoneMealItem.class)
public class MixinBoneMealItem {
    @WrapOperation(
        method = "applyBonemeal",
        remap = false,
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/BonemealableBlock;performBonemeal(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"
        )
    )
    private static void wrapApplyBoneMeal(
        BonemealableBlock instance,
        ServerLevel level,
        RandomSource random,
        BlockPos pos,
        BlockState state,
        Operation<Void> operation,
        @Local ItemStack boneMeal,
        @Local Player player
    ) {
        try (
            var ignored = GlobalVars.CURRENT_PLAYER.pushed(player instanceof FakePlayer ? null : player);
            var ignored1 = GlobalVars.CURRENT_ITEM.pushed(boneMeal)
        ) {
            operation.call(instance, level, random, pos, state);
        }
    }
}
