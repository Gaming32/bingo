package io.github.gaming32.bingo.fabric.datagen;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoItemTags;
import io.github.gaming32.bingo.data.BingoSub;
import io.github.gaming32.bingo.data.BingoTags;
import io.github.gaming32.bingo.triggers.ArrowPressTrigger;
import io.github.gaming32.bingo.triggers.EquipItemTrigger;
import io.github.gaming32.bingo.triggers.ExperienceChangeTrigger;
import io.github.gaming32.bingo.triggers.TryUseItemTrigger;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.advancements.critereon.FishingRodHookedTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PickedUpItemTrigger;
import net.minecraft.advancements.critereon.PlayerInteractTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.advancements.critereon.RecipeCraftedTrigger;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.advancements.critereon.TameAnimalTrigger;
import net.minecraft.advancements.critereon.UsingItemTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class BingoGoalProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public BingoGoalProvider(FabricDataOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "bingo/goals");
    }

    @Override
    @NotNull
    public CompletableFuture<?> run(CachedOutput output) {
        Set<ResourceLocation> existingGoals = new HashSet<>();
        List<CompletableFuture<?>> generators = new ArrayList<>();

        Consumer<BingoGoal> goalAdder = goal -> {
            if (!existingGoals.add(goal.getId())) {
                throw new IllegalArgumentException("Duplicate goal " + goal.getId());
            } else {
                Path path = pathProvider.json(goal.getId());
                generators.add(DataProvider.saveStable(output, goal.serialize(), path));
            }
        };

        addGoals(goalAdder);

        return CompletableFuture.allOf(generators.toArray(CompletableFuture[]::new));
    }

    @Override
    @NotNull
    public String getName() {
        return "Bingo goals";
    }

    private static void addGoals(Consumer<BingoGoal> goalAdder) {
        addVeryEasyGoals(goalAdder);
        addEasyGoals(goalAdder);
        addMediumGoals(goalAdder);
    }

    private static void addVeryEasyGoals(Consumer<BingoGoal> goalAdder) {
        goalAdder.accept(obtainItemGoal(veryEasyId("cobblestone"), Items.COBBLESTONE, 32, 64)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("dirt"), Items.DIRT, 32, 64)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("redstone"), Items.REDSTONE).tags(BingoTags.OVERWORLD)
            .infrequency(2)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("lava_bucket"), Items.LAVA_BUCKET)
            .reactant("use_buckets")
            .infrequency(4)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("milk_bucket"), Items.MILK_BUCKET)
            .reactant("use_buckets")
            .tags(BingoTags.OVERWORLD)
            .infrequency(4)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("water_bucket"), Items.WATER_BUCKET)
            .reactant("use_buckets")
            .tags(BingoTags.OVERWORLD)
            .infrequency(4)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("fish_bucket"), Items.TROPICAL_FISH_BUCKET, ItemPredicate.Builder.item().of(BingoItemTags.FISH_BUCKETS))
            .name(Component.translatable("bingo.goal.fish_bucket"))
            .tooltip(Component.translatable("bingo.goal.fish_bucket.tooltip", Component.translatable("advancements.husbandry.tactical_fishing.title")))
            .antisynergy("fish_bucket")
            .reactant("use_buckets")
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD)
            .infrequency(4)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("andesite"), Items.ANDESITE, 16, 32)
            .infrequency(3)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("granite"), Items.GRANITE, 16, 32)
            .infrequency(3)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("diorite"), Items.DIORITE, 16, 32)
            .infrequency(3)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("iron_block"), Items.IRON_BLOCK)
            .infrequency(2)
            .difficulty(0)
            .build());
        goalAdder.accept(BingoGoal.builder(veryEasyId("poppies_dandelions"))
            .sub("poppies_count", new BingoSub.RandomBingoSub(MinMaxBounds.Ints.between(5, 25)))
            .sub("dandelions_count", new BingoSub.RandomBingoSub(MinMaxBounds.Ints.between(5, 25)))
            .criterion("poppy", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(Items.POPPY).withCount(MinMaxBounds.Ints.exactly(0)).build()),
                subber -> subber.sub("conditions.items.0.count", "poppies_count"))
            .criterion("dandelion", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(Items.DANDELION).withCount(MinMaxBounds.Ints.exactly(0)).build()),
                subber -> subber.sub("conditions.items.0.count", "dandelions_count"))
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.and",
                    Component.translatable("bingo.count", 0, Items.POPPY.getDescription()),
                    Component.translatable("bingo.count", 0, Items.DANDELION.getDescription())),
                subber -> subber.sub("with.0.with.0", "poppies_count").sub("with.1.with.0", "dandelions_count"))
            .icon(Items.POPPY)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainLevelsGoal(veryEasyId("levels"), 5, 7)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("note_block"), Items.NOTE_BLOCK, 5, 7)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("leaves"), Items.OAK_LEAVES, ItemPredicate.Builder.item().of(ItemTags.LEAVES), 32, 64)
            .name(Component.translatable("bingo.goal.leaves"))
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        // TODO: leaf cube
        // TODO: colors of wool
        goalAdder.accept(obtainItemGoal(veryEasyId("snowball"), Items.SNOWBALL, 8, 16)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        // TODO: slabs
        // TODO: stairs
        goalAdder.accept(obtainItemGoal(veryEasyId("diamond"), Items.DIAMOND)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("rotten_flesh"), Items.ROTTEN_FLESH, 5, 15)
            .infrequency(2)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("stone"), Items.STONE, 10, 32)
            .tooltip(Component.translatable("bingo.goal.stone.tooltip"))
            .infrequency(2)
            .reactant("use_furnace")
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("bread"), Items.BREAD, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("hay_block"), Items.HAY_BLOCK)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("flower_pot"), Items.FLOWER_POT)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("feather"), Items.FEATHER, 2, 10)
            .infrequency(2)
            .difficulty(0)
            .build());
        // TODO: sleep in a bed
        goalAdder.accept(obtainItemGoal(veryEasyId("charcoal"), Items.CHARCOAL)
            .reactant("use_furnace")
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("coal"), Items.COAL)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("fishing_rod"), Items.FISHING_ROD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("apple"), Items.APPLE)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("stick"), Items.STICK, 32, 64)
            .infrequency(2)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("kelp"), Items.KELP, 32, 64)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("cod"), Items.COD, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("salmon"), Items.SALMON, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        // TODO: different edible items
        // TODO: breed a set of mobs
        // TODO: crouch distance
        // TODO: fill all slots of campfire
        // TODO: change sign color
        // TODO: extinguish campfire
        goalAdder.accept(BingoGoal.builder(veryEasyId("never_pickup_crafting_tables"))
            .criterion("pickup", PickedUpItemTrigger.TriggerInstance.thrownItemPickedUpByPlayer(
                ContextAwarePredicate.ANY, ItemPredicate.Builder.item().of(Items.CRAFTING_TABLE).build(), ContextAwarePredicate.ANY))
            .tags(BingoTags.NEVER)
            .name(Component.translatable("bingo.goal.never_pickup_crafting_tables"))
            .tooltip(Component.translatable("bingo.goal.never_pickup_crafting_tables.tooltip"))
            .icon(Items.CRAFTING_TABLE)
            .difficulty(0)
            .build());
        // TODO: gold items
        goalAdder.accept(obtainItemGoal(veryEasyId("sand"), Items.SAND, 10, 32)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("sandstone"), Items.SANDSTONE, 5, 10)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("cut_sandstone"), Items.CUT_SANDSTONE, 5, 10)
            .setAntisynergy("sandstone")
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("paper"), Items.PAPER, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(BingoGoal.builder(veryEasyId("never_fish"))
            .criterion("use", TryUseItemTrigger.builder().item(ItemPredicate.Builder.item().of(Items.FISHING_ROD).build()).build())
            .tags(BingoTags.NEVER, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.never_fish"))
            .tooltip(Component.translatable("bingo.goal.never_fish.tooltip"))
            .icon(Items.FISHING_ROD)
            .catalyst("fishing")
            .difficulty(0)
            .build());
        // TODO: deplete hoe
        // TODO: bounce on bed
        // TODO: hang painting
        // TODO: fill composter

        for (String woodType : List.of("oak", "spruce", "birch", "dark_oak", "acacia")) {
            Item planksItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_planks"));
            goalAdder.accept(obtainItemGoal(veryEasyId(woodType + "_planks"), planksItem, 32, 64)
                .tags(BingoTags.OVERWORLD)
                .infrequency(25)
                .difficulty(0)
                .build());

            Item logItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_log"));
            goalAdder.accept(obtainItemGoal(veryEasyId(woodType + "_log"), logItem, 5, 15)
                .tags(BingoTags.OVERWORLD)
                .infrequency(25)
                .difficulty(0)
                .build());

            Item woodItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_wood"));
            goalAdder.accept(obtainItemGoal(veryEasyId(woodType + "_wood"), woodItem, 5, 10)
                .infrequency(25)
                .tags(BingoTags.OVERWORLD)
                .difficulty(0)
                .build());

            Item strippedWoodItem = BuiltInRegistries.ITEM.get(new ResourceLocation("stripped_" + woodType + "_wood"));
            goalAdder.accept(obtainItemGoal(veryEasyId("stripped_" + woodType + "_wood"), strippedWoodItem, 5, 10)
                .reactant("axe_use")
                .infrequency(25)
                .tags(BingoTags.OVERWORLD)
                .difficulty(0)
                .build());

            Item strippedLogItem = BuiltInRegistries.ITEM.get(new ResourceLocation("stripped_" + woodType + "_log"));
            goalAdder.accept(obtainItemGoal(veryEasyId("stripped_" + woodType + "_log"), strippedLogItem, 5, 15)
                .reactant("axe_use")
                .infrequency(25)
                .tags(BingoTags.OVERWORLD)
                .difficulty(0)
                .build());
        }
    }

    private static ResourceLocation veryEasyId(String id) {
        return new ResourceLocation(Bingo.MOD_ID, "very_easy/" + id);
    }

    private static void addEasyGoals(Consumer<BingoGoal> goalAdder) {
        // TODO: different fish
        // TODO: grow tree in nether
        // TODO: colors of terracotta
        goalAdder.accept(obtainItemGoal(easyId("mushroom_stew"), Items.MUSHROOM_STEW, 2, 5)
            .difficulty(1)
            .build());
        goalAdder.accept(BingoGoal.builder(easyId("shoot_button"))
            .criterion("obtain", ArrowPressTrigger.builder()
                .arrow(EntityPredicate.Builder.entity().of(EntityTypeTags.ARROWS).build())
                .buttonOrPlate(BlockPredicate.Builder.block().of(BlockTags.BUTTONS).build())
                .build())
            .tags(BingoTags.ACTION)
            .name(Component.translatable("bingo.goal.shoot_button"))
            .icon(Items.OAK_BUTTON)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("writable_book"), Items.WRITABLE_BOOK)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("flint"), Items.FLINT, 16, 64)
            .difficulty(1)
            .build());
        goalAdder.accept(eatEntireCake());
        goalAdder.accept(obtainItemGoal(easyId("pumpkin_pie"), Items.PUMPKIN_PIE)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        goalAdder.accept(BingoGoal.builder(easyId("fish_treasure_junk"))
            .criterion("treasure", FishingRodHookedTrigger.TriggerInstance.fishedItem(
                ItemPredicate.ANY, EntityPredicate.ANY, ItemPredicate.Builder.item().of(BingoItemTags.FISHING_TREASURE).build()))
            .criterion("junk", FishingRodHookedTrigger.TriggerInstance.fishedItem(
                ItemPredicate.ANY, EntityPredicate.ANY, ItemPredicate.Builder.item().of(BingoItemTags.FISHING_JUNK).build()))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.fish_treasure_junk"))
            .tooltip(Component.translatable("bingo.goal.fish_treasure_junk.tooltip"))
            .icon(Items.FISHING_ROD)
            .reactant("fishing")
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("coarse_dirt"), Items.COARSE_DIRT, 16, 64)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("clock"), Items.CLOCK, 2, 3)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("iron_block"), Items.IRON_BLOCK, 2, 4)
            .infrequency(2)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("gold_block"), Items.GOLD_BLOCK)
            .infrequency(2)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("golden_apple"), Items.GOLDEN_APPLE)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("bookshelf"), Items.BOOKSHELF, 2, 4)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        goalAdder.accept(BingoGoal.builder(easyId("never_wear_chestplates"))
            .criterion("equip", EquipItemTrigger.builder()
                .newItem(ItemPredicate.Builder.item().of(BingoItemTags.ARMOR_CHESTPLATES).build())
                .slots(EquipmentSlot.CHEST)
                .build())
            .tags(BingoTags.NEVER)
            .name(Component.translatable("bingo.goal.never_wear_chestplates"))
            .icon(Items.IRON_CHESTPLATE)
            .antisynergy("never_wear_armor")
            .catalyst("wear_armor")
            .difficulty(1)
            .build());
        goalAdder.accept(BingoGoal.builder(easyId("never_use_shields"))
            .criterion("use", new UsingItemTrigger.TriggerInstance(ContextAwarePredicate.ANY, ItemPredicate.Builder.item().of(BingoItemTags.SHIELDS).build()))
            .tags(BingoTags.NEVER)
            .name(Component.translatable("bingo.goal.never_use_shields"))
            .tooltip(Component.translatable("bingo.goal.never_use_shields.tooltip"))
            .icon(Items.SHIELD)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("jukebox"), Items.JUKEBOX)
            .difficulty(1)
            .build());
        // TODO: 3x3x3 cube of glass with lava in middle
        goalAdder.accept(obtainItemGoal(easyId("mossy_cobblestone"), Items.MOSSY_COBBLESTONE, 16, 32)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("cactus"), Items.CACTUS, 5, 15)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("tnt"), Items.TNT, 2, 3)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainLevelsGoal(easyId("levels"), 8, 15)
            .difficulty(1)
            .build());
        // TODO: create a snow golem
        goalAdder.accept(obtainItemGoal(easyId("note_block"), Items.NOTE_BLOCK, 5, 15)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("ink_sac"), Items.INK_SAC, 5, 15)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("bread"), Items.BREAD, 6, 10)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("hay_block"), Items.HAY_BLOCK, 2, 3)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        // TODO: colors of wool
        goalAdder.accept(obtainItemGoal(easyId("piston"), Items.PISTON)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        // TODO: full iron armor
        // TODO: full leather armor
        // TODO: fill cauldron with water
        // TODO: complete a map
        goalAdder.accept(obtainItemGoal(easyId("soul_sand"), Items.SOUL_SAND, 5, 10)
            .tags(BingoTags.NETHER)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("soul_soil"), Items.SOUL_SOIL, 5, 10)
            .tags(BingoTags.NETHER)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("pumpkin"), Items.PUMPKIN, 5, 10)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("carved_pumpkin"), Items.CARVED_PUMPKIN, 2, 5)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("vine"), Items.VINE, 10, 30)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME)
            .difficulty(1)
            .build());
        // TODO: different slabs
        // TODO: every sword
        // TODO: every pickaxe
        goalAdder.accept(obtainItemGoal(easyId("bricks"), Items.BRICKS, 16, 64)
            .reactant("use_furnace")
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("nether_bricks"), Items.NETHER_BRICKS, 16, 32)
            .reactant("use_furnace")
            .tags(BingoTags.NETHER)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("arrow"), Items.ARROW, 16, 64)
            .difficulty(1)
            .build());
        // TODO: try to sleep in nether
        goalAdder.accept(obtainItemGoal(easyId("fermented_spider_eye"), Items.FERMENTED_SPIDER_EYE)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        // TODO: different stairs
        goalAdder.accept(obtainItemGoal(easyId("ender_pearl"), Items.ENDER_PEARL, 2, 3)
            .infrequency(2)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("egg"), Items.EGG, 16, 16)
            .difficulty(1)
            .build());
        // TODO: hang 3 different 4x4 paintings
        goalAdder.accept(obtainItemGoal(easyId("bone_block"), Items.BONE_BLOCK, 5, 10)
            .difficulty(1)
            .build());
        // TODO: 2 creepers in the same boat
        // TODO: trade with a villager
        // TODO: different colored shields
        goalAdder.accept(obtainItemGoal(easyId("dead_bush"), Items.DEAD_BUSH)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("grass"), Items.GRASS, 15, 32)
            .difficulty(1)
            .tooltip(Component.translatable("bingo.goal.grass.tooltip"))
            .build());

        for (String dyeColor : List.of("cyan", "magenta", "red", "orange", "yellow", "green", "pink", "purple", "lime")) {
            Item dyeItem = BuiltInRegistries.ITEM.get(new ResourceLocation(dyeColor + "_dye"));
            goalAdder.accept(obtainItemGoal(easyId(dyeColor + "_dye"), dyeItem)
                .infrequency(10)
                .tags(BingoTags.OVERWORLD)
                .difficulty(1)
                .build());
        }

        goalAdder.accept(BingoGoal.builder(easyId("never_sleep"))
            .criterion("sleep", PlayerTrigger.TriggerInstance.sleptInBed())
            .tags(BingoTags.NEVER, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.never_sleep"))
            .icon(Items.RED_BED)
            .catalyst("sleep")
            .difficulty(1)
            .build());
        // TODO: grow a huge mushroom
        // TODO: water, lava and milk bucket
        // TODO: different flowers
        // TODO: colors of concrete
        // TODO: colors of glazed terracotta
        // TODO: colors of bed next to each other
        // TODO: finish where you spawned using compass
        goalAdder.accept(obtainItemGoal(easyId("stone"), Items.STONE, 32, 64)
            .tooltip(Component.translatable("bingo.goal.stone.tooltip"))
            .infrequency(2)
            .reactant("use_furnace")
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        // TODO: kill passive mobs with only fire
        // TODO: kill creeper with only fire
        goalAdder.accept(obtainItemGoal(easyId("iron_nugget"), Items.IRON_NUGGET, 32, 64)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("gold_nugget"), Items.GOLD_NUGGET, 32, 64)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("rotten_flesh"), Items.ROTTEN_FLESH, 16, 32)
            .infrequency(2)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("redstone"), Items.REDSTONE, 16, 64)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("golden_carrot"), Items.GOLDEN_CARROT)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        // TODO: rotten flesh, spider eye, bone, gunpowder and ender pearl
        goalAdder.accept(obtainItemGoal(easyId("feather"), Items.FEATHER, 32, 64)
            .infrequency(2)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("lily_pad"), Items.LILY_PAD, 2, 10)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("stick"), Items.STICK, 65, 128)
            .infrequency(2)
            .difficulty(1)
            .build());
        // TODO: 4 different colors of leather armor at the same time
        goalAdder.accept(obtainItemGoal(easyId("seagrass"), Items.SEAGRASS, 15, 32)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD)
            .difficulty(1)
            .build());

        for (String woodType : List.of("oak", "spruce", "birch", "dark_oak", "acacia")) {
            Item planksItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_planks"));
            goalAdder.accept(obtainItemGoal(easyId(woodType + "_planks"), planksItem, 65, 128)
                .tags(BingoTags.OVERWORLD)
                .infrequency(25)
                .difficulty(1)
                .build());

            Item logItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_log"));
            goalAdder.accept(obtainItemGoal(easyId(woodType + "_log"), logItem, 16, 32)
                .tags(BingoTags.OVERWORLD)
                .infrequency(25)
                .difficulty(1)
                .build());

            Item woodItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_wood"));
            goalAdder.accept(obtainItemGoal(easyId(woodType + "_wood"), woodItem, 11, 20)
                .infrequency(25)
                .tags(BingoTags.OVERWORLD)
                .difficulty(1)
                .build());

            Item strippedWoodItem = BuiltInRegistries.ITEM.get(new ResourceLocation("stripped_" + woodType + "_wood"));
            goalAdder.accept(obtainItemGoal(easyId("stripped_" + woodType + "_wood"), strippedWoodItem, 11, 20)
                .reactant("axe_use")
                .infrequency(25)
                .tags(BingoTags.OVERWORLD)
                .difficulty(1)
                .build());

            Item strippedLogItem = BuiltInRegistries.ITEM.get(new ResourceLocation("stripped_" + woodType + "_log"));
            goalAdder.accept(obtainItemGoal(easyId("stripped_" + woodType + "_log"), strippedLogItem, 16, 32)
                .reactant("axe_use")
                .infrequency(25)
                .tags(BingoTags.OVERWORLD)
                .difficulty(1)
                .build());
        }

        goalAdder.accept(obtainItemGoal(easyId("tropical_fish"), Items.TROPICAL_FISH)
            .tags(BingoTags.OVERWORLD, BingoTags.OCEAN)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("pufferfish"), Items.PUFFERFISH)
            .tags(BingoTags.OVERWORLD, BingoTags.OCEAN)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("cod"), Items.COD, 6, 15)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD, BingoTags.OCEAN)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("salmon"), Items.SALMON, 4, 7)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD, BingoTags.OCEAN)
            .difficulty(1)
            .build());
        goalAdder.accept(BingoGoal.builder(easyId("never_use_boat"))
            .criterion("use", new PlayerInteractTrigger.TriggerInstance(
                ContextAwarePredicate.ANY,
                ItemPredicate.ANY,
                EntityPredicate.wrap(EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(EntityType.BOAT)).build())))
            .tags(BingoTags.NEVER, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.never_use_boat"))
            .icon(Items.OAK_BOAT)
            .difficulty(1)
            .build());
        // TODO: get a fish into the nether
        goalAdder.accept(obtainItemGoal(easyId("dried_kelp_block"), Items.DRIED_KELP_BLOCK, 11, 20)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        // TODO: drown a zombie
        goalAdder.accept(obtainItemGoal(easyId("gunpowder"), Items.GUNPOWDER, 2, 5)
            .infrequency(2)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("spider_eye"), Items.SPIDER_EYE, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        // TODO: different edible items
        // TODO: breed 2-4 sets of mobs
        // TODO crouch distance
        // TODO: never use debug
        // TODO: ring bell from 10 blocks away
        // TODO: repair item with grindstone
        goalAdder.accept(obtainItemGoal(easyId("sweet_berries"), Items.SWEET_BERRIES, 2, 6)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME)
            .difficulty(1)
            .build());
        // TODO: banner pattern
        // TODO: drink sussy stew
        // TODO: give fox sword
        goalAdder.accept(obtainItemGoal(easyId("honey_bottle"), Items.HONEY_BOTTLE)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("honeycomb"), Items.HONEYCOMB, 3, 3)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("basalt"), Items.BASALT, 2, 6)
            .tags(BingoTags.NETHER)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("blackstone"), Items.BLACKSTONE, 2, 6)
            .tags(BingoTags.NETHER)
            .difficulty(1)
            .build());
        // TODO: fill 4 slots of soul campfire with porkchops
        goalAdder.accept(obtainItemGoal(easyId("soul_lantern"), Items.SOUL_LANTERN)
            .tags(BingoTags.NETHER)
            .difficulty(1)
            .build());
        // TODO: open door with target block from 10 blocks away
        goalAdder.accept(obtainItemGoal(easyId("carrot_on_a_stick"), Items.CARROT_ON_A_STICK)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        // TODO: barter with piglin
        // TODO: become nauseous
        // TODO: enchanted item
        // TODO: remove enchantment with grindstone
        // TODO: never use sword
        // TODO: carnivore
        // TODO: clean banner
        // TODO: 5-7 different gold items
        goalAdder.accept(obtainItemGoal(easyId("sand"), Items.SAND, 33, 64)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("sandstone"), Items.SANDSTONE, 11, 32)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("cut_sandstone"), Items.CUT_SANDSTONE, 11, 32)
            .setAntisynergy("sandstone")
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("paper"), Items.PAPER, 6, 15)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("coal_block"), Items.COAL_BLOCK, 3, 6)
            .reactant("never_coal")
            .difficulty(1)
            .build());
        goalAdder.accept(obtainItemGoal(easyId("apple"), Items.APPLE, 2, 5)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
        // TODO: tame horse
        // TODO: hatch chicken from egg
        // TODO: empty cauldron without buckets or bottles
        // TODO: sleep in villager's bed
        // TODO: set fire to villager's house
        goalAdder.accept(obtainItemGoal(easyId("emerald"), Items.EMERALD)
            .tags(BingoTags.OVERWORLD)
            .difficulty(1)
            .build());
    }

    private static ResourceLocation easyId(String id) {
        return new ResourceLocation(Bingo.MOD_ID, "easy/" + id);
    }

    private static BingoGoal eatEntireCake() {
        BingoGoal.Builder builder = BingoGoal.builder(easyId("eat_entire_cake"));
        for (int level = 0; level < 7; level++) {
            LootItemCondition block;
            if (level == 6) {
                block = LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.AIR).build();
            } else {
                block = LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.CAKE)
                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.BITES, level + 1))
                    .build();
            }
            ContextAwarePredicate location = ContextAwarePredicate.create(block);
            builder.criterion("level_" + level, new ItemUsedOnLocationTrigger.TriggerInstance(
                CriteriaTriggers.ITEM_USED_ON_BLOCK.getId(),
                ContextAwarePredicate.ANY,
                location));
        }
        return builder
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.eat_entire_cake"))
            .icon(Items.CAKE)
            .reactant("eat_non_meat")
            .difficulty(1)
            .build();
    }

    private static void addMediumGoals(Consumer<BingoGoal> goalAdder) {
        // TODO: different edible items
        goalAdder.accept(obtainItemGoal(mediumId("beetroot_soup"), Items.BEETROOT_SOUP)
            .tags(BingoTags.OVERWORLD)
            .difficulty(2)
            .build());
        goalAdder.accept(BingoGoal.builder(mediumId("potted_cactus"))
            .criterion("pot", new ItemUsedOnLocationTrigger.TriggerInstance(
                CriteriaTriggers.ITEM_USED_ON_BLOCK.getId(),
                ContextAwarePredicate.ANY,
                ContextAwarePredicate.create(LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.POTTED_CACTUS).build())))
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.potted_cactus"))
            .icon(Items.CACTUS)
            .difficulty(2)
            .build());
        // TODO: detonate TNT minecart
        goalAdder.accept(obtainItemGoal(mediumId("magma_block"), Items.MAGMA_BLOCK, 10, 30)
            .difficulty(2)
            .build());
        goalAdder.accept(obtainItemGoal(mediumId("damaged_anvil"), Items.DAMAGED_ANVIL)
            .difficulty(2)
            .build());
        goalAdder.accept(obtainItemGoal(mediumId("melon_slice"), Items.MELON_SLICE, 16, 64)
            .tags(BingoTags.OVERWORLD)
            .difficulty(2)
            .build());
        goalAdder.accept(BingoGoal.builder(mediumId("never_wear_armor"))
            .criterion("equip", EquipItemTrigger.builder().newItem(ItemPredicate.Builder.item().of(BingoItemTags.ARMOR).build()).build())
            .tags(BingoTags.NEVER)
            .name(Component.translatable("bingo.goal.never_wear_armor"))
            .icon(Items.DIAMOND_CHESTPLATE)
            .antisynergy("never_wear_armor")
            .catalyst("wear_armor")
            .difficulty(2)
            .build());
        // TODO: get a skeleton's bow
        goalAdder.accept(obtainItemGoal(mediumId("diamond_block"), Items.DIAMOND_BLOCK)
            .infrequency(2)
            .difficulty(2)
            .build());
        goalAdder.accept(obtainItemGoal(mediumId("lapis_block"), Items.LAPIS_BLOCK, 2, 3)
            .tags(BingoTags.OVERWORLD)
            .difficulty(2)
            .build());
        // TODO: different saplings
        goalAdder.accept(BingoGoal.builder(mediumId("tame_wolf"))
            .criterion("obtain", TameAnimalTrigger.TriggerInstance.tamedAnimal(EntityPredicate.Builder.entity().of(EntityType.WOLF).build()))
            .tags(BingoTags.STAT, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.tame_wolf"))
            .icon(Items.BONE)
            .difficulty(2)
            .build());
        goalAdder.accept(obtainItemGoal(mediumId("fire_charge"), Items.FIRE_CHARGE, 6, 6)
            .tags(BingoTags.NETHER, BingoTags.COMBAT)
            .difficulty(2)
            .build());
        goalAdder.accept(obtainItemGoal(mediumId("magma_cream"), Items.MAGMA_CREAM, 2, 3)
            .tags(BingoTags.NETHER, BingoTags.COMBAT)
            .difficulty(2)
            .build());
        // TODO: create iron golem
        goalAdder.accept(obtainItemGoal(mediumId("ender_eye"), Items.ENDER_EYE)
            .reactant("pacifist")
            .tags(BingoTags.NETHER, BingoTags.COMBAT)
            .difficulty(2)
            .build());
        goalAdder.accept(obtainItemGoal(mediumId("rabbit_stew"), Items.RABBIT_STEW)
            .tags(BingoTags.OVERWORLD)
            .difficulty(2)
            .build());

        goalAdder.accept(potionGoal("fire_resistance_potion", Potions.FIRE_RESISTANCE, Potions.LONG_FIRE_RESISTANCE)
            .build());
        goalAdder.accept(potionGoal("healing_potion", Potions.HEALING, Potions.STRONG_HEALING)
            .tags(BingoTags.OVERWORLD, BingoTags.COMBAT)
            .build());
        goalAdder.accept(potionGoal("posion_potion", Potions.POISON, Potions.LONG_POISON, Potions.STRONG_POISON)
            .tags(BingoTags.OVERWORLD, BingoTags.COMBAT)
            .build());
        goalAdder.accept(potionGoal("harming_potion", Potions.HARMING, Potions.STRONG_HARMING)
            .tags(BingoTags.OVERWORLD, BingoTags.COMBAT)
            .build());
        goalAdder.accept(potionGoal("regeneration_potion", Potions.REGENERATION, Potions.LONG_REGENERATION, Potions.STRONG_REGENERATION)
            .tags(BingoTags.COMBAT)
            .build());
        goalAdder.accept(potionGoal("slowness_potion", Potions.SLOWNESS, Potions.LONG_SLOWNESS, Potions.STRONG_SLOWNESS)
            .tags(BingoTags.OVERWORLD, BingoTags.COMBAT)
            .build());
        goalAdder.accept(potionGoal("strength_potion", Potions.STRENGTH, Potions.LONG_STRENGTH, Potions.STRONG_STRENGTH)
            .tags(BingoTags.COMBAT)
            .build());
        goalAdder.accept(potionGoal("swiftness_potion", Potions.SWIFTNESS, Potions.LONG_SWIFTNESS, Potions.STRONG_SWIFTNESS)
            .tags(BingoTags.OVERWORLD, BingoTags.COMBAT)
            .build());
        goalAdder.accept(potionGoal("weakness_potion", Potions.WEAKNESS, Potions.LONG_WEAKNESS)
            .tags(BingoTags.OVERWORLD, BingoTags.COMBAT)
            .build());
        goalAdder.accept(potionGoal("leaping_potion", Potions.LEAPING, Potions.LONG_LEAPING, Potions.STRONG_LEAPING)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD, BingoTags.COMBAT)
            .build());
        goalAdder.accept(potionGoal("slow_falling_potion", Potions.SLOW_FALLING, Potions.LONG_SLOW_FALLING)
            .tags(BingoTags.COMBAT, BingoTags.OVERWORLD)
            .build());
        goalAdder.accept(potionGoal("turtle_master_potion", Potions.TURTLE_MASTER, Potions.LONG_TURTLE_MASTER, Potions.STRONG_TURTLE_MASTER)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD, BingoTags.COMBAT)
            .build());

        // TODO: finish by jumping from top to bottom of world
        // TODO: vegetarian
        // TODO: kill self with arrow
        // TODO: get a whilst trying to escape death
        // TODO: finish on top of the world
        // TODO: kill hostile mob with gravel
        // TODO: kill hostile mob with sand
        // TODO: put carpet on llama
        // TODO: activate a (4-6)x(4-6) nether portal
        goalAdder.accept(obtainItemGoal(mediumId("obsidian"), Items.OBSIDIAN, 3, 10)
            .difficulty(2)
            .build());
        goalAdder.accept(obtainItemGoal(mediumId("iron_block"), Items.IRON_BLOCK, 5, 7)
            .infrequency(2)
            .difficulty(2)
            .build());
        goalAdder.accept(obtainItemGoal(mediumId("gold_block"), Items.GOLD_BLOCK, 2, 4)
            .infrequency(2)
            .difficulty(2)
            .build());
        goalAdder.accept(obtainItemGoal(mediumId("daylight_detector"), Items.DAYLIGHT_DETECTOR)
            .tags(BingoTags.OVERWORLD, BingoTags.NETHER)
            .difficulty(2)
            .build());
        // TODO: enchanted golden sword
        // TODO: colors of wool
        // TODO: colors of terracotta
        // TODO: colors of glazed terracotta
        // TODO: colors of concrete
        // TODO: colors of bed next to each other
        // TODO: power redstone lamp
        // TODO: different flowers
        // TODO: put zombified piglin in water
        // TODO: place an iron, gold, diamond block on top of each other
        // TODO: kill hostile mob with anvil
        goalAdder.accept(obtainLevelsGoal(mediumId("levels"), 16, 26)
            .infrequency(2)
            .difficulty(2)
            .build());
        // TODO: different seeds
        // TODO: 4 different armor types
        // TODO: fill hopper with 320 items
        goalAdder.accept(obtainItemGoal(mediumId("red_nether_bricks"), Items.RED_NETHER_BRICKS, 16, 32)
            .reactant("use_furnace")
            .tags(BingoTags.NETHER)
            .difficulty(2)
            .build());
        goalAdder.accept(obtainItemGoal(mediumId("spectral_arrow"), Items.SPECTRAL_ARROW, 16, 32)
            .tags(BingoTags.NETHER)
            .difficulty(2)
            .build());
        goalAdder.accept(obtainItemGoal(mediumId("rotten_flesh"), Items.ROTTEN_FLESH, 33, 64)
            .infrequency(2)
            .difficulty(2)
            .build());
        goalAdder.accept(obtainItemGoal(mediumId("ink_sac"), Items.INK_SAC, 16, 32)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(2)
            .build());
        goalAdder.accept(obtainItemGoal(mediumId("slime_ball"), Items.SLIME_BALL, 5, 9)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .difficulty(2)
            .build());
        // TODO: lead on rabbit
        goalAdder.accept(obtainItemGoal(mediumId("firework_star"), Items.FIREWORK_STAR)
            .tags(BingoTags.OVERWORLD)
            .difficulty(2)
            .build());
        // TODO: hang mob with lead
        goalAdder.accept(obtainItemGoal(mediumId("blaze_rod"), Items.BLAZE_ROD)
            .reactant("pacifist")
            .tags(BingoTags.NETHER, BingoTags.COMBAT)
            .difficulty(2)
            .build());
        goalAdder.accept(obtainItemGoal(mediumId("ghast_tear"), Items.GHAST_TEAR)
            .reactant("pacifist")
            .tags(BingoTags.NETHER, BingoTags.COMBAT)
            .difficulty(2)
            .build());
        // TODO: never use coal
        goalAdder.accept(obtainItemGoal(mediumId("glowstone_dust"), Items.GLOWSTONE_DUST, 32, 64)
            .tags(BingoTags.NETHER)
            .difficulty(2)
            .build());
        goalAdder.accept(obtainItemGoal(mediumId("item_frame"), Items.ITEM_FRAME, 10, 32)
            .difficulty(2)
            .build());
        // TODO: different diamond items
        goalAdder.accept(obtainItemGoal(mediumId("prismarine_crystals"), Items.PRISMARINE_CRYSTALS, 2, 4)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD)
            .difficulty(2)
            .build());
        // TODO: dig straight down to bedrock from sea level (1x1 hole)
        // TODO: deplete diamond sword
        goalAdder.accept(obtainItemGoal(mediumId("saddle"), Items.SADDLE)
            .difficulty(2)
            .build());
        // TODO: give mob hat
        goalAdder.accept(obtainItemGoal(mediumId("heart_of_the_sea"), Items.HEART_OF_THE_SEA)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD)
            .difficulty(2)
            .build());
        goalAdder.accept(obtainItemGoal(mediumId("phantom_membrane"), Items.PHANTOM_MEMBRANE)
            .reactant("pacifist")
            .tags(BingoTags.COMBAT, BingoTags.OVERWORLD)
            .difficulty(2)
            .build());
        // TODO: add marker to map
        // TODO: water, lava, milk, fish bucket
        // TODO: leash dolphin to fence
        goalAdder.accept(obtainItemGoal(mediumId("dried_kelp_block"), Items.DRIED_KELP_BLOCK, 21, 32)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD)
            .difficulty(2)
            .build());
        goalAdder.accept(obtainItemGoal(mediumId("gunpowder"), Items.GUNPOWDER, 6, 15)
            .infrequency(2)
            .tags(BingoTags.COMBAT)
            .difficulty(2)
            .build());
        goalAdder.accept(obtainItemGoal(mediumId("spider_eye"), Items.SPIDER_EYE, 6, 15)
            .infrequency(2)
            .tags(BingoTags.COMBAT, BingoTags.OVERWORLD)
            .difficulty(2)
            .build());
        goalAdder.accept(obtainItemGoal(mediumId("ender_pearl"), Items.ENDER_PEARL, 4, 6)
            .infrequency(2)
            .tags(BingoTags.COMBAT)
            .difficulty(2)
            .build());
        // TODO: never use axe
        // TODO: enchant an item
        // TODO: blue shield with white flower charge pattern
        // TODO: tame a cat
        // TODO: breed mobs
        // TODO: crouch distance
        // TODO: kill n mobs
        goalAdder.accept(obtainItemGoal(mediumId("seagrass"), Items.SEAGRASS, 33, 64)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD)
            .difficulty(2)
            .build());
        // TODO: kill iron golem
        // TODO: kill mob with end crystal
        goalAdder.accept(BingoGoal.builder(mediumId("never_craft_sticks"))
            .criterion("craft", RecipeCraftedTrigger.TriggerInstance.craftedItem(new ResourceLocation("stick")))
            .tags(BingoTags.NEVER, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.never_craft_sticks"))
            .icon(Items.STICK)
            .difficulty(2)
            .build());
        // TODO: light campfire from 10 blocks away
        // TODO: max scale map
        // TODO: ignite TNT with lectern
        // TODO: kill hostile mob with berry bush
        // TODO: get pillager's crossbow
        ItemStack ominousBanner = Raid.getLeaderBannerInstance();
        goalAdder.accept(obtainItemGoal(mediumId("ominous_banner"), ominousBanner, ItemPredicate.Builder.item().of(ominousBanner.getItem()).hasNbt(ominousBanner.getTag()))
            .antisynergy("ominous_banner")
            .name(ominousBanner.getHoverName())
            .reactant("pacifist")
            .tags(BingoTags.COMBAT, BingoTags.OVERWORLD)
            .difficulty(2)
            .build());
        // TODO: gain a fox's trust
        goalAdder.accept(obtainItemGoal(mediumId("honey_block"), Items.HONEY_BLOCK)
            .setAntisynergy("honey")
            .infrequency(2)
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .difficulty(2)
            .build());
        goalAdder.accept(obtainItemGoal(mediumId("honeycomb_block"), Items.HONEYCOMB_BLOCK, 3, 3)
            .setAntisynergy("honeycomb")
            .infrequency(2)
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .difficulty(2)
            .build());
        // TODO: repair iron golem
        // TODO: grow tree with benis attached

        for (String woodType : List.of("warped", "crimson")) {
            Item stemItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_stem"));
            goalAdder.accept(obtainItemGoal(mediumId(woodType + "_stem"), stemItem, 16, 32)
                .infrequency(25)
                .tags(BingoTags.NETHER)
                .difficulty(2)
                .build());

            Item strippedStemItem = BuiltInRegistries.ITEM.get(new ResourceLocation("stripped_" + woodType + "_stem"));
            goalAdder.accept(obtainItemGoal(mediumId("stripped_" + woodType + "_stem"), strippedStemItem, 16, 32)
                .infrequency(25)
                .tags(BingoTags.NETHER)
                .difficulty(2)
                .build());

            Item hyphaeItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_hyphae"));
            goalAdder.accept(obtainItemGoal(mediumId(woodType + "_hyphae"), hyphaeItem, 16, 32)
                .infrequency(25)
                .tags(BingoTags.NETHER)
                .difficulty(2)
                .build());

            Item strippedHyphaeItem = BuiltInRegistries.ITEM.get(new ResourceLocation("stripped_" + woodType + "_hyphae"));
            goalAdder.accept(obtainItemGoal(mediumId("stripped_" + woodType + "_hyphae"), strippedHyphaeItem, 16, 32)
                .infrequency(25)
                .tags(BingoTags.NETHER)
                .difficulty(2)
                .build());

            Item planksItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_planks"));
            goalAdder.accept(obtainItemGoal(mediumId(woodType + "_planks"), planksItem, 16, 32)
                .infrequency(25)
                .tags(BingoTags.NETHER)
                .difficulty(2)
                .build());
        }

        goalAdder.accept(obtainItemGoal(mediumId("quartz_block"), Items.QUARTZ_BLOCK)
            .tags(BingoTags.NETHER)
            .difficulty(2)
            .build());
        // TODO: try to use respawn anchor in overworld
        goalAdder.accept(obtainItemGoal(mediumId("warped_fungus_on_a_stick"), Items.WARPED_FUNGUS_ON_A_STICK)
            .tags(BingoTags.NETHER)
            .difficulty(2)
            .build());
        // TODO: convert hoglin into zoglin
        // TODO: ride strider
        // TODO: damage strider with water
        goalAdder.accept(obtainItemGoal(mediumId("bamboo"), Items.BAMBOO, 6, 15)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME)
            .difficulty(2)
            .build());
        goalAdder.accept(obtainItemGoal(mediumId("crying_obsidian"), Items.CRYING_OBSIDIAN)
            .difficulty(2)
            .build());
        // TODO: kill self with ender pearl
        goalAdder.accept(obtainItemGoal(mediumId("grass_block"), Items.GRASS_BLOCK)
            .tooltip(Component.translatable("bingo.goal.grass_block.tooltip"))
            .tags(BingoTags.OVERWORLD)
            .difficulty(2)
            .build());
        // TODO: bounce on slime block
        // TODO: full gold armor
        goalAdder.accept(obtainItemGoal(mediumId("brown_wool"), Items.BROWN_WOOL)
            .tags(BingoTags.OVERWORLD)
            .difficulty(2)
            .build());
        // TODO: grow huge nether fungus
        // TODO: put chest on donkey
        goalAdder.accept(BingoGoal.builder(mediumId("never_place_torches"))
            .criterion("place", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.TORCH))
            .tags(BingoTags.NEVER)
            .name(Component.translatable("bingo.goal.never_place_torches"))
            .tooltip(Component.translatable("bingo.goal.never_place_torches.tooltip"))
            .icon(Items.TORCH)
            .difficulty(2)
            .build());
        goalAdder.accept(obtainItemGoal(mediumId("scute"), Items.SCUTE)
            .setAntisynergy("turtle_shell")
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD)
            .difficulty(2)
            .build());
    }

    private static ResourceLocation mediumId(String id) {
        return new ResourceLocation(Bingo.MOD_ID, "medium/" + id);
    }

    private static BingoGoal.Builder potionGoal(String id, Potion... potions) {
        ItemStack potionItem = PotionUtils.setPotion(new ItemStack(Items.POTION), potions[0]);
        BingoGoal.Builder builder = obtainItemGoal(
                mediumId(id),
                potionItem,
                Arrays.stream(potions).map(potion -> ItemPredicate.Builder.item().of(Items.POTION).isPotion(potion)).toArray(ItemPredicate.Builder[]::new))
            .antisynergy(id)
            .name(Items.POTION.getName(potionItem))
            .infrequency(12)
            .tags(BingoTags.NETHER)
            .difficulty(2);
        if (potions[0] != Potions.FIRE_RESISTANCE) {
            builder.reactant("pacifist");
        }
        return builder;
    }

    private static ResourceLocation hardId(String id) {
        return new ResourceLocation(Bingo.MOD_ID, "hard/" + id);
    }

    private static ResourceLocation veryHardId(String id) {
        return new ResourceLocation(Bingo.MOD_ID, "very_hard/" + id);
    }

    public static BingoGoal.Builder obtainItemGoal(ResourceLocation id, ItemLike item) {
        return obtainItemGoal(id, item, ItemPredicate.Builder.item().of(item))
            .antisynergy(BuiltInRegistries.ITEM.getKey(item.asItem()).getPath())
            .name(item.asItem().getDescription());
    }

    private static BingoGoal.Builder obtainItemGoal(ResourceLocation id, ItemLike icon, ItemPredicate.Builder item) {
        return obtainItemGoal(id, new ItemStack(icon), item);
    }

    private static BingoGoal.Builder obtainItemGoal(ResourceLocation id, ItemStack icon, ItemPredicate.Builder... items) {
        BingoGoal.Builder builder = BingoGoal.builder(id);
        if (items.length == 1) {
            builder.criterion("obtain", InventoryChangeTrigger.TriggerInstance.hasItems(items[0].build()));
        } else {
            List<String> requirements = new ArrayList<>(items.length);
            for (int i = 0; i < items.length; i++) {
                builder.criterion("obtain_" + i, InventoryChangeTrigger.TriggerInstance.hasItems(items[i].build()));
                requirements.add("obtain_" + i);
            }
            builder.requirements(requirements);
        }
        return builder
            .tags(BingoTags.ITEM)
            .icon(icon);
    }

    public static BingoGoal.Builder obtainItemGoal(ResourceLocation id, ItemLike item, int minCount, int maxCount) {
        if (minCount == maxCount) {
            return obtainItemGoal(id, item, ItemPredicate.Builder.item().of(item), minCount, maxCount)
                .antisynergy(BuiltInRegistries.ITEM.getKey(item.asItem()).getPath())
                .name(Component.translatable("bingo.count", minCount, item.asItem().getDescription()));
        }
        return obtainItemGoal(id, item, ItemPredicate.Builder.item().of(item), minCount, maxCount)
            .antisynergy(BuiltInRegistries.ITEM.getKey(item.asItem()).getPath())
            .name(Component.translatable("bingo.count", 0, item.asItem().getDescription()),
                subber -> subber.sub("with.0", "count"));
    }

    private static BingoGoal.Builder obtainItemGoal(ResourceLocation id, ItemLike icon, ItemPredicate.Builder item, int minCount, int maxCount) {
        if (minCount == maxCount) {
            return BingoGoal.builder(id)
                .criterion("obtain", InventoryChangeTrigger.TriggerInstance.hasItems(item.withCount(MinMaxBounds.Ints.exactly(minCount)).build()))
                .tags(BingoTags.ITEM)
                .icon(new ItemStack(icon, minCount));
        }
        return BingoGoal.builder(id)
            .sub("count", new BingoSub.RandomBingoSub(MinMaxBounds.Ints.between(minCount, maxCount)))
            .criterion("obtain", InventoryChangeTrigger.TriggerInstance.hasItems(
                item.withCount(MinMaxBounds.Ints.atLeast(0)).build()),
                subber -> subber.sub("conditions.items.0.count.min", "count"))
            .tags(BingoTags.ITEM)
            .icon(icon, subber -> subber.sub("count", "count"));
    }

    private static BingoGoal.Builder obtainLevelsGoal(ResourceLocation id, int minLevels, int maxLevels) {
        return BingoGoal.builder(id)
            .sub("count", new BingoSub.RandomBingoSub(MinMaxBounds.Ints.between(minLevels, maxLevels)))
            .criterion("obtain", ExperienceChangeTrigger.builder().levels(MinMaxBounds.Ints.atLeast(0)).build(),
                subber -> subber.sub("conditions.levels.min", "count"))
            .tags(BingoTags.STAT)
            .name(Component.translatable("bingo.goal.levels", 0), subber -> subber.sub("with.0", "count"))
            .icon(Items.EXPERIENCE_BOTTLE, subber -> subber.sub("count", "count"))
            .infrequency(2)
            .antisynergy("levels");
    }
}
