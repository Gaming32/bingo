package io.github.gaming32.bingo.data;

import io.github.gaming32.bingo.util.ResourceLocations;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

public final class BingoDifficulties {
    public static final ResourceKey<BingoDifficulty> VERY_EASY = createKey("very_easy");
    public static final ResourceKey<BingoDifficulty> EASY = createKey("easy");
    public static final ResourceKey<BingoDifficulty> MEDIUM = createKey("medium");
    public static final ResourceKey<BingoDifficulty> HARD = createKey("hard");
    public static final ResourceKey<BingoDifficulty> VERY_HARD = createKey("very_hard");

    private BingoDifficulties() {
    }

    public static void bootstrap(BootstrapContext<BingoDifficulty> context) {
        context.register(VERY_EASY, BingoDifficulty.builder().number(0).build());
        context.register(EASY, BingoDifficulty.builder().number(1).build());
        context.register(MEDIUM, BingoDifficulty.builder().number(2).build());
        context.register(HARD, BingoDifficulty.builder().number(3).build());
        context.register(VERY_HARD, BingoDifficulty.builder().number(4).build());
    }

    private static ResourceKey<BingoDifficulty> createKey(String name) {
        return ResourceKey.create(BingoRegistries.DIFFICULTY, ResourceLocations.bingo(name));
    }
}
