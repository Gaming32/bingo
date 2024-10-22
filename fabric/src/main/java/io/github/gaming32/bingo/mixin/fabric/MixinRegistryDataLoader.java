package io.github.gaming32.bingo.mixin.fabric;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Workaround for https://github.com/FabricMC/fabric/issues/4179
@Mixin(value = RegistryDataLoader.class, priority = 999)
public class MixinRegistryDataLoader {
    @WrapOperation(
        method = {"loadContentsFromManager", "loadContentsFromNetwork"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/registries/Registries;elementsDirPath(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/String;"
        )
    )
    private static String dontApplyFixForBingo(ResourceKey<? extends Registry<?>> registryKey, Operation<String> original) {
        if (registryKey.location().getNamespace().equals("bingo")) {
            return registryKey.location().getPath();
        }
        return original.call(registryKey);
    }
}
