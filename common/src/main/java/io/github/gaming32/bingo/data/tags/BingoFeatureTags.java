package io.github.gaming32.bingo.data.tags;

import io.github.gaming32.bingo.Bingo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public final class BingoFeatureTags {
    private BingoFeatureTags() {
    }

    public static final TagKey<ConfiguredFeature<?, ?>> HUGE_FUNGI = create("huge_fungi");
    public static final TagKey<ConfiguredFeature<?, ?>> HUGE_MUSHROOMS = create("huge_mushrooms");
    public static final TagKey<ConfiguredFeature<?, ?>> MEGA_JUNGLE_TREES = create("mega_jungle_trees");
    public static final TagKey<ConfiguredFeature<?, ?>> TREES = create("trees");

    private static TagKey<ConfiguredFeature<?, ?>> create(String name) {
        return TagKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(Bingo.MOD_ID, name));
    }
}
