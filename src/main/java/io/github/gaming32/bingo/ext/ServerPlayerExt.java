package io.github.gaming32.bingo.ext;

import net.minecraft.server.level.ServerPlayer;

public interface ServerPlayerExt {
    void bingo$markAdvancementsNeedClearing();

    boolean bingo$clearAdvancementsNeedClearing();

    void bingo$copyAdvancementsNeedClearingTo(ServerPlayer toPlayer);
}
