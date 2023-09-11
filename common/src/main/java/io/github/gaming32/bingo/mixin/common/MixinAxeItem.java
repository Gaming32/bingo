package io.github.gaming32.bingo.mixin.common;

import net.minecraft.world.item.AxeItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AxeItem.class)
public class MixinAxeItem {
//    @WrapOperation(
//        method = "useOn",
//        at = @At(
//            value = "INVOKE",
//            target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
//        )
//    )
//    private boolean mineralPillar(Level instance, BlockPos pos, BlockState newState, int flags, Operation<Boolean> original, @Local Player player) {
//        final boolean result = original.call(instance, pos, newState, flags);
//        if (result && player instanceof ServerPlayer serverPlayer) {
//            BingoTriggers.MINERAL_PILLAR.trigger(serverPlayer, instance, pos);
//        }
//        return result;
//    }
}
