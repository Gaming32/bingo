package io.github.gaming32.bingo.mixin.common;

import io.github.gaming32.bingo.ext.ItemEntityExt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity implements ItemEntityExt {
    @Shadow public abstract @Nullable Entity getOwner();

    @Unique
    private Entity bingo$droppedBy;

    @Override
    public void bingo$setDroppedBy(Entity entity) {
        bingo$droppedBy = entity;
    }

    @Nullable
    @Override
    public Entity bingo$getDroppedBy() {
        if (bingo$droppedBy == null) {
            bingo$droppedBy = getOwner();
        }
        return bingo$droppedBy;
    }
}
