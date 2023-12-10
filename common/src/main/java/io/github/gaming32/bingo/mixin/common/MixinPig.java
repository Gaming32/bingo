package io.github.gaming32.bingo.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Pig.class)
public abstract class MixinPig extends Animal {
    protected MixinPig(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(
        method = "thunderHit",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"
        )
    )
    private boolean zombifyPigTrigger(ServerLevel instance, Entity entity, Operation<Boolean> original, @Local LightningBolt lightning) {
        final boolean result = original.call(instance, entity);
        if (result) {
            if (lightning.getCause() != null) {
                BingoTriggers.ZOMBIFY_PIG.get().trigger(lightning.getCause(), (Pig)(Object)this, entity, true);
            } else {
                for (ServerPlayer player : instance.players()) {
                    if (player.distanceTo(lightning) < 256f) {
                        BingoTriggers.ZOMBIFY_PIG.get().trigger(player, (Pig)(Object)this, entity, false);
                    }
                }
            }
        }
        return result;
    }
}
