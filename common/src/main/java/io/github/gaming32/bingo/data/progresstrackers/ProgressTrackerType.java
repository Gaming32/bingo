package io.github.gaming32.bingo.data.progresstrackers;

import com.mojang.serialization.MapCodec;
import io.github.gaming32.bingo.platform.registry.DeferredRegister;
import io.github.gaming32.bingo.platform.registry.RegistryBuilder;
import io.github.gaming32.bingo.platform.registry.RegistryValue;

public interface ProgressTrackerType<P extends ProgressTracker> {
    DeferredRegister<ProgressTrackerType<?>> REGISTER = new RegistryBuilder("progress_tracker_type")
        .defaultId("empty")
        .build();

    RegistryValue<ProgressTrackerType<EmptyProgressTracker>> EMPTY = register("empty", EmptyProgressTracker.CODEC);
    RegistryValue<ProgressTrackerType<AchievedRequirementsProgressTracker>> ACHIEVED_REQUIREMENTS = register("achieved_requirements", AchievedRequirementsProgressTracker.CODEC);
    RegistryValue<ProgressTrackerType<CriterionProgressTracker>> CRITERION = register("criterion", CriterionProgressTracker.CODEC);
    RegistryValue<ProgressTrackerType<GoalAchievedCountProgressTracker>> GOAL_ACHIEVED_COUNT = register("goal_achieved_type", GoalAchievedCountProgressTracker.CODEC);

    MapCodec<P> codec();

    static <P extends ProgressTracker> RegistryValue<ProgressTrackerType<P>> register(String id, MapCodec<P> codec) {
        return REGISTER.register(id, () -> new ProgressTrackerType<>() {
            @Override
            public MapCodec<P> codec() {
                return codec;
            }

            @Override
            public String toString() {
                return "ProgressTrackerType[" + id + "]";
            }
        });
    }

    static void load() {
    }
}
