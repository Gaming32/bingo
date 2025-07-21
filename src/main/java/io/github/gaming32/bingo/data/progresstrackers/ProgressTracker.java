package io.github.gaming32.bingo.data.progresstrackers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.gaming32.bingo.data.goal.BingoGoal;
import io.github.gaming32.bingo.game.ActiveGoal;
import io.github.gaming32.bingo.game.BingoGame;
import io.github.gaming32.bingo.game.GoalProgress;
import net.minecraft.server.level.ServerPlayer;

public interface ProgressTracker {
    Codec<ProgressTracker> CODEC = ProgressTrackerType.REGISTER
        .registry()
        .byNameCodec()
        .dispatch(ProgressTracker::type, ProgressTrackerType::codec);

    default DataResult<ProgressTracker> validate(BingoGoal goal) {
        return DataResult.success(this);
    }

    default void goalProgressChanged(BingoGame game, ServerPlayer player, ActiveGoal goal, String criterion, int progress, int maxProgress) {
    }

    default void criterionChanged(BingoGame game, ServerPlayer player, ActiveGoal goal, String criterion, boolean complete) {
    }

    default void onGoalCompleted(BingoGame game, ServerPlayer player, ActiveGoal goal, int completedCount) {
        if (completedCount < goal.requiredCount()) {
            GoalProgress progress = game.getGoalProgress(player, goal);
            if (progress != null) {
                game.updateProgress(player, goal, 0, progress.maxProgress());
            }
        }
    }

    ProgressTrackerType<?> type();
}
