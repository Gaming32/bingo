package io.github.gaming32.bingo.data.tags;

import io.github.gaming32.bingo.Bingo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public final class BingoDamageTypeTags {
    private BingoDamageTypeTags() {
    }

    public static final TagKey<DamageType> VOID = create("void");

    private static TagKey<DamageType> create(String name) {
        return TagKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(Bingo.MOD_ID, name));
    }
}
