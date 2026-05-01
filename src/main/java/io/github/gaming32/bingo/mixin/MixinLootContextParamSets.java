package io.github.gaming32.bingo.mixin;

import net.minecraft.resources.Identifier;
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
            target = "Lnet/minecraft/resources/Identifier;withDefaultNamespace(Ljava/lang/String;)Lnet/minecraft/resources/Identifier;"
        ),
        require = 0
    )
    private static Identifier supportOtherNamespaces(String path) {
        return Identifier.parse(path);
    }
}
