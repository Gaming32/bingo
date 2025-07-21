package io.github.gaming32.bingo.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.ExtraCodecs;

public record GoalProgress(int progress, int maxProgress) {
    public static final Codec<GoalProgress> PERSISTENCE_CODEC = ExtraCodecs.intervalCodec(
        Codec.INT, "progress", "max_progress",
        (progress, maxProgress) -> {
            if (progress < 0) {
                return DataResult.error(() -> "progress < 0");
            }
            if (maxProgress < 0) {
                return DataResult.error(() -> "maxProgress < 0");
            }
            if (progress > maxProgress) {
                return DataResult.error(() -> "progress > maxProgress");
            }
            return DataResult.success(new GoalProgress(progress, maxProgress));
        },
        GoalProgress::progress, GoalProgress::maxProgress
    );
}
