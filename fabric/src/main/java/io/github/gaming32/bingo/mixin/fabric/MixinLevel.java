package io.github.gaming32.bingo.mixin.fabric;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.gaming32.bingo.fabric.FabricEvents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class MixinLevel {
    @Inject(
        method = "explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;ZLnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/sounds/SoundEvent;)Lnet/minecraft/world/level/Explosion;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Explosion;explode()V"
        )
    )
    private void onExplosion(
        Entity entity,
        DamageSource damageSource,
        ExplosionDamageCalculator explosionDamageCalculator,
        double d,
        double e,
        double f,
        float g,
        boolean bl,
        Level.ExplosionInteraction explosionInteraction,
        boolean bl2,
        ParticleOptions particleOptions,
        ParticleOptions particleOptions2,
        SoundEvent soundEvent,
        CallbackInfoReturnable<Explosion> cir,
        @Local Explosion explosion
    ) {
        FabricEvents.EXPLOSION.invoker().accept((Level)(Object)this, explosion);
    }
}
