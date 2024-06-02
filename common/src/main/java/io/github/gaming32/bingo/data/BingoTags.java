package io.github.gaming32.bingo.data;

import io.github.gaming32.bingo.util.ResourceLocations;
import net.minecraft.resources.ResourceLocation;

public final class BingoTags {
    private BingoTags() {
    }

    public static final ResourceLocation ACTION = create("action");
    public static final ResourceLocation BUILD = create("build");
    public static final ResourceLocation COLOR = create("color");
    public static final ResourceLocation COMBAT = create("combat");
    public static final ResourceLocation END = create("end");
    public static final ResourceLocation FINISH = create("finish");
    public static final ResourceLocation ITEM = create("item");
    public static final ResourceLocation NETHER = create("nether");
    public static final ResourceLocation NEVER = create("never");
    public static final ResourceLocation OCEAN = create("ocean");
    public static final ResourceLocation OVERWORLD = create("overworld");
    public static final ResourceLocation RARE_BIOME = create("rare_biome");
    public static final ResourceLocation STAT = create("stat");
    public static final ResourceLocation STALEMATE = create("stalemate");
    public static final ResourceLocation VILLAGE = create("village");

    private static ResourceLocation create(String name) {
        return ResourceLocations.bingo(name);
    }
}
