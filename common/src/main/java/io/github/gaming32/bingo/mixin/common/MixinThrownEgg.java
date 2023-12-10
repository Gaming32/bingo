package io.github.gaming32.bingo.mixin.common;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ThrownEgg.class)
public abstract class MixinThrownEgg extends ThrowableItemProjectile {
    public MixinThrownEgg(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyVariable(method = "onHit", at = @At(value = "STORE", ordinal = 0), ordinal = 1)
    private int triggerOnInitializeLoopVariable(int loopVar, @Local(ordinal = 0) int numChickens) {
        if (getOwner() instanceof ServerPlayer player) {
            BingoTriggers.CHICKEN_HATCH.get().trigger(player, (ThrownEgg) (Object) this, numChickens);
        }
        return loopVar;
    }
}
