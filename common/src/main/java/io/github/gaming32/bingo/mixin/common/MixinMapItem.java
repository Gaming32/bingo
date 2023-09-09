package io.github.gaming32.bingo.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MapItem.class)
public class MixinMapItem {
    @WrapOperation(
        method = "update",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/saveddata/maps/MapItemSavedData;updateColor(IIB)Z"
        )
    )
    private boolean onUpdateColor(
        MapItemSavedData instance, int x, int z, byte color,
        Operation<Boolean> operation, @Share("updated") LocalBooleanRef updated
    ) {
        final boolean result = operation.call(instance, x, z, color);
        if (result) {
            updated.set(true);
        }
        return result;
    }

    @Inject(method = "update", at = @At("TAIL"))
    private void checkComplete(
        Level level, Entity viewer, MapItemSavedData data,
        CallbackInfo ci, @Share("updated") LocalBooleanRef updated
    ) {
        if (!updated.get() || !(viewer instanceof ServerPlayer player)) return;
        for (final byte b : data.colors) {
            if (b == 0) return;
        }
        BingoTriggers.COMPLETED_MAP.trigger(player, data);
    }
}
