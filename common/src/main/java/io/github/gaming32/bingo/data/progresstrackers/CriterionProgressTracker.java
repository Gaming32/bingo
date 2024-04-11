package io.github.gaming32.bingo.data.progresstrackers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.game.ActiveGoal;
import io.github.gaming32.bingo.game.BingoGame;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

public record CriterionProgressTracker(String criterion, float scale) implements ProgressTracker {
    public static final MapCodec<CriterionProgressTracker> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.STRING.fieldOf("criterion").forGetter(CriterionProgressTracker::criterion),
            ExtraCodecs.strictOptionalField(Codec.FLOAT, "scale", 1f).forGetter(CriterionProgressTracker::scale)
        ).apply(instance, CriterionProgressTracker::new)
    );

    public static CriterionProgressTracker unscaled(String criterion) {
        return new CriterionProgressTracker(criterion, 1f);
    }

    @Override
    public DataResult<ProgressTracker> validate(BingoGoal goal) {
        if (!goal.getCriteria().containsKey(criterion)) {
            return DataResult.error(() -> "Specified progress criterion '" + criterion + "' does not exist");
        }
        return DataResult.success(this);
    }

    @Override
    public void goalProgressChanged(BingoGame game, ServerPlayer player, ActiveGoal goal, String criterion, int progress, int maxProgress) {
        if (!criterion.equals(this.criterion)) return;
        game.updateProgress(player, goal, (int)(progress * scale), (int)(maxProgress * scale));
    }

    @Override
    public ProgressTrackerType<?> type() {
        return ProgressTrackerType.CRITERION.get();
    }
}
