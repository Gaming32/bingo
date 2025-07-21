package io.github.gaming32.bingo.mixin.fabric;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import io.github.gaming32.bingo.fabric.event.FabricEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerLevel.class)
public class MixinServerLevel {
    @WrapWithCondition(
        method = "explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/core/Holder;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/ServerExplosion;explode()V"
        )
    )
    private boolean onExplosion(ServerExplosion instance) {
        FabricEvents.SERVER_EXPLOSION.invoker().accept((ServerLevel)(Object)this, instance);
        return true;
    }
}
