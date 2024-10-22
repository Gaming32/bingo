package io.github.gaming32.bingo.mixin.fabric;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Workaround for https://github.com/FabricMC/fabric/issues/4179
@Mixin(Registries.class)
public class MixinRegistries {
    @Inject(method = "elementsDirPath", at = @At("HEAD"), cancellable = true)
    private static void useNamespacedPathForBingo(ResourceKey<? extends Registry<?>> registryKey, CallbackInfoReturnable<String> cir) {
        final var location = registryKey.location();
        if (location.getNamespace().equals("bingo")) {
            cir.setReturnValue("bingo/" + location.getPath());
        }
    }
}
