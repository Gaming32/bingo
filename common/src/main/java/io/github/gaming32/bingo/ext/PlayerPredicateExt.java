package io.github.gaming32.bingo.ext;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.stats.Stat;

import java.util.Map;

public interface PlayerPredicateExt {
    void bingo$setRelativeStats(Map<Stat<?>, MinMaxBounds.Ints> relativeStats);
}
