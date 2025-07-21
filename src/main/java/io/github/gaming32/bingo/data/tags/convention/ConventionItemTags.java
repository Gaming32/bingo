package io.github.gaming32.bingo.data.tags.convention;

import io.github.gaming32.bingo.util.ResourceLocations;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ConventionItemTags {
    public static final TagKey<Item> ARMORS = create("armors");

    private ConventionItemTags() {
    }

    private static TagKey<Item> create(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocations.c(name));
    }
}
