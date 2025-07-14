package io.github.gaming32.bingo.fabric.datagen.tag;

import io.github.gaming32.bingo.data.tags.bingo.BingoBlockTags;
import io.github.gaming32.bingo.data.tags.bingo.BingoItemTags;
import io.github.gaming32.bingo.fabric.datagen.BingoDataGenUtil;
import io.github.gaming32.bingo.mixin.fabric.CompositeEntryBaseAccessor;
import io.github.gaming32.bingo.mixin.fabric.LootItemAccessor;
import io.github.gaming32.bingo.mixin.fabric.LootTableAccessor;
import io.github.gaming32.bingo.mixin.fabric.MobBucketItemAccessor;
import io.github.gaming32.bingo.mixin.fabric.NestedLootTableAccessor;
import io.github.gaming32.bingo.mixin.fabric.TagEntryAccessor;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.CompositeEntryBase;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.entries.TagEntry;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
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

        // Cannot copy() because that can't copy from Vanilla's block tags, only our block tags
        BingoDataGenUtil.loadVanillaTag(BlockTags.CLIMBABLE, registries)
            .stream()
            .map(x -> x.value().asItem())
            .filter(x -> x != Items.AIR)
            .forEachOrdered(getOrCreateTagBuilder(BingoItemTags.CLIMBABLE)::add);

        // exclude fishing rod from these tags, makes it too easy
        addItemsFromLootTable(getOrCreateTagBuilder(BingoItemTags.FISHING_JUNK), BuiltInLootTables.FISHING_JUNK, registries, item -> item != Items.FISHING_ROD);
        addItemsFromLootTable(getOrCreateTagBuilder(BingoItemTags.FISHING_TREASURE), BuiltInLootTables.FISHING_TREASURE, registries, item -> item != Items.FISHING_ROD && item != Items.BOOK);
        getOrCreateTagBuilder(BingoItemTags.FISHING_TREASURE).add(Items.ENCHANTED_BOOK);

        getOrCreateTagBuilder(BingoItemTags.LIVING_CORAL_BLOCKS).add(
            Items.TUBE_CORAL_BLOCK,
            Items.BRAIN_CORAL_BLOCK,
            Items.BUBBLE_CORAL_BLOCK,
            Items.FIRE_CORAL_BLOCK,
            Items.HORN_CORAL_BLOCK
        );

        getOrCreateTagBuilder(BingoItemTags.DEAD_CORAL_BLOCKS).add(
            Items.DEAD_BRAIN_CORAL_BLOCK,
            Items.DEAD_BUBBLE_CORAL_BLOCK,
            Items.DEAD_FIRE_CORAL_BLOCK,
            Items.DEAD_HORN_CORAL_BLOCK,
            Items.DEAD_TUBE_CORAL_BLOCK
        );

        final var fishBucketsBuilder = getOrCreateTagBuilder(BingoItemTags.FISH_BUCKETS);

        final var trimTemplatesBuilder = getOrCreateTagBuilder(BingoItemTags.TRIM_TEMPLATES);
        VanillaRecipeProvider.smithingTrims()
            .map(VanillaRecipeProvider.TrimTemplate::template)
            .forEach(trimTemplatesBuilder::add);

        final var vanillaMeat = BingoDataGenUtil.loadVanillaTag(ItemTags.MEAT, registries);
        final var vanillaVillagerPlantableSeeds = BingoDataGenUtil.loadVanillaTag(
            ItemTags.VILLAGER_PLANTABLE_SEEDS,
            registries
        );
        final var vanillaSaplings = BingoDataGenUtil.loadVanillaTag(ItemTags.SAPLINGS, registries);

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
            switch (item.value()) {
                case MobBucketItem mobBucketItem -> {
                    EntityType<? extends Mob> entityType = ((MobBucketItemAccessor) mobBucketItem).getType();
                    Class<? extends Mob> entityTypeClass = BingoDataGenUtil.getEntityTypeClass(entityType);
                    if (entityTypeClass != null && entityType != EntityType.TADPOLE && AbstractFish.class.isAssignableFrom(entityTypeClass)) {
                        fishBucketsBuilder.add(item.key());
                    }
                }
                case BlockItem blockItem -> {
                    Block block = blockItem.getBlock();
                    if (
                        block instanceof BonemealableBlock &&
                            !vanillaVillagerPlantableSeeds.contains(item) &&
                            !vanillaSaplings.contains(item)
                    ) {
                        bonemealableBuilder.add(item.key());
                    }
                }
                default -> {
                }
            }
        });

        copy(BingoBlockTags.BASIC_MINERAL_BLOCKS, BingoItemTags.BASIC_MINERAL_BLOCKS);
        copy(BingoBlockTags.ALL_MINERAL_BLOCKS, BingoItemTags.ALL_MINERAL_BLOCKS);
    }

    private static ResourceKey<Item> item(String path) {
        return ResourceKey.create(Registries.ITEM, ResourceLocations.minecraft(path));
    }

    private static void addItemsFromLootTable(FabricTagBuilder tagBuilder, ResourceKey<LootTable> lootTable, HolderLookup.Provider registries, Predicate<Item> filter) {
        addItemsFromLootTable(tagBuilder, BingoDataGenUtil.loadVanillaLootTable(lootTable, registries), registries, filter);
    }

    private static void addItemsFromLootTable(FabricTagBuilder tagBuilder, LootTable lootTable, HolderLookup.Provider registries, Predicate<Item> filter) {
        for (LootPool pool : ((LootTableAccessor) lootTable).getPools()) {
            for (LootPoolEntryContainer entry : pool.entries) {
                addItemsFromLootEntry(tagBuilder, entry, registries, filter);
            }
        }
    }

    private static void addItemsFromLootEntry(FabricTagBuilder tagBuilder, LootPoolEntryContainer lootEntry, HolderLookup.Provider registries, Predicate<Item> filter) {
        switch (lootEntry) {
            case CompositeEntryBase composite -> {
                for (LootPoolEntryContainer child : ((CompositeEntryBaseAccessor) composite).getChildren()) {
                    addItemsFromLootEntry(tagBuilder, child, registries, filter);
                }
            }
            case LootItem lootItem -> {
                Item item = ((LootItemAccessor) lootItem).getItem().value();
                if (filter.test(item)) {
                    tagBuilder.add(item);
                }
            }
            case TagEntry tagEntry -> tagBuilder.forceAddTag(((TagEntryAccessor) tagEntry).getTag());
            case NestedLootTable nestedTable -> {
                LootTable nestedTableObj = ((NestedLootTableAccessor) nestedTable).getContents().map(
                    lootTableKey -> BingoDataGenUtil.loadVanillaLootTable(lootTableKey, registries),
                    Function.identity()
                );
                addItemsFromLootTable(tagBuilder, nestedTableObj, registries, filter);
            }
            case EmptyLootItem ignored -> {
            }
            default -> {
                throw new UnsupportedOperationException("Unknown loot entry type : " + lootEntry.getClass().getName());
            }
        }
    }
}
