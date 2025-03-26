package io.github.gaming32.bingo.mixin.common;

import io.github.gaming32.bingo.ext.LeashFenceKnotEntityExt;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(LeashFenceKnotEntity.class)
public abstract class MixinLeashFenceKnotEntity extends BlockAttachedEntity implements LeashFenceKnotEntityExt {
    @Unique
    private Player bingo$cachedOwner;
    @Unique
    private UUID bingo$ownerUuid;

    protected MixinLeashFenceKnotEntity(EntityType<? extends HangingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public @Nullable Player bingo$getOwner() {
        if (bingo$cachedOwner == null) {
            final UUID uuid = bingo$ownerUuid;
            if (uuid == null) {
                return null;
            }
            final Player player = level().getPlayerByUUID(uuid);
            if (player == null) {
                return null;
            }
            bingo$cachedOwner = player;
            bingo$ownerUuid = null;
            return player;
        }
        return bingo$cachedOwner;
    }

    @Override
    public void bingo$setOwner(@Nullable Player player) {
        bingo$cachedOwner = player;
        bingo$ownerUuid = null;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void writeOwner(CompoundTag compound, CallbackInfo ci) {
        final UUID ownerUuid;
        if (bingo$cachedOwner != null) {
            ownerUuid = bingo$cachedOwner.getUUID();
        } else if (bingo$ownerUuid != null) {
            ownerUuid = bingo$ownerUuid;
        } else {
            return;
        }
        compound.store("bingo:owner", UUIDUtil.CODEC, ownerUuid);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void readOwner(CompoundTag compound, CallbackInfo ci) {
        final UUID ownerUuid = compound.read("bingo:owner", UUIDUtil.CODEC).orElse(null);
        if (bingo$cachedOwner != null && bingo$cachedOwner.getUUID().equals(ownerUuid)) return;
        bingo$ownerUuid = ownerUuid;
        bingo$cachedOwner = null;
    }
}
