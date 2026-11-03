package io.github.gaming32.bingo.mixin;

import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedBlock.class)
public class MixinBedBlock {
    @Inject(method = "destroyOnUse", at = @At("HEAD"))
    private void intentionalGameDesignTrigger(
        BlockState state, Level level, BlockPos pos, Player player, CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (player instanceof ServerPlayer serverPlayer) {
            BingoTriggers.INTENTIONAL_GAME_DESIGN.get().trigger(serverPlayer, pos);
        }
    }
}
