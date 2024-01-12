package io.github.gaming32.bingo.mixin.common;

import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.world.inventory.GrindstoneMenu$4")
public class MixinGrindstoneMenu_4 {
    @Shadow(aliases = {"field_16780", "f_39618_"})
    @Final
    GrindstoneMenu this$0;
    @Shadow(aliases = {"field_16779", "f_39617_"})
    @Final
    ContainerLevelAccess val$access;

    @Inject(method = "onTake", at = @At("HEAD"))
    private void onUseGrindstone(Player player, ItemStack stack, CallbackInfo ci) {
        if (player instanceof ServerPlayer serverPlayer) {
            val$access.execute((level, blockPos) -> {
                BingoTriggers.USE_GRINDSTONE.get().trigger(serverPlayer, blockPos, this$0.repairSlots.getItem(0), this$0.repairSlots.getItem(1));
            });
        }
    }
}
