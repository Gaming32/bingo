package io.github.gaming32.bingo.data.tags;

import io.github.gaming32.bingo.Bingo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.dimension.LevelStem;

public final class BingoDimensionTags {
    private BingoDimensionTags() {
    }

    public static final TagKey<LevelStem> OVERWORLDS = create("overworlds");
    public static final TagKey<LevelStem> NETHERS = create("nethers");
    public static final TagKey<LevelStem> ENDS = create("ends");

    private static TagKey<LevelStem> create(String name) {
        return TagKey.create(Registries.LEVEL_STEM, new ResourceLocation(Bingo.MOD_ID, name));
    }
}
