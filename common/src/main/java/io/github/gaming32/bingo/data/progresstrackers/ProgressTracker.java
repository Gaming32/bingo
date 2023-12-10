package io.github.gaming32.bingo.data.progresstrackers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.game.ActiveGoal;
import io.github.gaming32.bingo.game.BingoGame;
import io.github.gaming32.bingo.util.BingoCodecs;
import net.minecraft.server.level.ServerPlayer;

public interface ProgressTracker {
    Codec<ProgressTracker> CODEC = BingoCodecs.registrarByName(ProgressTrackerType.REGISTRAR)
        .dispatch(ProgressTracker::type, ProgressTrackerType::codec);

    default DataResult<ProgressTracker> validate(BingoGoal goal) {
        return DataResult.success(this);
    }

    default void goalProgressChanged(BingoGame game, ServerPlayer player, ActiveGoal goal, String criterion, int progress, int maxProgress) {
    }

    default void criterionChanged(BingoGame game, ServerPlayer player, ActiveGoal goal, String criterion, boolean complete) {
    }

    ProgressTrackerType<?> type();
}
