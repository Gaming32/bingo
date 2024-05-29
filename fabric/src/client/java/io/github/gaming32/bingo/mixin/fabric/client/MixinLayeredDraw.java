package io.github.gaming32.bingo.mixin.fabric.client;

import io.github.gaming32.bingo.client.BingoClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayeredDraw.class)
public class MixinLayeredDraw {
    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/LayeredDraw;renderInner(Lnet/minecraft/client/gui/GuiGraphics;F)V",
            shift = At.Shift.AFTER
        )
    )
    @SuppressWarnings("UnreachableCode")
    private void renderHud(GuiGraphics guiGraphics, float f, CallbackInfo ci) {
        final Minecraft minecraft = Minecraft.getInstance();
        if ((Object)this == ((GuiAccessor)minecraft.gui).getLayers()) {
            BingoClient.renderBoardOnHud(minecraft, guiGraphics);
        }
    }
}
