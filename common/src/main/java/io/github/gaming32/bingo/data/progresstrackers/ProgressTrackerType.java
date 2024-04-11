package io.github.gaming32.bingo.data.progresstrackers;

import com.mojang.serialization.MapCodec;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.registry.registries.options.DefaultIdRegistrarOption;
import io.github.gaming32.bingo.Bingo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public interface ProgressTrackerType<P extends ProgressTracker> {
    ResourceKey<Registry<ProgressTrackerType<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(
        new ResourceLocation("bingo:progress_tracker_type")
    );
    Registrar<ProgressTrackerType<?>> REGISTRAR = Bingo.REGISTRAR_MANAGER
        .<ProgressTrackerType<?>>builder(REGISTRY_KEY.location())
        .option(new DefaultIdRegistrarOption(new ResourceLocation("bingo:empty")))
        .build();

    RegistrySupplier<ProgressTrackerType<EmptyProgressTracker>> EMPTY = register("empty", EmptyProgressTracker.CODEC);
    RegistrySupplier<ProgressTrackerType<AchievedRequirementsProgressTracker>> ACHIEVED_REQUIREMENTS = register("achieved_requirements", AchievedRequirementsProgressTracker.CODEC);
    RegistrySupplier<ProgressTrackerType<CriterionProgressTracker>> CRITERION = register("criterion", CriterionProgressTracker.CODEC);
    RegistrySupplier<ProgressTrackerType<GoalAchievedCountProgressTracker>> GOAL_ACHIEVED_COUNT = register("goal_achieved_type", GoalAchievedCountProgressTracker.CODEC);

    MapCodec<P> codec();

    static <P extends ProgressTracker> RegistrySupplier<ProgressTrackerType<P>> register(String id, MapCodec<P> codec) {
        if (id.indexOf(':') < 0) {
            id = "bingo:" + id;
        }
        final ResourceLocation location = new ResourceLocation(id);
        return REGISTRAR.register(location, () -> new ProgressTrackerType<>() {
            @Override
            public MapCodec<P> codec() {
                return codec;
            }

            @Override
            public String toString() {
                return "ProgressTrackerType[" + location + "]";
            }
        });
    }

    static void load() {
    }
}
