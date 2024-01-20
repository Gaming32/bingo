package io.github.gaming32.bingo.data.tags;

import io.github.gaming32.bingo.Bingo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class BingoEntityTypeTags {
    private BingoEntityTypeTags() {
    }

    public static final TagKey<EntityType<?>> BOATS = create("boats");
    public static final TagKey<EntityType<?>> TAMABLE = create("tamable");
    public static final TagKey<EntityType<?>> PASSIVE = create("passive");
    public static final TagKey<EntityType<?>> HOSTILE = create("hostile");

    private static TagKey<EntityType<?>> create(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation(Bingo.MOD_ID, name));
    }
}
