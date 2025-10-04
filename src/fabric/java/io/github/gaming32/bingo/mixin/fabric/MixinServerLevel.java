package io.github.gaming32.bingo.mixin.fabric;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import io.github.gaming32.bingo.fabric.event.FabricEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerLevel.class)
public class MixinServerLevel {
    @ModifyReceiver(
        method = "explode",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/ServerExplosion;explode()I"
        )
    )
    private ServerExplosion onExplosion(ServerExplosion instance) {
        FabricEvents.SERVER_EXPLOSION.invoker().accept((ServerLevel)(Object)this, instance);
        return instance;
    }
}
