package io.github.gaming32.bingo.data.tags;

import io.github.gaming32.bingo.util.ResourceLocations;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.List;

public final class BingoItemTags {
    private BingoItemTags() {
    }

    public static final TagKey<Item> ALLOWED_HEADS = create("allowed_heads");
    public static final TagKey<Item> ALL_MINERAL_BLOCKS = create("all_mineral_blocks");
    public static final TagKey<Item> BANNER_PATTERNS = create("banner_patterns");
    public static final TagKey<Item> BASIC_MINERAL_BLOCKS = create("basic_mineral_blocks");
    public static final TagKey<Item> BONEMEALABLE = create("bonemealable");
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
    public static final TagKey<Item> MEAT = create("meat");
    public static final TagKey<Item> NOT_MEAT = create("not_meat");
    public static final TagKey<Item> SLABS = create("slabs");
    public static final TagKey<Item> STAIRS = create("stairs");

    public static final TagKey<Item> ARMORS_LEATHER = createConvention("armors/leather");
    public static final TagKey<Item> ARMORS_CHAINMAIL = createConvention("armors/chainmail");
    public static final TagKey<Item> ARMORS_IRON = createConvention("armors/iron");
    public static final TagKey<Item> ARMORS_GOLD = createConvention("armors/gold");
    public static final TagKey<Item> ARMORS_DIAMOND = createConvention("armors/diamond");
    public static final TagKey<Item> ARMORS_TURTLE_SCUTE = createConvention("armors/turtle_scute");
    public static final TagKey<Item> ARMORS_NETHERITE = createConvention("armors/netherite");
    public static final TagKey<Item> ARMORS_ARMADILLO_SCUTE = createConvention("armors/armadillo_scute");

    public static final List<TagKey<Item>> ARMOR_TYPE_TAGS = List.of(
        ARMORS_LEATHER,
        ARMORS_CHAINMAIL,
        ARMORS_IRON,
        ARMORS_GOLD,
        ARMORS_DIAMOND,
        ARMORS_TURTLE_SCUTE,
        ARMORS_NETHERITE,
        ARMORS_ARMADILLO_SCUTE
    );

    private static TagKey<Item> create(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocations.bingo(name));
    }

    // Proposed convention tags
    private static TagKey<Item> createConvention(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocations.c(name));
    }
}
