package io.github.gaming32.bingo.mixin.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LootContextParamSets.class)
public class MixinLootContextParamSets {
    @Redirect(
        method = "register",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/resources/ResourceLocation;withDefaultNamespace(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"
        ),
        require = 0
    )
    private static ResourceLocation supportOtherNamespaces(String string) {
        return ResourceLocation.parse(string);
    }
}
