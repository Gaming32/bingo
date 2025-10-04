package io.github.gaming32.bingo.mixin.common;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShelfBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShelfBlock.class)
public class MixinShelfBlock {
    @Definition(id = "swapItemNoUpdate", method = "Lnet/minecraft/world/level/block/entity/ShelfBlockEntity;swapItemNoUpdate(ILnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;")
    @Definition(id = "hotbarStack", local = @Local(type = ItemStack.class, ordinal = 0))
    @Definition(id = "shelfStack", local = @Local(type = ItemStack.class, ordinal = 1))
    @Expression("shelfStack = ?.swapItemNoUpdate(?, hotbarStack)")
    @Inject(method = "swapHotbar", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private void countHotbarSwap(
        CallbackInfoReturnable<Boolean> cir,
        @Local(ordinal = 0) ItemStack hotbarStack,
        @Local(ordinal = 1) ItemStack shelfStack,
        @Share("count") LocalIntRef count
    ) {
        if (!hotbarStack.isEmpty() && !shelfStack.isEmpty()) {
            count.set(count.get() + 1);
        }
    }

    @Inject(method = "swapHotbar", at = @At("RETURN"))
    private void onHotbarSwapped(CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) Inventory inventory, @Share("count") LocalIntRef count) {
        if (count.get() == Inventory.SELECTION_SIZE && inventory.player instanceof ServerPlayer player) {
            BingoTriggers.HOTBAR_SWAPPED_WITH_SHELF.get().trigger(player);
        }
    }
}
