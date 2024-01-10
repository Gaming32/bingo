package io.github.gaming32.bingo.mixin.common;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.item.HangingEntityItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HangingEntityItem.class)
public class MixinHangingEntityItem {
    @Inject(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private void onPlaceEntity(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir, @Local HangingEntity entity) {
        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        if (entity instanceof Painting painting) {
            BingoTriggers.ADJACENT_PAINTING.get().trigger(player, painting);
        }
    }
}
