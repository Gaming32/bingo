package io.github.gaming32.bingo.fabric.datagen.tag;

import io.github.gaming32.bingo.data.tags.BingoBlockTags;
import io.github.gaming32.bingo.data.tags.BingoItemTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BannerPatternItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class BingoItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public BingoItemTagProvider(
        FabricDataOutput output,
        CompletableFuture<HolderLookup.Provider> registriesFuture,
        FabricTagProvider.BlockTagProvider blockTagProvider
    ) {
        super(output, registriesFuture, blockTagProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        getOrCreateTagBuilder(BingoItemTags.ALLOWED_HEADS).add(
            Items.SKELETON_SKULL,
            Items.PLAYER_HEAD,
            Items.ZOMBIE_HEAD,
            Items.CREEPER_HEAD,
            Items.DRAGON_HEAD,
            Items.PIGLIN_HEAD
        );

        getOrCreateTagBuilder(BingoItemTags.ARMOR_HELMETS).add(
            Items.LEATHER_HELMET,
            Items.TURTLE_HELMET,
            Items.CHAINMAIL_HELMET,
            Items.IRON_HELMET,
            Items.GOLDEN_HELMET,
            Items.DIAMOND_HELMET,
            Items.NETHERITE_HELMET
        );
        getOrCreateTagBuilder(BingoItemTags.ARMOR_CHESTPLATES).add(
            Items.LEATHER_CHESTPLATE,
            Items.CHAINMAIL_CHESTPLATE,
            Items.IRON_CHESTPLATE,
            Items.GOLDEN_CHESTPLATE,
            Items.DIAMOND_CHESTPLATE,
            Items.NETHERITE_CHESTPLATE
        );
        getOrCreateTagBuilder(BingoItemTags.ARMOR_LEGGINGS).add(
            Items.LEATHER_LEGGINGS,
            Items.CHAINMAIL_LEGGINGS,
            Items.IRON_LEGGINGS,
            Items.GOLDEN_LEGGINGS,
            Items.DIAMOND_LEGGINGS,
            Items.NETHERITE_LEGGINGS
        );
        getOrCreateTagBuilder(BingoItemTags.ARMOR_BOOTS).add(
            Items.LEATHER_BOOTS,
            Items.CHAINMAIL_BOOTS,
            Items.IRON_BOOTS,
            Items.GOLDEN_BOOTS,
            Items.DIAMOND_BOOTS,
            Items.NETHERITE_BOOTS
        );
        getOrCreateTagBuilder(BingoItemTags.ARMOR)
            .addOptionalTag(ConventionalItemTags.ARMORS)
            .addTag(BingoItemTags.ARMOR_HELMETS)
            .addTag(BingoItemTags.ARMOR_CHESTPLATES)
            .addTag(BingoItemTags.ARMOR_LEGGINGS)
            .addTag(BingoItemTags.ARMOR_BOOTS);

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

        var glazedTerracottaBuilder = getOrCreateTagBuilder(BingoItemTags.GLAZED_TERRACOTTA);
        var concreteBuilder = getOrCreateTagBuilder(BingoItemTags.CONCRETE);
        for (DyeColor dyeColor : DyeColor.values()) {
            Item glazedTerracotta = BuiltInRegistries.ITEM.get(new ResourceLocation(dyeColor.getName() + "_glazed_terracotta"));
            glazedTerracottaBuilder.add(glazedTerracotta);
            Item concrete = BuiltInRegistries.ITEM.get(new ResourceLocation(dyeColor.getName() + "_concrete"));
            concreteBuilder.add(concrete);
        }

        var bucketsBuilder = getOrCreateTagBuilder(BingoItemTags.BUCKETS)
            .addTag(ConventionalItemTags.WATER_BUCKETS)
            .addTag(ConventionalItemTags.ENTITY_WATER_BUCKETS)
            .addTag(ConventionalItemTags.LAVA_BUCKETS)
            .addTag(ConventionalItemTags.MILK_BUCKETS)
            .addTag(ConventionalItemTags.EMPTY_BUCKETS)
            .add(Items.MILK_BUCKET);
        var goldInNameBuilder = getOrCreateTagBuilder(BingoItemTags.GOLD_IN_NAME);
        var diamondInNameBuilder = getOrCreateTagBuilder(BingoItemTags.DIAMOND_IN_NAME);
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
            switch (item) {
                case BucketItem ignored -> bucketsBuilder.add(item);
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
