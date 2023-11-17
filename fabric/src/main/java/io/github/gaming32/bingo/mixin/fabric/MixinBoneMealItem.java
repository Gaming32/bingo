package io.github.gaming32.bingo.mixin.fabric;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.gaming32.bingo.ext.GlobalVars;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BoneMealItem.class)
public class MixinBoneMealItem {
    @WrapOperation(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BoneMealItem;growCrop(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean wrapUseOn(ItemStack boneMeal, Level level, BlockPos pos, Operation<Boolean> operation, UseOnContext context) {
        final Player player = context.getPlayer();
        // J25, and we'll be able to use "var _ ="
        try (
            var ignored = GlobalVars.CURRENT_PLAYER.pushed(player instanceof FakePlayer ? null : player);
            var ignored1 = GlobalVars.CURRENT_ITEM.pushed(boneMeal)
        ) {
            return operation.call(boneMeal, level, pos);
        }
    }
}
