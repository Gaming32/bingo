package io.github.gaming32.bingo.mixin.common;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerStatsCounter.class)
public class MixinServerStatsCounter extends StatsCounter {
    @WrapMethod(method = "setValue")
    private void onSetValue(Player player, Stat<?> stat, int value, Operation<Void> original) {
        if (Bingo.activeGame != null) {
            final var baseStats = Bingo.activeGame.getOrCreateBaseStats(player);
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
