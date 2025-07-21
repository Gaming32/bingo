package io.github.gaming32.bingo.mixin.common;

import io.github.gaming32.bingo.ext.LivingEntityExt;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CombatTracker.class)
public class MixinCombatTracker {
    @Shadow @Final private LivingEntity mob;

    @Inject(method = "recordDamage", at = @At("HEAD"))
    private void onEntityDamage(DamageSource source, float damage, CallbackInfo ci) {
        ((LivingEntityExt) mob).bingo$recordDamage(source);
    }
}
