package io.github.gaming32.bingo.mixin.common.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.client.BoardScreen;
import io.github.gaming32.bingo.client.config.BingoClientConfig;
import io.github.gaming32.bingo.client.config.BoardCorner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public class MixinGui {
    @Shadow @Final private Minecraft minecraft;

    @WrapOperation(
        method = "render",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderEffects(Lnet/minecraft/client/gui/GuiGraphics;)V")
    )
    private void moveEffects(Gui instance, GuiGraphics guiGraphics, Operation<Void> original) {
        final BingoClientConfig config = BingoClient.CONFIG;
        if (
            BingoClient.clientGame == null || config.getBoardCorner() != BoardCorner.UPPER_RIGHT ||
                minecraft.getDebugOverlay().showDebugScreen() || minecraft.screen instanceof BoardScreen
        ) {
            original.call(instance, guiGraphics);
            return;
        }

        final float scale = config.getBoardScale();
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(
            (-BingoClient.getBoardWidth() - BingoClient.BOARD_OFFSET) * scale,
            BingoClient.BOARD_OFFSET * scale, 0f
        );
        original.call(instance, guiGraphics);
        guiGraphics.pose().popPose();
    }
}
