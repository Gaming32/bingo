package io.github.gaming32.bingo.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(ConduitBlockEntity.class)
public class MixinConduitBlockEntity {
    @WrapOperation(
        method = "serverTick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/ConduitBlockEntity;updateShape(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Ljava/util/List;)Z"
        )
    )
    private static boolean powerConduitTrigger(Level level, BlockPos pos, List<BlockPos> positions, Operation<Boolean> original) {
        final int oldLevel = positions.size() / 7;
        final boolean result = original.call(level, pos, positions);
        final int newLevel = positions.size() / 7;
        if (result && newLevel != oldLevel) {
            for (ServerPlayer serverplayer : level.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(10))) {
                BingoTriggers.POWER_CONDUIT.get().trigger(serverplayer, newLevel);
            }
        }
        return result;
    }
}
