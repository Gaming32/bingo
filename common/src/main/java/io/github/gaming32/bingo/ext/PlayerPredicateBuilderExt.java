package io.github.gaming32.bingo.ext;

import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.stats.Stat;

public interface PlayerPredicateBuilderExt {
    PlayerPredicate.Builder bingo$addRelativeStat(Stat<?> stat, MinMaxBounds.Ints value);
}
