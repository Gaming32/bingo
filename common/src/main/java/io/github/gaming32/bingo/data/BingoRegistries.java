package io.github.gaming32.bingo.data;

import io.github.gaming32.bingo.data.icons.GoalIconType;
import io.github.gaming32.bingo.data.progresstrackers.ProgressTrackerType;
import io.github.gaming32.bingo.data.subs.BingoSubType;
import io.github.gaming32.bingo.util.ResourceLocations;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class BingoRegistries {
    public static final ResourceKey<Registry<GoalIconType<?>>> GOAL_ICON_TYPE = createKey("goal_icon_type");
    public static final ResourceKey<Registry<ProgressTrackerType<?>>> PROGRESS_TRACKER_TYPE = createKey("progress_tracker_type");
    public static final ResourceKey<Registry<BingoSubType<?>>> BINGO_SUB_TYPE = createKey("bingo_sub_type");
    public static final ResourceKey<Registry<BingoTag>> TAG = createKey("tag");
    public static final ResourceKey<Registry<BingoDifficulty>> DIFFICULTY = createKey("difficulty");

    private static <T> ResourceKey<Registry<T>> createKey(String name) {
        return ResourceKey.createRegistryKey(ResourceLocations.bingo(name));
    }
}
