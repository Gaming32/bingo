package io.github.gaming32.bingo.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RangedCrossbowAttackGoal.class)
public class MixinRangedCrossbowAttackGoal {
    @WrapOperation(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/monster/RangedAttackMob;performRangedAttack(Lnet/minecraft/world/entity/LivingEntity;F)V"
        )
    )
    private void mobBrokeCrossbowTrigger(RangedAttackMob instance, LivingEntity target, float velocity, Operation<Void> original) {
        final LivingEntity mobEntity = (LivingEntity)instance;
        final ItemStack stack = mobEntity.getItemInHand(ProjectileUtil.getWeaponHoldingHand(mobEntity, Items.CROSSBOW));
        final boolean wasEmpty = stack.isEmpty();
        original.call(instance, target, velocity);
        if (!wasEmpty && stack.isEmpty() && target instanceof ServerPlayer serverPlayer) {
            BingoTriggers.MOB_BROKE_CROSSBOW.get().trigger(serverPlayer, mobEntity);
        }
    }
}
