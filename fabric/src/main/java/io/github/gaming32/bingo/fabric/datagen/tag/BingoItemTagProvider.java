package io.github.gaming32.bingo.fabric.datagen.tag;

import io.github.gaming32.bingo.data.tags.BingoBlockTags;
import io.github.gaming32.bingo.data.tags.BingoItemTags;
import io.github.gaming32.bingo.fabric.datagen.BingoDataGenFabric;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class BingoItemTagProvider extends FabricTagProvider.ItemTagProvider {
    private static final Set<Item> FORCED_MEAT = Set.of(
        Items.COD,
        Items.COOKED_COD,
        Items.SALMON,
        Items.COOKED_SALMON,
        Items.PUFFERFISH,
        Items.TROPICAL_FISH,
        Items.SPIDER_EYE,
        Items.RABBIT_STEW
    );

    public BingoItemTagProvider(
        FabricDataOutput output,
        CompletableFuture<HolderLookup.Provider> registriesFuture,
        FabricTagProvider.BlockTagProvider blockTagProvider
    ) {
        super(output, registriesFuture, blockTagProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        getOrCreateTagBuilder(BingoItemTags.ALLOWED_HEADS).add(
            Items.SKELETON_SKULL,
            Items.PLAYER_HEAD,
            Items.ZOMBIE_HEAD,
            Items.CREEPER_HEAD,
            Items.DRAGON_HEAD,
            Items.PIGLIN_HEAD
        );

        // Cannot copy() because that can't copy from Vanilla's block tags, only our block tags
        getOrCreateTagBuilder(BingoItemTags.CLIMBABLE).add(
            Items.LADDER,
            Items.SCAFFOLDING,
            Items.VINE,
            Items.GLOW_BERRIES,
            Items.WEEPING_VINES,
            Items.TWISTING_VINES
        );

        getOrCreateTagBuilder(BingoItemTags.FISH_BUCKETS).add(
            Items.COD_BUCKET,
            Items.PUFFERFISH_BUCKET,
            Items.SALMON_BUCKET,
            Items.TROPICAL_FISH_BUCKET
        );
        getOrCreateTagBuilder(BingoItemTags.FISHING_JUNK).add(
            Items.LILY_PAD,
            Items.BOWL,
            Items.LEATHER,
            Items.LEATHER_BOOTS,
            Items.ROTTEN_FLESH,
            Items.STICK,
            Items.POTION,
            Items.BONE,
            Items.INK_SAC,
            Items.TRIPWIRE_HOOK
        );
        getOrCreateTagBuilder(BingoItemTags.FISHING_TREASURE).add(
            Items.BOW,
            Items.ENCHANTED_BOOK,
            Items.NAME_TAG,
            Items.NAUTILUS_SHELL,
            Items.SADDLE
        );

        getOrCreateTagBuilder(BingoItemTags.LIVING_CORAL_BLOCKS).add(
            Items.TUBE_CORAL_BLOCK,
            Items.BRAIN_CORAL_BLOCK,
            Items.BUBBLE_CORAL_BLOCK,
            Items.FIRE_CORAL_BLOCK,
            Items.HORN_CORAL_BLOCK
        );

        getOrCreateTagBuilder(BingoItemTags.BONEMEALABLE)
            .forceAddTag(ItemTags.VILLAGER_PLANTABLE_SEEDS)
            .forceAddTag(ItemTags.SAPLINGS)
            .forceAddTag(ItemTags.TALL_FLOWERS)
            .add(
                Items.BAMBOO,
                Items.BIG_DRIPLEAF,
                Items.SMALL_DRIPLEAF,
                Items.GLOW_BERRIES,
                Items.COCOA_BEANS,
                Items.CRIMSON_FUNGUS,
                Items.WARPED_FUNGUS,
                Items.GLOW_LICHEN,
                Items.GRASS_BLOCK,
                Items.KELP,
                Items.MANGROVE_LEAVES,
                Items.MOSS_BLOCK,
                Items.RED_MUSHROOM,
                Items.BROWN_MUSHROOM,
                Items.NETHERRACK,
                Items.WARPED_NYLIUM,
                Items.CRIMSON_NYLIUM,
                Items.PINK_PETALS,
                Items.ROOTED_DIRT,
                Items.SEAGRASS,
                Items.SEA_PICKLE,
                Items.MELON_SEEDS,
                Items.PUMPKIN_SEEDS,
                Items.SWEET_BERRIES,
                Items.TALL_GRASS,
                Items.TWISTING_VINES,
                Items.WEEPING_VINES
            );

        // The vanilla flower tag contains weird stuff like cherry leaves that bees are attracted to, but they are not flowers
        getOrCreateTagBuilder(BingoItemTags.FLOWERS).forceAddTag(ItemTags.SMALL_FLOWERS).forceAddTag(ItemTags.TALL_FLOWERS);

        getOrCreateTagBuilder(BingoItemTags.DEAD_CORAL_BLOCKS).add(
            Items.DEAD_BRAIN_CORAL_BLOCK,
            Items.DEAD_BUBBLE_CORAL_BLOCK,
            Items.DEAD_FIRE_CORAL_BLOCK,
            Items.DEAD_HORN_CORAL_BLOCK,
            Items.DEAD_TUBE_CORAL_BLOCK
        );

        getOrCreateTagBuilder(BingoItemTags.BUCKETS)
            // These tags are missing on NeoForge (for now at least)
            .addOptionalTag(ConventionalItemTags.WATER_BUCKETS)
            .addOptionalTag(ConventionalItemTags.ENTITY_WATER_BUCKETS)
            .addOptionalTag(ConventionalItemTags.LAVA_BUCKETS)
            .addOptionalTag(ConventionalItemTags.MILK_BUCKETS)
            .addOptionalTag(ConventionalItemTags.EMPTY_BUCKETS)
            // WATER_BUCKETS
            .add(Items.WATER_BUCKET)
            // ENTITY_WATER_BUCKETS
            .add(Items.AXOLOTL_BUCKET)
            .add(Items.COD_BUCKET)
            .add(Items.PUFFERFISH_BUCKET)
            .add(Items.TADPOLE_BUCKET)
            .add(Items.TROPICAL_FISH_BUCKET)
            .add(Items.SALMON_BUCKET)
            // LAVA_BUCKETS
            .add(Items.LAVA_BUCKET)
            // MILK_BUCKETS
            .add(Items.MILK_BUCKET)
            // EMPTY_BUCKETS
            .add(Items.BUCKET);

        var glazedTerracottaBuilder = getOrCreateTagBuilder(BingoItemTags.GLAZED_TERRACOTTA);
        var concreteBuilder = getOrCreateTagBuilder(BingoItemTags.CONCRETE);
        for (DyeColor dyeColor : DyeColor.values()) {
            Item glazedTerracotta = BuiltInRegistries.ITEM.get(new ResourceLocation(dyeColor.getName() + "_glazed_terracotta"));
            glazedTerracottaBuilder.add(glazedTerracotta);
            Item concrete = BuiltInRegistries.ITEM.get(new ResourceLocation(dyeColor.getName() + "_concrete"));
            concreteBuilder.add(concrete);
        }

        final var vanillaMeatTag = BingoDataGenFabric.loadTag(ItemTags.MEAT, registries);

        var goldInNameBuilder = getOrCreateTagBuilder(BingoItemTags.GOLD_IN_NAME);
        var diamondInNameBuilder = getOrCreateTagBuilder(BingoItemTags.DIAMOND_IN_NAME);
        var meatBuilder = getOrCreateTagBuilder(BingoItemTags.MEAT);
        var notMeatBuilder = getOrCreateTagBuilder(BingoItemTags.NOT_MEAT);
        var bannerPatternsBuilder = getOrCreateTagBuilder(BingoItemTags.BANNER_PATTERNS);
        var slabBuilder = getOrCreateTagBuilder(BingoItemTags.SLABS);
        var stairsBuilder = getOrCreateTagBuilder(BingoItemTags.STAIRS);
        Pattern goldPattern = Pattern.compile("\\bGold(?:en)?\\b");
        Pattern diamondPattern = Pattern.compile("\\bDiamond\\b");
        for (Item item : BuiltInRegistries.ITEM) {
            String itemName = item.getDescription().getString();
            if (goldPattern.matcher(itemName).find()) {
                goldInNameBuilder.add(item);
            }
            if (diamondPattern.matcher(itemName).find()) {
                diamondInNameBuilder.add(item);
            }
            if (item.components().has(DataComponents.FOOD)) {
                if (vanillaMeatTag.contains(item) || FORCED_MEAT.contains(item)) {
                    meatBuilder.add(item);
                } else {
                    notMeatBuilder.add(item);
                }
            }
            switch (item) {
                case BannerPatternItem ignored -> bannerPatternsBuilder.add(item);
                case BlockItem blockItem -> {
                    Block block = blockItem.getBlock();
                    if (block instanceof SlabBlock) {
                        slabBuilder.add(item);
                    } else if (block instanceof StairBlock) {
                        stairsBuilder.add(item);
                    }
                }
                default -> {}
            }
        }

        copy(BingoBlockTags.BASIC_MINERAL_BLOCKS, BingoItemTags.BASIC_MINERAL_BLOCKS);
        copy(BingoBlockTags.ALL_MINERAL_BLOCKS, BingoItemTags.ALL_MINERAL_BLOCKS);
    }
}
