package io.github.gaming32.bingo.mixin.common;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerExplosion.class)
public interface ServerExplosionAccessor {
    @Accessor
    DamageSource getDamageSource();
}
