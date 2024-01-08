package io.github.gaming32.bingo.mixin.common;

import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Zombie.class)
public class MixinZombie extends Monster {
    protected MixinZombie(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "doUnderWaterConversion", at = @At("HEAD"))
    private void onConversion(CallbackInfo ci) {
        if (getTarget() instanceof ServerPlayer targetPlayer) {
            BingoTriggers.ZOMBIE_DROWNED.get().trigger(targetPlayer, (Zombie) (Object) this);
        }
    }
}
