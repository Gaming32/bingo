package io.github.gaming32.bingo.mixin.common.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.client.BoardScreen;
import io.github.gaming32.bingo.client.config.BingoClientConfig;
import io.github.gaming32.bingo.client.config.BoardCorner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Shadow @Final
    private Minecraft minecraft;

    @WrapOperation(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/toasts/ToastManager;render(Lnet/minecraft/client/gui/GuiGraphics;)V"
        )
    )
    private void moveToasts(ToastManager instance, GuiGraphics graphics, Operation<Void> original) {
        final BingoClientConfig config = BingoClient.CONFIG;
        if (
            BingoClient.clientGame == null || config.getBoardCorner() != BoardCorner.UPPER_RIGHT ||
                minecraft.getDebugOverlay().showDebugScreen() || minecraft.screen instanceof BoardScreen
        ) {
            original.call(instance, graphics);
            return;
        }

        final float scale = config.getBoardScale();
        graphics.enableScissor(
            0, 0,
            (int)(graphics.guiWidth() - (BingoClient.getBoardWidth() + BingoClient.BOARD_OFFSET) * scale),
            graphics.guiHeight()
        );
        graphics.pose().pushMatrix();
        graphics.pose().translate(
            (-BingoClient.getBoardWidth() - BingoClient.BOARD_OFFSET) * scale,
            BingoClient.BOARD_OFFSET * scale
        );
        original.call(instance, graphics);
        graphics.pose().popMatrix();
        graphics.disableScissor();
    }
}
