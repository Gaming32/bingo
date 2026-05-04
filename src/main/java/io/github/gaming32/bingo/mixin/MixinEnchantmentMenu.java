package io.github.gaming32.bingo.mixin;

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

    @Inject(
        method = "lambda$clickMenuButton$0",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/advancements/criterion/EnchantedItemTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;I)V",
            shift = At.Shift.AFTER
        )
    )
    private void customEnchantTrigger(ItemStack itemStack, int buttonId, Player player, int enchantmentCost, ItemStack currency, Level level, BlockPos pos, CallbackInfo ci) {
        BingoTriggers.ENCHANTED_ITEM.get().trigger((ServerPlayer)player, enchantmentCost, costs[buttonId]);
    }
}
