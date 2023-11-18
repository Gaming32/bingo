package io.github.gaming32.bingo.data.progresstrackers;

import com.mojang.serialization.Codec;

public enum EmptyProgressTracker implements ProgressTracker {
    INSTANCE;

    public static final Codec<EmptyProgressTracker> CODEC = Codec.unit(INSTANCE);

    @Override
    public ProgressTrackerType<?> type() {
        return ProgressTrackerType.EMPTY.get();
    }
}
