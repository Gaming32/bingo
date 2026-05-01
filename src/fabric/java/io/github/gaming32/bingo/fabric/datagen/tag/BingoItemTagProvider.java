package io.github.gaming32.bingo.fabric.datagen.tag;

import io.github.gaming32.bingo.data.tags.bingo.BingoBlockTags;
import io.github.gaming32.bingo.data.tags.bingo.BingoItemTags;
import io.github.gaming32.bingo.fabric.datagen.BingoDataGenUtil;
import io.github.gaming32.bingo.mixin.fabric.CompositeEntryBaseAccessor;
import io.github.gaming32.bingo.mixin.fabric.LootItemAccessor;
import io.github.gaming32.bingo.mixin.fabric.LootPoolAccessor;
import io.github.gaming32.bingo.mixin.fabric.LootTableAccessor;
import io.github.gaming32.bingo.mixin.fabric.MobBucketItemAccessor;
import io.github.gaming32.bingo.mixin.fabric.NestedLootTableAccessor;
import io.github.gaming32.bingo.mixin.fabric.TagEntryAccessor;
import io.github.gaming32.bingo.util.Identifiers;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.level.block.BaseTorchBlock;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class BingoItemTagProvider extends FabricTagsProvider.ItemTagsProvider {
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
        FabricPackOutput output,
        CompletableFuture<HolderLookup.Provider> registriesFuture,
        FabricTagsProvider.BlockTagsProvider blockTagProvider
    ) {
        super(output, registriesFuture, blockTagProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        Map<ResourceKey<Item>, DataComponentMap> itemComponents = new HashMap<>();

        for (var pendingComponents : BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(registries)) {
            if (pendingComponents.key() == Registries.ITEM) {
                @SuppressWarnings("unchecked")
                var pendingItemComponents = (DataComponentInitializers.PendingComponents<Item>) pendingComponents;
                pendingItemComponents.forEach((item, components) -> itemComponents.put(item.key(), components));
            }
        }

        final var items = registries.lookupOrThrow(Registries.ITEM);

        // Cannot copy() because that can't copy from Vanilla's block tags, only our block tags
        BingoDataGenUtil.loadVanillaTag(BlockTags.CLIMBABLE, registries)
            .stream()
            .map(x -> x.value().asItem())
            .filter(x -> x != Items.AIR)
            .forEachOrdered(valueLookupBuilder(BingoItemTags.CLIMBABLE)::add);

        // exclude fishing rod from these tags, makes it too easy
        addItemsFromLootTable(valueLookupBuilder(BingoItemTags.FISHING_JUNK), BuiltInLootTables.FISHING_JUNK, registries, item -> item != Items.FISHING_ROD);
        addItemsFromLootTable(valueLookupBuilder(BingoItemTags.FISHING_TREASURE), BuiltInLootTables.FISHING_TREASURE, registries, item -> item != Items.FISHING_ROD && item != Items.BOOK);
        valueLookupBuilder(BingoItemTags.FISHING_TREASURE).add(Items.ENCHANTED_BOOK);

        valueLookupBuilder(BingoItemTags.LIVING_CORAL_BLOCKS).add(
            Items.TUBE_CORAL_BLOCK,
            Items.BRAIN_CORAL_BLOCK,
            Items.BUBBLE_CORAL_BLOCK,
            Items.FIRE_CORAL_BLOCK,
            Items.HORN_CORAL_BLOCK
        );

        valueLookupBuilder(BingoItemTags.DEAD_CORAL_BLOCKS).add(
            Items.DEAD_BRAIN_CORAL_BLOCK,
            Items.DEAD_BUBBLE_CORAL_BLOCK,
            Items.DEAD_FIRE_CORAL_BLOCK,
            Items.DEAD_HORN_CORAL_BLOCK,
            Items.DEAD_TUBE_CORAL_BLOCK
        );

        final var fishBucketsBuilder = valueLookupBuilder(BingoItemTags.FISH_BUCKETS);

        final var trimTemplatesBuilder = valueLookupBuilder(BingoItemTags.TRIM_TEMPLATES);
        VanillaRecipeProvider.smithingTrims()
            .map(VanillaRecipeProvider.TrimTemplate::template)
            .forEach(trimTemplatesBuilder::add);

        final var vanillaMeat = BingoDataGenUtil.loadVanillaTag(ItemTags.MEAT, registries);
        final var vanillaVillagerPlantableSeeds = BingoDataGenUtil.loadVanillaTag(
            ItemTags.VILLAGER_PLANTABLE_SEEDS,
            registries
        );
        final var vanillaSaplings = BingoDataGenUtil.loadVanillaTag(ItemTags.SAPLINGS, registries);

        var goldInNameBuilder = valueLookupBuilder(BingoItemTags.GOLD_IN_NAME);
        var copperInNameBuilder = valueLookupBuilder(BingoItemTags.COPPER_IN_NAME);
        var diamondInNameBuilder = valueLookupBuilder(BingoItemTags.DIAMOND_IN_NAME);
        var meatBuilder = valueLookupBuilder(BingoItemTags.MEAT);
        var notMeatBuilder = valueLookupBuilder(BingoItemTags.NOT_MEAT);
        var torchesBuilder = valueLookupBuilder(BingoItemTags.TORCHES);
        var bannerPatternsBuilder = valueLookupBuilder(BingoItemTags.BANNER_PATTERNS);
        var bonemealableBuilder = valueLookupBuilder(BingoItemTags.BONEMEALABLE)
            .forceAddTag(ItemTags.VILLAGER_PLANTABLE_SEEDS)
            .forceAddTag(ItemTags.SAPLINGS);
        var nautilusArmorBuilder = valueLookupBuilder(BingoItemTags.NAUTILUS_ARMOR);
        Pattern goldPattern = Pattern.compile("\\bGold(?:en)?\\b");
        Pattern copperPattern = Pattern.compile("\\bCopper\\b");
        Pattern diamondPattern = Pattern.compile("\\bDiamond\\b");
        items.listElements().forEach(item -> {
            String id = item.key().identifier().getPath();
            if (id.endsWith("_nautilus_armor")) {
                nautilusArmorBuilder.add(item.value());
            }

            String itemName = Component.translatable(item.value().getDescriptionId()).getString();
            if (goldPattern.matcher(itemName).find()) {
                goldInNameBuilder.add(item.value());
            }
            if (copperPattern.matcher(itemName).find()) {
                copperInNameBuilder.add(item.value());
            }
            if (diamondPattern.matcher(itemName).find()) {
                diamondInNameBuilder.add(item.value());
            }

            DataComponentMap components = itemComponents.getOrDefault(item.key(), DataComponentMap.EMPTY);

            if (components.has(DataComponents.FOOD) && components.has(DataComponents.CONSUMABLE)) {
                if (vanillaMeat.contains(item) || FORCED_MEAT.contains(item.value())) {
                    meatBuilder.add(item.value());
                } else {
                    notMeatBuilder.add(item.value());
                }
            }
            if (components.get(DataComponents.PROVIDES_BANNER_PATTERNS) != null) {
                bannerPatternsBuilder.add(item.value());
            }
            switch (item.value()) {
                case MobBucketItem mobBucketItem -> {
                    EntityType<? extends Mob> entityType = ((MobBucketItemAccessor) mobBucketItem).getType();
                    Class<? extends Mob> entityTypeClass = BingoDataGenUtil.getEntityTypeClass(entityType);
                    if (entityTypeClass != null && entityType != EntityType.TADPOLE && AbstractFish.class.isAssignableFrom(entityTypeClass)) {
                        fishBucketsBuilder.add(item.value());
                    }
                }
                case BlockItem blockItem -> {
                    Block block = blockItem.getBlock();
                    switch (block) {
                        case BaseTorchBlock ignored -> torchesBuilder.add(item.value());
                        case BonemealableBlock ignored -> {
                            if (!vanillaVillagerPlantableSeeds.contains(item) && !vanillaSaplings.contains(item)) {
                                bonemealableBuilder.add(item.value());
                            }
                        }
                        default -> {
                        }
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
        return ResourceKey.create(Registries.ITEM, Identifiers.minecraft(path));
    }

    private static void addItemsFromLootTable(TagAppender<Item, Item> tagAppender, ResourceKey<LootTable> lootTable, HolderLookup.Provider registries, Predicate<Item> filter) {
        addItemsFromLootTable(tagAppender, BingoDataGenUtil.loadVanillaLootTable(lootTable, registries), registries, filter);
    }

    private static void addItemsFromLootTable(TagAppender<Item, Item> tagAppender, LootTable lootTable, HolderLookup.Provider registries, Predicate<Item> filter) {
        for (LootPool pool : ((LootTableAccessor) lootTable).getPools()) {
            for (LootPoolEntryContainer entry : ((LootPoolAccessor) pool).getEntries()) {
                addItemsFromLootEntry(tagAppender, entry, registries, filter);
            }
        }
    }

    private static void addItemsFromLootEntry(TagAppender<Item, Item> tagAppender, LootPoolEntryContainer lootEntry, HolderLookup.Provider registries, Predicate<Item> filter) {
        switch (lootEntry) {
            case CompositeEntryBase composite -> {
                for (LootPoolEntryContainer child : ((CompositeEntryBaseAccessor) composite).getChildren()) {
                    addItemsFromLootEntry(tagAppender, child, registries, filter);
                }
            }
            case LootItem lootItem -> {
                Item item = ((LootItemAccessor) lootItem).getItem().value();
                if (filter.test(item)) {
                    tagAppender.add(item);
                }
            }
            case TagEntry tagEntry -> tagAppender.forceAddTag(((TagEntryAccessor) tagEntry).getTag());
            case NestedLootTable nestedTable -> {
                LootTable nestedTableObj = ((NestedLootTableAccessor) nestedTable).getContents().map(
                    lootTableKey -> BingoDataGenUtil.loadVanillaLootTable(lootTableKey, registries),
                    Function.identity()
                );
                addItemsFromLootTable(tagAppender, nestedTableObj, registries, filter);
            }
            case EmptyLootItem ignored -> {
            }
            default -> {
                throw new UnsupportedOperationException("Unknown loot entry type : " + lootEntry.getClass().getName());
            }
        }
    }
}
