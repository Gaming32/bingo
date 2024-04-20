package io.github.gaming32.bingo.mixin.fabric;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DyedItemColor.class)
public class MixinDyedItemColor {
    @WrapOperation(
        method = "applyDyes",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/tags/TagKey;)Z"
        )
    )
    private static boolean dyeableDuringDataGen(ItemStack instance, TagKey<Item> tag, Operation<Boolean> original) {
        // Internal, but how else are we supposed to check if datagen is active?
        if (FabricDataGenHelper.ENABLED) {
            return true;
        }
        return original.call(instance, tag);
    }
}
