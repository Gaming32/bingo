package io.github.gaming32.bingo.data;

import io.github.gaming32.bingo.util.ResourceLocations;
import net.minecraft.Util;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
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
        register(context, VERY_EASY, 0);
        register(context, EASY, 1);
        register(context, MEDIUM, 2);
        register(context, HARD, 3);
        register(context, VERY_HARD, 4);
    }

    private static void register(BootstrapContext<BingoDifficulty> context, ResourceKey<BingoDifficulty> key, int number) {
        context.register(key, new BingoDifficulty(
            Component.translatable(Util.makeDescriptionId("bingo_difficulty", key.location())), number
        ));
    }

    private static ResourceKey<BingoDifficulty> createKey(String name) {
        return ResourceKey.create(BingoRegistries.DIFFICULTY, ResourceLocations.bingo(name));
    }
}
