package io.github.gaming32.bingo.mixin.fabric.client;

import io.github.gaming32.bingo.fabric.FabricClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Shadow @Nullable public LocalPlayer player;

    @Inject(
        method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;)V",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;gameMode:Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;",
            opcode = Opcodes.PUTFIELD
        )
    )
    private void onPlayerQuit(Screen nextScreen, CallbackInfo ci) {
        FabricClientEvents.PLAYER_QUIT.invoker().accept(player);
    }
}
