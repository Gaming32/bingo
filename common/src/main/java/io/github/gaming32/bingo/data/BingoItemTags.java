package io.github.gaming32.bingo.data;

import io.github.gaming32.bingo.Bingo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class BingoItemTags {
    private BingoItemTags() {
    }

    public static final TagKey<Item> ALLOWED_HEADS = create("allowed_heads");
    public static final TagKey<Item> ARMOR = create("armor");
    public static final TagKey<Item> ARMOR_HELMETS = create("armor/helmets");
    public static final TagKey<Item> ARMOR_CHESTPLATES = create("armor/chestplates");
    public static final TagKey<Item> ARMOR_LEGGINGS = create("armor/leggings");
    public static final TagKey<Item> ARMOR_BOOTS = create("armor/boots");
    public static final TagKey<Item> FISH_BUCKETS = create("fish_buckets");
    public static final TagKey<Item> FISHING_JUNK = create("fishing_junk");
    public static final TagKey<Item> FISHING_TREASURE = create("fishing_treasure");
    public static final TagKey<Item> SHIELDS = create("shields");

    private static TagKey<Item> create(String name) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(Bingo.MOD_ID, name));
    }
}
