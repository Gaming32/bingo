package io.github.gaming32.bingo.data.progresstrackers;

import com.mojang.serialization.MapCodec;
import io.github.gaming32.bingo.game.ActiveGoal;
import io.github.gaming32.bingo.game.BingoGame;
import net.minecraft.server.level.ServerPlayer;

public enum GoalAchievedCountProgressTracker implements ProgressTracker {
    INSTANCE;

    public static final MapCodec<GoalAchievedCountProgressTracker> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public void onGoalCompleted(BingoGame game, ServerPlayer player, ActiveGoal goal, int completedCount) {
        game.updateProgress(player, goal, completedCount, goal.requiredCount());
    }

    @Override
    public ProgressTrackerType<?> type() {
        return ProgressTrackerType.GOAL_ACHIEVED_COUNT.get();
    }
}
