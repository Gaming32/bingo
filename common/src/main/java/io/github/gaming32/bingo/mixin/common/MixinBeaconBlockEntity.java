package io.github.gaming32.bingo.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BeaconBlockEntity.class)
public class MixinBeaconBlockEntity {
    @WrapOperation(
        method = "applyEffects",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"
        )
    )
    private static boolean beaconEffectTrigger(Player instance, MobEffectInstance effectInstance, Operation<Boolean> original) {
        if (instance instanceof ServerPlayer serverPlayer) {
            BingoTriggers.BEACON_EFFECT.get().trigger(serverPlayer, effectInstance);
        }
        return original.call(instance, effectInstance);
    }
}
