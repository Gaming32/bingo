package io.github.gaming32.bingo.mixin.common;

import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VehicleEntity.class)
public abstract class MixinVehicleEntity extends Entity {
    public MixinVehicleEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
        method = "hurt",
        at = {
            @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/entity/vehicle/VehicleEntity;destroy(Lnet/minecraft/world/damagesource/DamageSource;)V"
            ),
            @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/entity/vehicle/VehicleEntity;discard()V"
            )
        }
    )
    private void onKilled(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (isRemoved()) return;
        if (source.getEntity() instanceof ServerPlayer player) {
            BingoTriggers.DESTROY_VEHICLE.get().trigger(player, (VehicleEntity)(Object)this, source);
        }
    }
}
