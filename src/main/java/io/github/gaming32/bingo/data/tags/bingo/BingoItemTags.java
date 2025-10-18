package io.github.gaming32.bingo.data.tags.bingo;

import io.github.gaming32.bingo.util.ResourceLocations;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class BingoItemTags {
    private BingoItemTags() {
    }

    public static final TagKey<Item> ALL_MINERAL_BLOCKS = create("all_mineral_blocks");
    public static final TagKey<Item> BANNER_PATTERNS = create("banner_patterns");
    public static final TagKey<Item> BASIC_MINERAL_BLOCKS = create("basic_mineral_blocks");
    public static final TagKey<Item> BONEMEALABLE = create("bonemealable");
    public static final TagKey<Item> CLIMBABLE = create("climbable");
    public static final TagKey<Item> DEAD_CORAL_BLOCKS = create("dead_coral_blocks");
    public static final TagKey<Item> DIAMOND_IN_NAME = create("diamond_in_name");
    public static final TagKey<Item> FISHING_JUNK = create("fishing_junk");
    public static final TagKey<Item> FISHING_TREASURE = create("fishing_treasure");
    public static final TagKey<Item> FISH_BUCKETS = create("fish_buckets");
    public static final TagKey<Item> GOLD_IN_NAME = create("gold_in_name");
    public static final TagKey<Item> COPPER_IN_NAME = create("copper_in_name");
    public static final TagKey<Item> LIVING_CORAL_BLOCKS = create("living_coral_blocks");
    public static final TagKey<Item> MEAT = create("meat");
    public static final TagKey<Item> NOT_MEAT = create("not_meat");
    public static final TagKey<Item> TORCHES = create("torches");
    public static final TagKey<Item> TRIM_TEMPLATES = create("trim_templates");

    private static TagKey<Item> create(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocations.bingo(name));
    }
}
