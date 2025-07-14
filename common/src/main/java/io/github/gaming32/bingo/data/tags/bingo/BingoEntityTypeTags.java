package io.github.gaming32.bingo.data.tags.bingo;

import io.github.gaming32.bingo.util.ResourceLocations;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class BingoEntityTypeTags {
    private BingoEntityTypeTags() {
    }

    public static final TagKey<EntityType<?>> PASSIVE = create("passive");
    public static final TagKey<EntityType<?>> HOSTILE = create("hostile");

    private static TagKey<EntityType<?>> create(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, ResourceLocations.bingo(name));
    }
}
