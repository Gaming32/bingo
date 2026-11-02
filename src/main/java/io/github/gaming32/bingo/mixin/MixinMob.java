package io.github.gaming32.bingo.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MixinMob {
    @Inject(
        method = "convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z", shift = At.Shift.AFTER)
    )
    private <T extends Mob> void onConvert(
        CallbackInfoReturnable<T> cir,
        @Local(name = "serverLevel") ServerLevel serverLevel,
        @Local(name = "newMob") T newMob
    ) {
        Mob self = (Mob) (Object) this;
        for (ServerPlayer player : serverLevel.players()) {
            if (player.distanceToSqr(self) <= 16 * 16) {
                BingoTriggers.MOB_CONVERTED.get().trigger(player, self, newMob);
            }
        }
    }
}
