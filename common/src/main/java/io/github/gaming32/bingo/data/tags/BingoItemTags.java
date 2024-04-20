package io.github.gaming32.bingo.data.tags;

import io.github.gaming32.bingo.Bingo;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class BingoItemTags {
    private BingoItemTags() {
    }

    public static final TagKey<Item> ALLOWED_HEADS = create("allowed_heads");
    public static final TagKey<Item> ALL_MINERAL_BLOCKS = create("all_mineral_blocks");
    public static final TagKey<Item> ARMOR = create("armor");
    public static final TagKey<Item> ARMOR_BOOTS = create("armor/boots");
    public static final TagKey<Item> ARMOR_CHESTPLATES = create("armor/chestplates");
    public static final TagKey<Item> ARMOR_HELMETS = create("armor/helmets");
    public static final TagKey<Item> ARMOR_LEGGINGS = create("armor/leggings");
    public static final TagKey<Item> BANNER_PATTERNS = create("banner_patterns");
    public static final TagKey<Item> BASIC_MINERAL_BLOCKS = create("basic_mineral_blocks");
    public static final TagKey<Item> BONEMEALABLE = create("bonemealable");
    public static final TagKey<Item> BUCKETS = create("buckets");
    public static final TagKey<Item> CLIMBABLE = create("climbable");
    public static final TagKey<Item> CONCRETE = create("concrete");
    public static final TagKey<Item> DEAD_CORAL_BLOCKS = create("dead_coral_blocks");
    public static final TagKey<Item> DIAMOND_IN_NAME = create("diamond_in_name");
    public static final TagKey<Item> FISHING_JUNK = create("fishing_junk");
    public static final TagKey<Item> FISHING_TREASURE = create("fishing_treasure");
    public static final TagKey<Item> FISH_BUCKETS = create("fish_buckets");
    public static final TagKey<Item> FLOWERS = create("flowers");
    public static final TagKey<Item> GLAZED_TERRACOTTA = create("glazed_terracotta");
    public static final TagKey<Item> GOLD_IN_NAME = create("gold_in_name");
    public static final TagKey<Item> LIVING_CORAL_BLOCKS = create("living_coral_blocks");
    public static final TagKey<Item> SLABS = create("slabs");
    public static final TagKey<Item> STAIRS = create("stairs");

    private static TagKey<Item> create(String name) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(Bingo.MOD_ID, name));
    }
}
