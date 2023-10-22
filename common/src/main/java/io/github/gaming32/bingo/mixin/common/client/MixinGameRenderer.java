package io.github.gaming32.bingo.mixin.common.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.client.BingoClientConfig;
import io.github.gaming32.bingo.client.BoardCorner;
import io.github.gaming32.bingo.client.BoardScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Shadow @Final
    Minecraft minecraft;

    @WrapOperation(
        method = "render",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/ToastComponent;render(Lnet/minecraft/client/gui/GuiGraphics;)V")
    )
    private void moveToasts(ToastComponent instance, GuiGraphics guiGraphics, Operation<Void> original) {
        final BingoClientConfig.BoardConfig boardConfig = BingoClient.getConfig().board;
        if (
            BingoClient.clientGame == null || boardConfig.corner != BoardCorner.UPPER_RIGHT ||
                minecraft.getDebugOverlay().showDebugScreen() || minecraft.screen instanceof BoardScreen
        ) {
            original.call(instance, guiGraphics);
            return;
        }

        guiGraphics.enableScissor(
            0, 0,
            (int)(guiGraphics.guiWidth() - (BingoClient.getBoardWidth() + BingoClient.BOARD_OFFSET) * boardConfig.scale),
            guiGraphics.guiHeight()
        );
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(
            (-BingoClient.getBoardWidth() - BingoClient.BOARD_OFFSET) * boardConfig.scale,
            BingoClient.BOARD_OFFSET * boardConfig.scale, 0f
        );
        original.call(instance, guiGraphics);
        guiGraphics.pose().popPose();
        guiGraphics.disableScissor();
    }
}
