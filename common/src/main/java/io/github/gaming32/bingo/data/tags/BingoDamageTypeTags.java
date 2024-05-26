package io.github.gaming32.bingo.data.tags;

import io.github.gaming32.bingo.util.ResourceLocations;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public final class BingoDamageTypeTags {
    private BingoDamageTypeTags() {
    }

    public static final TagKey<DamageType> VOID = create("void");
    public static final TagKey<DamageType> BERRY_BUSH = create("berry_bush");

    private static TagKey<DamageType> create(String name) {
        return TagKey.create(Registries.DAMAGE_TYPE, ResourceLocations.bingo(name));
    }
}
