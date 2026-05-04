package io.github.gaming32.bingo.ext;

import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public interface ItemEntityExt {
    void bingo$setDroppedBy(Entity entity);

    @Nullable
    Entity bingo$getDroppedBy();
}
