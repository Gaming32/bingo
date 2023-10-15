package io.github.gaming32.bingo.mixin.common.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.gaming32.bingo.client.BingoClient;
import io.github.gaming32.bingo.client.BingoClientConfig;
import io.github.gaming32.bingo.client.BoardCorner;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Gui.class)
public class MixinGui {
    @WrapOperation(
        method = "render",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderEffects(Lnet/minecraft/client/gui/GuiGraphics;)V")
    )
    private void moveEffects(Gui instance, GuiGraphics guiGraphics, Operation<Void> original) {
        final BingoClientConfig.BoardConfig boardConfig = BingoClient.getConfig().board;
        if (BingoClient.clientGame == null || boardConfig.corner != BoardCorner.UPPER_RIGHT) {
            original.call(instance, guiGraphics);
            return;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(
            (-BingoClient.getBoardWidth() - BingoClient.BOARD_OFFSET) * boardConfig.scale,
            BingoClient.BOARD_OFFSET * boardConfig.scale, 0f
        );
        original.call(instance, guiGraphics);
        guiGraphics.pose().popPose();
    }
}
