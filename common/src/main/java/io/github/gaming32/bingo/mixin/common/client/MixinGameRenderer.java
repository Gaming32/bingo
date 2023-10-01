package io.github.gaming32.bingo.mixin.common.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.client.BingoClientConfig;
import io.github.gaming32.bingo.client.BoardCorner;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @WrapOperation(
        method = "render",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/toasts/ToastComponent;render(Lnet/minecraft/client/gui/GuiGraphics;)V")
    )
    private void moveToastsDown(ToastComponent instance, GuiGraphics guiGraphics, Operation<Void> original) {
        final BingoClientConfig.BoardConfig boardConfig = BingoClient.getConfig().board;
        if (BingoClient.clientBoard == null || boardConfig.corner != BoardCorner.UPPER_RIGHT) {
            original.call(instance, guiGraphics);
            return;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, BingoClient.BOARD_HEIGHT * boardConfig.scale + 2 * BingoClient.BOARD_OFFSET, 0);
        original.call(instance, guiGraphics);
        guiGraphics.pose().popPose();
    }
}
