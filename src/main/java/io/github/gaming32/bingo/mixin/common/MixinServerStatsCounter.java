package io.github.gaming32.bingo.mixin.common;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.gaming32.bingo.ext.MinecraftServerExt;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerStatsCounter.class)
public class MixinServerStatsCounter extends StatsCounter {
    @Shadow @Final private MinecraftServer server;

    @WrapMethod(method = "setValue")
    private void onSetValue(Player player, Stat<?> stat, int value, Operation<Void> original) {
        final var game = ((MinecraftServerExt) server).bingo$getGame();
        if (game != null) {
            final var baseStats = game.getOrCreateBaseStats(player);
            if (!baseStats.containsKey(stat)) {
                baseStats.put(stat, getValue(stat));
            }
        }
        original.call(player, stat, value);
        if (player instanceof ServerPlayer serverPlayer) {
            BingoTriggers.RELATIVE_STATS.get().trigger(serverPlayer);
        }
    }
}
