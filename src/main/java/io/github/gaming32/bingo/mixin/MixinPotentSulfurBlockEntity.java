package io.github.gaming32.bingo.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.PotentSulfurBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PotentSulfurBlockEntity.class)
public class MixinPotentSulfurBlockEntity {
    @Inject(method = "lambda$static$5", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;addDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    private static void onLaunchEntity(CallbackInfo ci, @Local(argsOnly = true, name = "pos") BlockPos pos, @Local(name = "entityToBeLaunched") Entity entityToBeLaunched) {
        if (entityToBeLaunched instanceof ServerPlayer player) {
            BingoTriggers.LAUNCHED_BY_GEYSER.get().trigger(player, pos);
        }
    }
}
