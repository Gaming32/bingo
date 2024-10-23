package io.github.gaming32.bingo.mixin.common;

import io.github.gaming32.bingo.ext.ItemEntityExt;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity extends Entity implements ItemEntityExt {
    public MixinItemEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow public abstract @Nullable Entity getOwner();

    @Unique
    @Nullable
    private Entity bingo$droppedBy;
    @Unique
    @Nullable
    private UUID bingo$dropepdByUuid;

    @Override
    public void bingo$setDroppedBy(Entity entity) {
        bingo$droppedBy = entity;
        bingo$dropepdByUuid = entity.getUUID();
    }

    @Nullable
    @Override
    public Entity bingo$getDroppedBy() {
        if (bingo$dropepdByUuid != null && level() instanceof ServerLevel serverLevel) {
            Entity newDroppedBy = serverLevel.getEntity(bingo$dropepdByUuid);
            if (newDroppedBy != null) {
                bingo$droppedBy = newDroppedBy;
            }
        }
        if (bingo$droppedBy == null) {
            bingo$droppedBy = getOwner();
            if (bingo$droppedBy != null) {
                bingo$dropepdByUuid = bingo$droppedBy.getUUID();
            }
        }
        return bingo$droppedBy;
    }

    @Inject(
        method = "hurtServer",
        at = {
            @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/item/ItemStack;onDestroyed(Lnet/minecraft/world/entity/item/ItemEntity;)V"
            ),
            @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/item/ItemStack;onDestroyed(Lnet/minecraft/world/entity/item/ItemEntity;Lnet/minecraft/world/damagesource/DamageSource;)V"
            )
        },
        allow = 1
    )
    @SuppressWarnings({"UnresolvedMixinReference", "MixinAnnotationTarget"})
    private void onKilled(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        BingoTriggers.KILL_ITEM.get().trigger((ItemEntity) (Object) this, source, amount);
    }
}
