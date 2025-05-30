package io.github.gaming32.bingo.data;

import io.github.gaming32.bingo.data.goal.BingoGoal;
import io.github.gaming32.bingo.data.icons.GoalIconType;
import io.github.gaming32.bingo.data.progresstrackers.ProgressTrackerType;
import io.github.gaming32.bingo.data.subs.BingoSubType;
import io.github.gaming32.bingo.game.mode.BingoGameMode;
import io.github.gaming32.bingo.util.ResourceLocations;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class BingoRegistries {
    public static final ResourceKey<Registry<BingoDifficulty>> DIFFICULTY = createKey("difficulty");
    public static final ResourceKey<Registry<BingoGameMode>> GAME_MODE = createKey("game_mode");
    public static final ResourceKey<Registry<BingoGoal>> GOAL = createKey("goal");
    public static final ResourceKey<Registry<GoalIconType<?>>> GOAL_ICON_TYPE = createKey("goal_icon_type");
    public static final ResourceKey<Registry<ProgressTrackerType<?>>> PROGRESS_TRACKER_TYPE = createKey("progress_tracker_type");
    public static final ResourceKey<Registry<BingoSubType<?>>> SUB_TYPE = createKey("sub_type");
    public static final ResourceKey<Registry<BingoTag>> TAG = createKey("tag");

    private static <T> ResourceKey<Registry<T>> createKey(String name) {
        return ResourceKey.createRegistryKey(ResourceLocations.bingo(name));
    }
}
