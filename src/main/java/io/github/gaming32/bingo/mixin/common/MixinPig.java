package io.github.gaming32.bingo.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
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
            target = "Lnet/minecraft/world/entity/animal/Pig;convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;"
        )
    )
    private Mob zombifyPigTrigger(
        Pig instance, EntityType<?> entityType, ConversionParams conversionParams, ConversionParams.AfterConversion<?> afterConversion,
        Operation<Mob> original,
        @Local(argsOnly = true) ServerLevel level,
        @Local(argsOnly = true) LightningBolt lightning
    ) {
        final var newEntity = original.call(instance, entityType, conversionParams, afterConversion);
        if (newEntity != null) {
            if (lightning.getCause() != null) {
                BingoTriggers.ZOMBIFY_PIG.get().trigger(lightning.getCause(), (Pig)(Object)this, newEntity, true);
            } else {
                for (ServerPlayer player : level.players()) {
                    if (player.distanceTo(lightning) < 256f) {
                        BingoTriggers.ZOMBIFY_PIG.get().trigger(player, (Pig)(Object)this, newEntity, false);
                    }
                }
            }
        }
        return newEntity;
    }
}
