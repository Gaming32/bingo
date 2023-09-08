package io.github.gaming32.bingo.mixin.common;

import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.level.ServerPlayer$2")
public class MixinServerPlayContainerListener {
    @Shadow(aliases = {"field_29183", "f_143458_"}, remap = false)
    @Final
    private ServerPlayer this$0;

    @Inject(
        method = "slotChanged",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/advancements/CriteriaTriggers;INVENTORY_CHANGED:Lnet/minecraft/advancements/critereon/InventoryChangeTrigger;"
        )
    )
    private void onInventoryChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack, CallbackInfo ci) {
        BingoTriggers.HAS_SOME_ITEMS_FROM_TAG.trigger(this$0, this$0.getInventory());
    }
}
