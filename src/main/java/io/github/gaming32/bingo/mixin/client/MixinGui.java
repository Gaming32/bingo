package io.github.gaming32.bingo.mixin.client;

import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.client.BoardScreen;
import io.github.gaming32.bingo.client.config.BoardCorner;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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

    @Inject(method = "extractEffects", at = @At("HEAD"))
    private void moveEffectsPre(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (bingo$effectsNeedMoving()) {
            final float scale = BingoClient.CONFIG.getBoardScale();
            graphics.pose().pushMatrix();
            graphics.pose().translate(
                (-BingoClient.getBoardWidth() - BingoClient.BOARD_OFFSET) * scale,
                BingoClient.BOARD_OFFSET * scale
            );
        }
    }

    @Inject(method = "extractEffects", at = @At("RETURN"))
    private void moveEffectsPost(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (bingo$effectsNeedMoving()) {
            graphics.pose().popMatrix();
        }
    }

    @Unique
    private boolean bingo$effectsNeedMoving() {
        return BingoClient.clientGame != null && BingoClient.CONFIG.getBoardCorner() == BoardCorner.UPPER_RIGHT &&
               !minecraft.getDebugOverlay().showDebugScreen() && !(minecraft.screen instanceof BoardScreen);
    }
}
