package io.github.gaming32.bingo.mixin.fabric;

import io.github.gaming32.bingo.fabric.event.FabricEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerLevel.class)
public class MixinServerLevel {
    @ModifyVariable(method = "explode", at = @At("STORE"))
    private ServerExplosion onExplosion(ServerExplosion instance) {
        FabricEvents.SERVER_EXPLOSION.invoker().accept((ServerLevel)(Object)this, instance);
        return instance;
    }
}
