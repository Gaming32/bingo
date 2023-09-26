package io.github.gaming32.bingo.mixin.common.client;

import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.client.BoardCorner;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Gui.class)
public class MixinGui {
    @ModifyArg(
        method = "renderEffects",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"
        ),
        index = 2
    )
    private int moveEffectContainersDown(int original) {
        if (BingoClient.clientBoard != null && BingoClient.boardCorner == BoardCorner.UPPER_RIGHT) {
            return original + BingoClient.BOARD_HEIGHT + BingoClient.BOARD_OFFSET;
        }
        return original;
    }

    @ModifyVariable(method = "method_18620", at = @At("HEAD"), index = 3, argsOnly = true)
    private static int moveEffectsDown(int original) {
        if (BingoClient.clientBoard != null && BingoClient.boardCorner == BoardCorner.UPPER_RIGHT) {
            return original + BingoClient.BOARD_HEIGHT + BingoClient.BOARD_OFFSET;
        }
        return original;
    }
}
