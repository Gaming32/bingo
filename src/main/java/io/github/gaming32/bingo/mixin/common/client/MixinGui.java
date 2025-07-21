package io.github.gaming32.bingo.mixin.common.client;

import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.client.BoardScreen;
import io.github.gaming32.bingo.client.config.BoardCorner;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class MixinGui {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "renderEffects", at = @At("HEAD"))
    private void moveEffectsPre(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (bingo$effectsNeedMoving()) {
            final float scale = BingoClient.CONFIG.getBoardScale();
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(
                (-BingoClient.getBoardWidth() - BingoClient.BOARD_OFFSET) * scale,
                BingoClient.BOARD_OFFSET * scale
            );
        }
    }

    @Inject(method = "renderEffects", at = @At("RETURN"))
    private void moveEffectsPost(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (bingo$effectsNeedMoving()) {
            guiGraphics.pose().popMatrix();
        }
    }

    @Unique
    private boolean bingo$effectsNeedMoving() {
        return BingoClient.clientGame != null && BingoClient.CONFIG.getBoardCorner() == BoardCorner.UPPER_RIGHT &&
               !minecraft.getDebugOverlay().showDebugScreen() && !(minecraft.screen instanceof BoardScreen);
    }
}
