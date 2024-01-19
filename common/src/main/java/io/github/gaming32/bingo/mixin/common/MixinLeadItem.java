package io.github.gaming32.bingo.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.gaming32.bingo.ext.LeashFenceKnotEntityExt;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.LeadItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LeadItem.class)
public class MixinLeadItem {
    @WrapOperation(
        method = "bindPlayerMobs",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/decoration/LeashFenceKnotEntity;getOrCreateKnot(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/entity/decoration/LeashFenceKnotEntity;"
        )
    )
    private static LeashFenceKnotEntity setKnotOwner(
        Level level, BlockPos pos, Operation<LeashFenceKnotEntity> original,
        @Local Player player
    ) {
        final LeashFenceKnotEntity entity = original.call(level, pos);
        ((LeashFenceKnotEntityExt)entity).bingo$setOwner(player);
        return entity;
    }
}
