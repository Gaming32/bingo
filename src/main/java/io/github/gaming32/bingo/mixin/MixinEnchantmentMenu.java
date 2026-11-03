package io.github.gaming32.bingo.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentMenu.class)
public class MixinEnchantmentMenu {
    @Shadow @Final public int[] costs;

    // The costs get recalculated (to 0) before the vanilla trigger if the last of the lapis is used up, so capture it beforehand
    @Inject(method = "lambda$clickMenuButton$0", at = @At("HEAD"))
    private void captureCost(ItemStack itemStack, int buttonId, Player player, int enchantmentCost, ItemStack currency, Level level, BlockPos pos, CallbackInfo ci, @Share("levelsRequired") LocalIntRef levelsRequired) {
        levelsRequired.set(costs[buttonId]);
    }

    @Inject(
        method = "lambda$clickMenuButton$0",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/advancements/triggers/EnchantedItemTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;I)V",
            shift = At.Shift.AFTER
        )
    )
    private void customEnchantTrigger(ItemStack itemStack, int buttonId, Player player, int enchantmentCost, ItemStack currency, Level level, BlockPos pos, CallbackInfo ci, @Share("levelsRequired") LocalIntRef levelsRequired) {
        BingoTriggers.ENCHANTED_ITEM.get().trigger((ServerPlayer)player, enchantmentCost, levelsRequired.get());
    }
}
