package io.github.gaming32.bingo.data.progresstrackers;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import io.github.gaming32.bingo.data.goal.BingoGoal;
import io.github.gaming32.bingo.game.ActiveGoal;
import io.github.gaming32.bingo.game.BingoGame;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

// For goals that need any N of their criteria
public record AchievedCriteriaProgressTracker(int required) implements ProgressTracker {
    public static final MapCodec<AchievedCriteriaProgressTracker> CODEC = ExtraCodecs.POSITIVE_INT
        .xmap(AchievedCriteriaProgressTracker::new, AchievedCriteriaProgressTracker::required)
        .fieldOf("required");

    @Override
    public DataResult<ProgressTracker> validate(BingoGoal goal) {
        if (required > goal.getCriteria().size()) {
            return DataResult.error(() -> "Required criteria count " + required + " exceeds the number of criteria " + goal.getCriteria().size());
        }
        return DataResult.success(this);
    }

    @Override
    public void criterionChanged(BingoGame game, ServerPlayer player, ActiveGoal goal, String criterion, boolean complete) {
        int completed = 0;
        for (final String ignored : game.getOrStartProgress(player, goal).getCompletedCriteria()) {
            completed++;
        }
        game.updateProgress(player, goal, Math.min(completed, required), required);
    }

    @Override
    public ProgressTrackerType<?> type() {
        return ProgressTrackerType.ACHIEVED_CRITERIA.get();
    }
}
