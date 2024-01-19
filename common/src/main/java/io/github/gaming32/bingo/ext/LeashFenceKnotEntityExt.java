package io.github.gaming32.bingo.ext;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface LeashFenceKnotEntityExt {
    @Nullable Player bingo$getOwner();

    void bingo$setOwner(@Nullable Player player);
}
