package io.github.gaming32.bingo.data.progresstrackers;

import com.mojang.serialization.MapCodec;

public enum EmptyProgressTracker implements ProgressTracker {
    INSTANCE;

    public static final MapCodec<EmptyProgressTracker> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public ProgressTrackerType<?> type() {
        return ProgressTrackerType.EMPTY.get();
    }
}
