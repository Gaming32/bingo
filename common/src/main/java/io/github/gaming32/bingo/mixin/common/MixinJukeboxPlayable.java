package io.github.gaming32.bingo.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(JukeboxPlayable.class)
public class MixinJukeboxPlayable {
    @WrapOperation(
        method = "tryInsertIntoJukebox",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/entity/JukeboxBlockEntity;setTheItem(Lnet/minecraft/world/item/ItemStack;)V"
        )
    )
    private static void partyParrotsTrigger(
        JukeboxBlockEntity instance, ItemStack itemStack, Operation<Void> original,
        @Local(argsOnly = true) Player player
    ) {
        if (player instanceof ServerPlayer serverPlayer) {
            BingoTriggers.PARTY_PARROTS.get().trigger(serverPlayer, instance);
        }
    }
}
