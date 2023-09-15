package io.github.gaming32.bingo.mixin.common;

import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class MixinBlock {
    @Inject(method = "playerWillDestroy", at = @At("RETURN"))
    private void onPlayerWillDestroy(Level level, BlockPos pos, BlockState state, Player player, CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer) {
            BingoTriggers.BREAK_BLOCK.trigger(serverPlayer, pos, player.getMainHandItem());
        }
    }
}
