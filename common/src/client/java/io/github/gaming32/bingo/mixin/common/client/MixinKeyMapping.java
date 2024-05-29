package io.github.gaming32.bingo.mixin.common.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.gaming32.bingo.network.BingoNetworking;
import io.github.gaming32.bingo.network.messages.c2s.KeyPressedPayload;
import net.minecraft.client.KeyMapping;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyMapping.class)
public class MixinKeyMapping {
    @Inject(
        method = "click",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/KeyMapping;clickCount:I",
            opcode = Opcodes.PUTFIELD
        )
    )
    private static void onKeyPress(InputConstants.Key key, CallbackInfo ci, @Local KeyMapping keyMapping) {
        if (BingoNetworking.instance().canServerReceive(KeyPressedPayload.TYPE)) {
            new KeyPressedPayload(keyMapping.getName()).sendToServer();
        }
    }
}
