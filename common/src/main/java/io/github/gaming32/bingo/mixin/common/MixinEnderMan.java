package io.github.gaming32.bingo.mixin.common;

import io.github.gaming32.bingo.ext.EnderManExt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderMan.class)
public class MixinEnderMan implements EnderManExt {
    @Unique
    private boolean hasOnlyBeenDamagedByEndermite = true;

    @Inject(method = "hurt", at = @At("RETURN"))
    private void onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            if (!(source.getEntity() instanceof Endermite)) {
                hasOnlyBeenDamagedByEndermite = false;
            }
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void onWriteNbt(CompoundTag nbt, CallbackInfo ci) {
        nbt.putBoolean("bingo:has_only_been_damaged_by_endermite", hasOnlyBeenDamagedByEndermite);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void onReadNbt(CompoundTag nbt, CallbackInfo ci) {
        hasOnlyBeenDamagedByEndermite = nbt.getBoolean("bingo:has_only_been_damaged_by_endermite");
    }

    @Override
    public boolean bingo$hasOnlyBeenDamagedByEndermite() {
        return hasOnlyBeenDamagedByEndermite;
    }
}
