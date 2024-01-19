package io.github.gaming32.bingo.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.gaming32.bingo.ext.LeashFenceKnotEntityExt;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PathfinderMob.class)
public class MixinPathfinderMob extends Mob {
    protected MixinPathfinderMob(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(
        method = "tickLeash",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/PathfinderMob;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"
        )
    )
    private void pulledByLeashTrigger(
        PathfinderMob instance, Vec3 vec3, Operation<Void> original,
        @Local Entity leashHolder
    ) {
        final Vec3 force = vec3.subtract(getDeltaMovement());
        original.call(instance, vec3);
        if (!(bingo$getLeashPlayer(leashHolder) instanceof ServerPlayer serverPlayer)) return;
        BingoTriggers.PULLED_BY_LEASH.get().trigger(
            serverPlayer, instance, leashHolder instanceof LeashFenceKnotEntity knot ? knot : null, force
        );
    }

    @Unique
    @Nullable
    private static Player bingo$getLeashPlayer(Entity leashHolder) {
        if (leashHolder instanceof Player player) {
            return player;
        }
        if (leashHolder instanceof LeashFenceKnotEntityExt knot) {
            return knot.bingo$getOwner();
        }
        return null;
    }
}
