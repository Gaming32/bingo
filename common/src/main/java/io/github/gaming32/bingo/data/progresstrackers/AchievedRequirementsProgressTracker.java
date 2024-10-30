package io.github.gaming32.bingo.data.progresstrackers;

import com.mojang.serialization.MapCodec;
import io.github.gaming32.bingo.game.ActiveGoal;
import io.github.gaming32.bingo.game.BingoGame;
import io.github.gaming32.bingo.mixin.common.AdvancementProgressAccessor;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.level.ServerPlayer;

public enum AchievedRequirementsProgressTracker implements ProgressTracker {
    INSTANCE;

    public static final MapCodec<AchievedRequirementsProgressTracker> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public void criterionChanged(BingoGame game, ServerPlayer player, ActiveGoal goal, String criterion, boolean complete) {
        final AdvancementProgress progress = game.getOrStartProgress(player, goal);
        final int completed = ((AdvancementProgressAccessor)progress).callCountCompletedRequirements();
        final int total = goal.requirements().size();
        game.updateProgress(player, goal, completed, total);
    }

    @Override
    public ProgressTrackerType<?> type() {
        return ProgressTrackerType.ACHIEVED_REQUIREMENTS.get();
    }
}
