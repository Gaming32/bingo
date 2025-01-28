package io.github.gaming32.bingo.data;

import io.github.gaming32.bingo.util.ResourceLocations;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

public final class BingoTags {
    public static final ResourceKey<BingoTag> ACTION = createKey("action");
    public static final ResourceKey<BingoTag> BUILD = createKey("build");
    public static final ResourceKey<BingoTag> COLOR = createKey("color");
    public static final ResourceKey<BingoTag> COMBAT = createKey("combat");
    public static final ResourceKey<BingoTag> END = createKey("end");
    public static final ResourceKey<BingoTag> FINISH = createKey("finish");
    public static final ResourceKey<BingoTag> ITEM = createKey("item");
    public static final ResourceKey<BingoTag> LOCKOUT_INFLICTABLE = createKey("lockout_inflictable");
    public static final ResourceKey<BingoTag> NETHER = createKey("nether");
    public static final ResourceKey<BingoTag> NEVER = createKey("never");
    public static final ResourceKey<BingoTag> OCEAN = createKey("ocean");
    public static final ResourceKey<BingoTag> OVERWORLD = createKey("overworld");
    public static final ResourceKey<BingoTag> RARE_BIOME = createKey("rare_biome");
    public static final ResourceKey<BingoTag> STAT = createKey("stat");
    public static final ResourceKey<BingoTag> VILLAGE = createKey("village");

    private BingoTags() {
    }

    public static void bootstrap(BootstrapContext<BingoTag> context) {
        context.register(ACTION, BingoTag.builder().difficultyMax(20, 20, 20, 20, 20).build());
        context.register(BUILD, BingoTag.builder().difficultyMax(20, 20, 20, 20, 20).build());
        context.register(COLOR, BingoTag.builder().difficultyMax(2, 2, 2, 2, 2).build());
        context.register(COMBAT, BingoTag.builder().difficultyMax(5, 10, 20, 20, 20).build());
        context.register(END, BingoTag.builder().difficultyMax(0, 0, 0, 1, 5).build());
        context.register(FINISH, BingoTag.builder()
            .difficultyMax(1, 1, 1, 1, 1)
            .disallowOnSameLine()
            .specialType(BingoTag.SpecialType.FINISH)
            .build()
        );
        context.register(ITEM, BingoTag.builder().difficultyMax(25, 25, 20, 20, 20).build());
        context.register(LOCKOUT_INFLICTABLE, BingoTag.builder().markerTag().build());
        context.register(NETHER, BingoTag.builder().difficultyMax(0, 2, 5, 10, 15).build());
        context.register(NEVER, BingoTag.builder()
            .difficultyMax(3, 3, 3, 2, 1)
            .disallowOnSameLine()
            .specialType(BingoTag.SpecialType.NEVER)
            .build()
        );
        context.register(OCEAN, BingoTag.builder().difficultyMax(5, 5, 5, 5, 5).build());
        context.register(OVERWORLD, BingoTag.builder().difficultyMax(25, 25, 24, 21, 18).build());
        context.register(RARE_BIOME, BingoTag.builder().difficultyMax(0, 1, 2, 4, 6).build());
        context.register(STAT, BingoTag.builder().difficultyMax(5, 5, 5, 5, 5).build());
        context.register(VILLAGE, BingoTag.builder().difficultyMax(0, 1, 2, 3, 4).build());
    }

    private static ResourceKey<BingoTag> createKey(String name) {
        return ResourceKey.create(BingoRegistries.TAG, ResourceLocations.bingo(name));
    }
}
