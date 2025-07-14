package io.github.gaming32.bingo.fabric.datagen.tag;

import io.github.gaming32.bingo.data.tags.bingo.BingoBlockTags;
import io.github.gaming32.bingo.data.tags.bingo.BingoItemTags;
import io.github.gaming32.bingo.fabric.datagen.BingoDataGenFabric;
import io.github.gaming32.bingo.util.ResourceLocations;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;

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
        final var items = registries.lookupOrThrow(Registries.ITEM);

        getOrCreateTagBuilder(BingoItemTags.ALLOWED_HEADS).add(
            Items.SKELETON_SKULL,
            Items.PLAYER_HEAD,
            Items.ZOMBIE_HEAD,
            Items.CREEPER_HEAD,
            Items.DRAGON_HEAD,
            Items.PIGLIN_HEAD
        );

        // Cannot copy() because that can't copy from Vanilla's block tags, only our block tags
        BingoDataGenFabric.loadVanillaTag(BlockTags.CLIMBABLE, registries)
            .stream()
            .map(x -> x.value().asItem())
            .filter(x -> x != Items.AIR)
            .forEachOrdered(getOrCreateTagBuilder(BingoItemTags.CLIMBABLE)::add);

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

        getOrCreateTagBuilder(BingoItemTags.FLOWERS)
            .forceAddTag(ItemTags.SMALL_FLOWERS)
            .add(
                Items.SUNFLOWER,
                Items.LILAC,
                Items.PEONY,
                Items.ROSE_BUSH,
                Items.PITCHER_PLANT,
                Items.FLOWERING_AZALEA,
                Items.PINK_PETALS,
                Items.WILDFLOWERS,
                Items.CHORUS_FLOWER,
                Items.SPORE_BLOSSOM,
                Items.CACTUS_FLOWER
            );

        getOrCreateTagBuilder(BingoItemTags.DEAD_CORAL_BLOCKS).add(
            Items.DEAD_BRAIN_CORAL_BLOCK,
            Items.DEAD_BUBBLE_CORAL_BLOCK,
            Items.DEAD_FIRE_CORAL_BLOCK,
            Items.DEAD_HORN_CORAL_BLOCK,
            Items.DEAD_TUBE_CORAL_BLOCK
        );

        final var trimTemplatesBuilder = getOrCreateTagBuilder(BingoItemTags.TRIM_TEMPLATES);
        VanillaRecipeProvider.smithingTrims()
            .map(VanillaRecipeProvider.TrimTemplate::template)
            .forEach(trimTemplatesBuilder::add);

        final var vanillaMeat = BingoDataGenFabric.loadVanillaTag(ItemTags.MEAT, registries);
        final var vanillaVillagerPlantableSeeds = BingoDataGenFabric.loadVanillaTag(
            ItemTags.VILLAGER_PLANTABLE_SEEDS,
            registries
        );
        final var vanillaSaplings = BingoDataGenFabric.loadVanillaTag(ItemTags.SAPLINGS, registries);

        var goldInNameBuilder = getOrCreateTagBuilder(BingoItemTags.GOLD_IN_NAME);
        var diamondInNameBuilder = getOrCreateTagBuilder(BingoItemTags.DIAMOND_IN_NAME);
        var meatBuilder = getOrCreateTagBuilder(BingoItemTags.MEAT);
        var notMeatBuilder = getOrCreateTagBuilder(BingoItemTags.NOT_MEAT);
        var bannerPatternsBuilder = getOrCreateTagBuilder(BingoItemTags.BANNER_PATTERNS);
        var bonemealableBuilder = getOrCreateTagBuilder(BingoItemTags.BONEMEALABLE)
            .forceAddTag(ItemTags.VILLAGER_PLANTABLE_SEEDS)
            .forceAddTag(ItemTags.SAPLINGS);
        Pattern goldPattern = Pattern.compile("\\bGold(?:en)?\\b");
        Pattern diamondPattern = Pattern.compile("\\bDiamond\\b");
        items.listElements().forEach(item -> {
            String itemName = item.value().getName().getString();
            if (goldPattern.matcher(itemName).find()) {
                goldInNameBuilder.add(item.key());
            }
            if (diamondPattern.matcher(itemName).find()) {
                diamondInNameBuilder.add(item.key());
            }
            if (item.value().components().has(DataComponents.FOOD)) {
                if (vanillaMeat.contains(item) || FORCED_MEAT.contains(item.value())) {
                    meatBuilder.add(item.key());
                } else {
                    notMeatBuilder.add(item.key());
                }
            }
            if (item.value().components().get(DataComponents.PROVIDES_BANNER_PATTERNS) != null) {
                bannerPatternsBuilder.add(item.key());
            }
            if (item.value() instanceof BlockItem blockItem) {
                Block block = blockItem.getBlock();
                if (
                    block instanceof BonemealableBlock &&
                    !vanillaVillagerPlantableSeeds.contains(item) &&
                    !vanillaSaplings.contains(item)
                ) {
                    bonemealableBuilder.add(item.key());
                }
            }
        });

        copy(BingoBlockTags.BASIC_MINERAL_BLOCKS, BingoItemTags.BASIC_MINERAL_BLOCKS);
        copy(BingoBlockTags.ALL_MINERAL_BLOCKS, BingoItemTags.ALL_MINERAL_BLOCKS);
    }

    private static ResourceKey<Item> item(String path) {
        return ResourceKey.create(Registries.ITEM, ResourceLocations.minecraft(path));
    }
}
