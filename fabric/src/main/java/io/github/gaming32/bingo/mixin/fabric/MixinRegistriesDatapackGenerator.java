package io.github.gaming32.bingo.mixin.fabric;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.gaming32.bingo.fabric.datagen.CustomRegistriesDatapackGenerator;
import net.minecraft.data.registries.RegistriesDatapackGenerator;
import net.minecraft.resources.RegistryDataLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(RegistriesDatapackGenerator.class)
public class MixinRegistriesDatapackGenerator {
    @ModifyExpressionValue(
        method = "lambda$run$2",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/resources/RegistryDataLoader;WORLDGEN_REGISTRIES:Ljava/util/List;"
        )
    )
    private List<RegistryDataLoader.RegistryData<?>> getCustomRegistries(List<RegistryDataLoader.RegistryData<?>> original) {
        if ((Object)this instanceof CustomRegistriesDatapackGenerator custom) {
            return custom.getGeneratedRegistries();
        }
        return original;
    }
}
