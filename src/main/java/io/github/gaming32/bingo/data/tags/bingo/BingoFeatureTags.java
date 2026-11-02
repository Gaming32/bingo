package io.github.gaming32.bingo.data.tags.bingo;

import io.github.gaming32.bingo.util.Identifiers;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.feature.Feature;

public final class BingoFeatureTags {
    private BingoFeatureTags() {
    }

    public static final TagKey<Feature> HUGE_FUNGI = create("huge_fungi");
    public static final TagKey<Feature> HUGE_MUSHROOMS = create("huge_mushrooms");
    public static final TagKey<Feature> MEGA_JUNGLE_TREES = create("mega_jungle_trees");
    public static final TagKey<Feature> TREES = create("trees");

    private static TagKey<Feature> create(String name) {
        return TagKey.create(Registries.FEATURE, Identifiers.bingo(name));
    }
}
