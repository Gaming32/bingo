package io.github.gaming32.bingo.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ThrownEgg.class)
public abstract class MixinThrownEgg extends ThrowableItemProjectile {
    public MixinThrownEgg(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyVariable(method = "onHit", at = @At(value = "STORE", ordinal = 0), name = "i")
    private int triggerOnInitializeLoopVariable(int i, @Local(name = "count") int count) {
        if (getOwner() instanceof ServerPlayer player) {
            BingoTriggers.CHICKEN_HATCH.get().trigger(player, (ThrownEgg) (Object) this, count);
        }
        return i;
    }
}
