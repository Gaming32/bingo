package io.github.gaming32.bingo.fabric.datagen;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.conditions.BlockPatternCondition;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoSub;
import io.github.gaming32.bingo.data.BingoTags;
import io.github.gaming32.bingo.data.tags.BingoBlockTags;
import io.github.gaming32.bingo.data.tags.BingoItemTags;
import io.github.gaming32.bingo.triggers.EnchantedItemTrigger;
import io.github.gaming32.bingo.triggers.*;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
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
        addHardGoals(goalAdder);
        addVeryHardGoals(goalAdder);
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
            .sub("poppies_count", BingoSub.random(5, 25))
            .sub("dandelions_count", BingoSub.random(5, 25))
            .criterion("poppy", TotalCountInventoryChangeTrigger.builder().items(ItemPredicate.Builder.item().of(Items.POPPY).withCount(MinMaxBounds.Ints.exactly(0)).build()).build(),
                subber -> subber.sub("conditions.items.0.count", "poppies_count"))
            .criterion("dandelion", TotalCountInventoryChangeTrigger.builder().items(ItemPredicate.Builder.item().of(Items.DANDELION).withCount(MinMaxBounds.Ints.exactly(0)).build()).build(),
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
        goalAdder.accept(BingoGoal.builder(veryEasyId("sleep_in_bed"))
            .criterion("sleep", PlayerTrigger.TriggerInstance.sleptInBed())
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.sleep_in_bed"))
            .icon(Items.RED_BED)
            .reactant("sleep")
            .difficulty(0)
            .build());
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
        goalAdder.accept(BingoGoal.builder(veryEasyId("breed_mobs"))
            .criterion("breed", BredAnimalsTrigger.TriggerInstance.bredAnimals())
            .name(Component.translatable("bingo.goal.breed_mobs"))
            .tooltip(Component.translatable("bingo.goal.breed_mobs.tooltip"))
            .antisynergy("breed_animals")
            .infrequency(2)
            .tags(BingoTags.ACTION, BingoTags.STAT)
            .difficulty(0)
            .icon(Items.WHEAT_SEEDS)
            .build()
        );
        goalAdder.accept(crouchDistanceGoal(veryEasyId("crouch_distance"), 50, 100)
            .difficulty(0)
            .build()
        );
        // TODO: fill all slots of campfire
        goalAdder.accept(BingoGoal.builder(veryEasyId("dye_sign"))
            .criterion("dye", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(BlockTags.ALL_SIGNS).build()),
                ItemPredicate.Builder.item().of(BingoItemTags.DYES)
            ))
            .name(Component.translatable("bingo.goal.dye_sign"))
            .tags(BingoTags.ACTION)
            .difficulty(0)
            .icon(Items.OAK_SIGN)
            .build()
        );
        goalAdder.accept(BingoGoal.builder(veryEasyId("extinguish_campfire"))
            .criterion("extinguish", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(Blocks.CAMPFIRE).build()),
                ItemPredicate.Builder.item().of(ItemTags.SHOVELS)
            ))
            .name(Component.translatable("bingo.goal.extinguish_campfire"))
            .tags(BingoTags.ACTION)
            .difficulty(0)
            .icon(Items.CAMPFIRE)
            .build()
        );
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
        goalAdder.accept(BingoGoal.builder(veryEasyId("break_hoe"))
            .criterion("break", ItemBrokenTrigger.TriggerInstance.itemBroken(ItemPredicate.Builder.item().of(ItemTags.HOES)))
            .tags(BingoTags.ACTION, BingoTags.STAT)
            .name(Component.translatable("bingo.goal.break_hoe"))
            .icon(Items.STONE_HOE)
            .difficulty(0)
            .build()
        );
        goalAdder.accept(BingoGoal.builder(veryEasyId("bounce_on_bed"))
            .criterion("bounce", BingoTriggers.bounceOnBed())
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.bounce_on_bed"))
            .icon(Blocks.WHITE_BED)
            .difficulty(0)
            .build()
        );
        goalAdder.accept(BingoGoal.builder(veryEasyId("fill_composter"))
            .criterion("fill", new ItemUsedOnLocationTrigger.TriggerInstance(
                CriteriaTriggers.ITEM_USED_ON_BLOCK.getId(),
                ContextAwarePredicate.ANY,
                ContextAwarePredicate.create(LocationCheck.checkLocation(
                    LocationPredicate.Builder.location().setBlock(
                        BlockPredicate.Builder.block().of(Blocks.COMPOSTER).setProperties(
                            StatePropertiesPredicate.Builder.properties().hasProperty(ComposterBlock.LEVEL, 7).build()
                        ).build()
                    )
                ).build())
            ))
            .name(Component.translatable("bingo.goal.fill_composter"))
            .tooltip(Component.translatable("bingo.goal.fill_composter.tooltip"))
            .tags(BingoTags.ACTION)
            .icon(Blocks.COMPOSTER)
            .difficulty(0)
            .build()
        );

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
        goalAdder.accept(BingoGoal.builder(easyId("grow_tree_in_nether"))
            .criterion("grow", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                LocationPredicate.Builder.location()
                    .setBlock(BlockPredicate.Builder.block().of(BlockTags.OVERWORLD_NATURAL_LOGS).build())
                    .setDimension(Level.NETHER),
                ItemPredicate.Builder.item().of(Items.BONE_MEAL)
            ))
            .tags(BingoTags.ACTION, BingoTags.NETHER, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.grow_tree_in_nether"))
            .tooltip(Component.translatable("bingo.goal.grow_tree_in_nether.tooltip"))
            .icon(Items.BONE_MEAL)
            .difficulty(1)
            .build()
        );
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
        goalAdder.accept(BingoGoal.builder(easyId("create_snow_golem"))
            .criterion("summon", SummonedEntityTrigger.TriggerInstance.summonedEntity(
                EntityPredicate.Builder.entity().of(EntityType.SNOW_GOLEM)
            ))
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.create_snow_golem"))
            .icon(Blocks.CARVED_PUMPKIN)
            .difficulty(1)
            .build()
        );
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
        goalAdder.accept(BingoGoal.builder(easyId("full_iron_armor"))
            .criterion("armor", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS
            ))
            .tags(BingoTags.ITEM)
            .infrequency(3)
            .name(Component.translatable("bingo.goal.full_iron_armor"))
            .icon(Items.IRON_HELMET)
            .difficulty(1)
            .build()
        );
        goalAdder.accept(BingoGoal.builder(easyId("full_leather_armor"))
            .criterion("armor", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS
            ))
            .tags(BingoTags.ITEM)
            .infrequency(3)
            .name(Component.translatable("bingo.goal.full_leather_armor"))
            .icon(Items.LEATHER_HELMET)
            .difficulty(1)
            .build()
        );
        // TODO: fill cauldron with water
        goalAdder.accept(BingoGoal.builder(easyId("complete_map"))
            .criterion("complete", CompleteMapTrigger.TriggerInstance.completeMap())
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.complete_map"))
            .icon(Items.FILLED_MAP)
            .antisynergy("complete_map")
            .difficulty(1)
            .build()
        );
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
        goalAdder.accept(BingoGoal.builder(easyId("grow_huge_mushroom"))
            .criterion("grow", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                LocationPredicate.Builder.location()
                    .setBlock(BlockPredicate.Builder.block().of(Blocks.MUSHROOM_STEM).build()),
                ItemPredicate.Builder.item().of(Items.BONE_MEAL)
            ))
            .tags(BingoTags.ACTION)
            .name(Component.translatable("bingo.goal.grow_huge_mushroom"))
            .icon(Blocks.RED_MUSHROOM_BLOCK)
            .difficulty(1)
            .build()
        );
        goalAdder.accept(BingoGoal.builder(easyId("water_lava_milk"))
            .criterion("buckets", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.WATER_BUCKET, Items.LAVA_BUCKET, Items.MILK_BUCKET
            ))
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD)
            .name(Component.translatable(
                "bingo.three",
                Items.WATER_BUCKET.getDescription(),
                Items.LAVA_BUCKET.getDescription(),
                Items.MILK_BUCKET.getDescription()
            ))
            .icon(Items.LAVA_BUCKET)
            .antisynergy("bucket_types", "water_bucket", "lava_bucket", "milk_bucket")
            .reactant("use_buckets")
            .difficulty(1)
            .build()
        );
        // TODO: different flowers
        // TODO: colors of concrete
        // TODO: colors of glazed terracotta
        goalAdder.accept(bedRowGoal(easyId("bed_row"), 3, 6)
            .difficulty(1)
            .build());
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
        goalAdder.accept(crouchDistanceGoal(easyId("crouch_distance"), 100, 200)
            .difficulty(1)
            .build()
        );
        // TODO: never use debug
        // TODO: ring bell from 10 blocks away
        // TODO: repair item with grindstone
        goalAdder.accept(obtainItemGoal(easyId("sweet_berries"), Items.SWEET_BERRIES, 2, 6)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME)
            .difficulty(1)
            .build());
        // TODO: banner pattern
        // TODO: drink sussy stew
        goalAdder.accept(BingoGoal.builder(easyId("drink_sus_stew"))
            .criterion("drink", ConsumeItemTrigger.TriggerInstance.usedItem(Items.SUSPICIOUS_STEW))
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD)
            .reactant("eat_non_meat")
            .name(Component.translatable("bingo.goal.drink_sus_stew", Items.SUSPICIOUS_STEW.getDescription()))
            .icon(Items.SUSPICIOUS_STEW)
            .difficulty(1)
            .build()
        );
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
        goalAdder.accept(BingoGoal.builder(easyId("tame_horse"))
            .criterion("obtain", TameAnimalTrigger.TriggerInstance.tamedAnimal(EntityPredicate.Builder.entity().of(EntityType.HORSE).build()))
            .name(Component.translatable("bingo.goal.tame_horse"))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .icon(Items.HORSE_SPAWN_EGG)
            .difficulty(1)
            .build());
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
        goalAdder.accept(BingoGoal.builder(mediumId("skeleton_bow"))
            .criterion("pickup", ItemPickedUpTrigger.TriggerInstance.pickedUpFrom(
                ItemPredicate.Builder.item().of(Items.BOW).build(),
                EntityPredicate.Builder.entity().of(EntityType.SKELETON).build()
            ))
            .reactant("pacifist")
            .tags(BingoTags.ITEM, BingoTags.COMBAT)
            .name(Component.translatable("bingo.goal.skeleton_bow"))
            .icon(Items.BOW)
            .difficulty(2)
            .build()
        );
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
        goalAdder.accept(bedRowGoal(mediumId("bed_row"), 7, 10)
            .difficulty(2)
            .build());
        // TODO: power redstone lamp
        // TODO: different flowers
        // TODO: put zombified piglin in water
        goalAdder.accept(mineralPillarGoal(mediumId("basic_mineral_blocks"), BingoBlockTags.BASIC_MINERAL_BLOCKS)
            .name(Component.translatable("bingo.goal.basic_mineral_blocks"))
            .difficulty(2)
            .tags(BingoTags.OVERWORLD)
            .icon(Blocks.DIAMOND_BLOCK)
            .build()
        );
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
        goalAdder.accept(BingoGoal.builder(mediumId("tame_cat"))
            .criterion("obtain", TameAnimalTrigger.TriggerInstance.tamedAnimal(EntityPredicate.Builder.entity().of(EntityType.CAT).build()))
            .name(Component.translatable("bingo.goal.tame_cat"))
            .tags(BingoTags.ACTION, BingoTags.VILLAGE, BingoTags.OVERWORLD)
            .icon(Items.CAT_SPAWN_EGG)
            .difficulty(2)
            .build());
        // TODO: breed mobs
        goalAdder.accept(crouchDistanceGoal(mediumId("crouch_distance"), 200, 400)
            .difficulty(2)
            .build()
        );
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
        goalAdder.accept(BingoGoal.builder(mediumId("pillager_crossbow"))
            .criterion("pickup", ItemPickedUpTrigger.TriggerInstance.pickedUpFrom(
                ItemPredicate.Builder.item().of(Items.CROSSBOW).build(),
                EntityPredicate.Builder.entity().of(EntityType.PILLAGER).build()
            ))
            .reactant("pacifist")
            .tags(BingoTags.ITEM, BingoTags.COMBAT, BingoTags.OVERWORLD, BingoTags.RARE_BIOME)
            .name(Component.translatable("bingo.goal.pillager_crossbow"))
            .icon(Items.CROSSBOW)
            .difficulty(2)
            .build()
        );
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
            .setAntisynergy("turtle_helmet")
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

    private static void addHardGoals(Consumer<BingoGoal> goalAdder) {
        goalAdder.accept(BingoGoal.builder(hardId("level_10_enchant"))
            .criterion("enchant", EnchantedItemTrigger.builder().requiredLevels(MinMaxBounds.Ints.atLeast(10)).build())
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.level_10_enchant"))
            .icon(new ItemStack(Items.ENCHANTING_TABLE, 10))
            .difficulty(3)
            .build());
        goalAdder.accept(BingoGoal.builder(hardId("milk_mooshroom"))
            .criterion("obtain", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
                ItemPredicate.Builder.item().of(Items.BUCKET),
                EntityPredicate.wrap(EntityPredicate.Builder.entity().of(EntityType.MOOSHROOM).build())))
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.milk_mooshroom"))
            .icon(Items.MOOSHROOM_SPAWN_EGG)
            .infrequency(2)
            .difficulty(3)
            .build());
        goalAdder.accept(BingoGoal.builder(hardId("shear_mooshroom"))
            .criterion("obtain", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
                ItemPredicate.Builder.item().of(Items.SHEARS),
                EntityPredicate.wrap(EntityPredicate.Builder.entity().of(EntityType.MOOSHROOM).build())))
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.shear_mooshroom"))
            .icon(Items.COW_SPAWN_EGG)
            .infrequency(2)
            .difficulty(3)
            .build());
        goalAdder.accept(obtainItemGoal(hardId("sea_lantern"), Items.SEA_LANTERN, 2, 5)
            .tags(BingoTags.OVERWORLD, BingoTags.OCEAN)
            .difficulty(3)
            .build());
        goalAdder.accept(obtainItemGoal(hardId("sponge"), Items.SPONGE)
            .tooltip(Component.translatable("bingo.goal.sponge.tooltip"))
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD)
            .difficulty(3)
            .build());
        goalAdder.accept(BingoGoal.builder(hardId("listen_to_music"))
            .criterion("obtain", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(Blocks.JUKEBOX).build()),
                ItemPredicate.Builder.item().of(ItemTags.MUSIC_DISCS)))
            .tags(BingoTags.ITEM)
            .name(Component.translatable("bingo.goal.listen_to_music"))
            .icon(Items.JUKEBOX)
            .difficulty(3)
            .build());
        // TODO: different flowers
        goalAdder.accept(obtainItemGoal(hardId("diamond_block"), Items.DIAMOND_BLOCK, 2, 4)
            .infrequency(2)
            .difficulty(3)
            .build());
        goalAdder.accept(BingoGoal.builder(hardId("zombified_piglin_sword"))
            .criterion("pickup", ItemPickedUpTrigger.TriggerInstance.pickedUpFrom(
                ItemPredicate.Builder.item().of(Items.GOLDEN_SWORD).build(),
                EntityPredicate.Builder.entity().of(EntityType.ZOMBIFIED_PIGLIN).build()
            ))
            .reactant("pacifist")
            .tags(BingoTags.ITEM, BingoTags.COMBAT, BingoTags.NETHER)
            .name(Component.translatable("bingo.goal.zombified_piglin_sword"))
            .icon(Items.GOLDEN_SWORD)
            .difficulty(3)
            .build()
        );
        // TODO: finish by launching foreworks of n different colors
        goalAdder.accept(BingoGoal.builder(hardId("nametag_enderman"))
            .criterion("nametag", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
                ItemPredicate.Builder.item().of(Items.NAME_TAG),
                EntityPredicate.wrap(EntityPredicate.Builder.entity().of(EntityType.ENDERMAN).build())))
            .tags(BingoTags.ACTION, BingoTags.COMBAT, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.nametag_enderman"))
            .icon(Items.NAME_TAG)
            .difficulty(3)
            .build());
        // TODO: finish on top of blaze spawner
        // TODO: colors of wool
        // TODO: colors of terracotta
        // TODO: colors of glazed terracotta
        // TODO: colors of concrete
        goalAdder.accept(bedRowGoal(hardId("bed_row"), 11, 14)
            .difficulty(3)
            .build());
        goalAdder.accept(BingoGoal.builder(hardId("poison_parrot"))
            .criterion("poison", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
                ItemPredicate.Builder.item().of(Items.COOKIE),
                EntityPredicate.wrap(EntityPredicate.Builder.entity().of(EntityType.PARROT).build())))
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.poison_parrot"))
            .icon(Items.COOKIE)
            .infrequency(2)
            .reactant("pacifist")
            .difficulty(3)
            .build());
        goalAdder.accept(BingoGoal.builder(hardId("tame_parrot"))
            .criterion("tame", TameAnimalTrigger.TriggerInstance.tamedAnimal(EntityPredicate.Builder.entity().of(EntityType.PARROT).build()))
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.tame_parrot"))
            .icon(Items.PARROT_SPAWN_EGG)
            .infrequency(2)
            .difficulty(3)
            .build());
        // TODO: ice block on top of magma block
        goalAdder.accept(obtainLevelsGoal(hardId("levels"), 27, 37)
            .difficulty(3)
            .build());
        // TODO: build an ice cube
        // TODO: finish on top of stairway to heaven
        // TODO: get ghast into overworld
        goalAdder.accept(obtainItemGoal(hardId("enchanted_golden_apple"), Items.ENCHANTED_GOLDEN_APPLE)
            .difficulty(3)
            .build());

        goalAdder.accept(BingoGoal.builder(hardId("never_wear_armor_or_use_shields"))
            .criterion("equip", EquipItemTrigger.builder().newItem(ItemPredicate.Builder.item().of(BingoItemTags.ARMOR).build()).build())
            .criterion("use", new UsingItemTrigger.TriggerInstance(ContextAwarePredicate.ANY, ItemPredicate.Builder.item().of(BingoItemTags.SHIELDS).build()))
            .requirements(List.of("equip", "use"))
            .tags(BingoTags.NEVER)
            .name(Component.translatable("bingo.goal.never_wear_armor_or_use_shields"))
            .tooltip(Component.translatable("bingo.goal.never_wear_armor_or_use_shields.tooltip"))
            .icon(makeItemWithGlint(Items.SHIELD))
            .antisynergy("never_wear_armor")
            .catalyst("wear_armor")
            .difficulty(3)
            .build());
        // TODO: kill mob that is wearing full armor
        // TODO: enchant 5 items
        // TODO: never use buckets
        goalAdder.accept(obtainItemGoal(hardId("conduit"), Items.CONDUIT)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD)
            .difficulty(3)
            .build());
        // TODO: 2-5 types of dead coral blocks
        goalAdder.accept(obtainItemGoal(hardId("sea_pickle"), Items.SEA_PICKLE, 16, 32)
            .tags(BingoTags.OCEAN, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .difficulty(3)
            .build());
        // TODO: didn't want to live in the same world as death message
        goalAdder.accept(obtainItemGoal(hardId("cookie"), Items.COOKIE)
            .tags(BingoTags.OVERWORLD)
            .difficulty(3)
            .build());
        // TODO: grow full jungle tree
        goalAdder.accept(obtainItemGoal(hardId("prismarine_shard"), Items.PRISMARINE_SHARD, 2, 10)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD)
            .difficulty(3)
            .build());
        goalAdder.accept(obtainItemGoal(hardId("jungle_log"), Items.JUNGLE_LOG, 16, 32)
            .infrequency(4)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .difficulty(3)
            .build());
        goalAdder.accept(obtainItemGoal(hardId("jungle_wood"), Items.JUNGLE_WOOD, 11, 20)
            .infrequency(4)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .difficulty(3)
            .build());
        goalAdder.accept(obtainItemGoal(hardId("stripped_jungle_wood"), Items.STRIPPED_JUNGLE_WOOD, 11, 20)
            .reactant("axe_use")
            .infrequency(4)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .difficulty(3)
            .build());
        goalAdder.accept(obtainItemGoal(hardId("stripped_jungle_log"), Items.STRIPPED_JUNGLE_LOG, 11, 20)
            .reactant("axe_use")
            .infrequency(4)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .difficulty(3)
            .build());
        // TODO: different diamond items
        // TODO: destroy mob spawner
        goalAdder.accept(obtainItemGoal(hardId("popped_chorus_fruit"), Items.POPPED_CHORUS_FRUIT, 32, 64)
            .tags(BingoTags.END)
            .difficulty(3)
            .build());
        // TODO: get villager into the end
        goalAdder.accept(obtainItemGoal(hardId("dragon_breath"), Items.DRAGON_BREATH, 5, 16)
            .tags(BingoTags.COMBAT, BingoTags.END)
            .difficulty(3)
            .build());
        goalAdder.accept(obtainItemGoal(hardId("dragon_egg"), Items.DRAGON_EGG)
            .tags(BingoTags.COMBAT, BingoTags.END)
            .difficulty(3)
            .build());
        goalAdder.accept(BingoGoal.builder(hardId("complete_full_size_map"))
            .criterion("complete", CompleteMapTrigger.TriggerInstance.completeMap(
                MinMaxBounds.Ints.atLeast(MapItemSavedData.MAX_SCALE)
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.complete_full_size_map"))
            .icon(Items.FILLED_MAP)
            .antisynergy("complete_map")
            .difficulty(3)
            .build()
        );
        // TODO: be killed by a villager
        // TODO: pop a totem
        // TODO: every type of sword
        // TODO: every type of pickaxe
        goalAdder.accept(BingoGoal.builder(hardId("pacifist"))
            .criterion("kill", KilledTrigger.TriggerInstance.playerKilledEntity())
            .tags(BingoTags.NEVER, BingoTags.STAT)
            .name(Component.translatable("bingo.goal.pacifist"))
            .tooltip(Component.translatable("bingo.goal.pacifist.tooltip"))
            .icon(Items.DIAMOND_SWORD)
            .catalyst("pacifist")
            .difficulty(3)
            .build());
        // TODO: finish by scaffolding tower then removing it
        // TODO: feed panda cake
        // TODO: breed pandas
        // TODO: disarm pillager
        // TODO: stun ravager
        // TODO: hero of the village
        // TODO: gail ocelot's trust
        goalAdder.accept(obtainItemGoal(hardId("ender_eye"), Items.ENDER_EYE, 12, 12)
            .reactant("pacifist")
            .tags(BingoTags.NETHER, BingoTags.COMBAT)
            .tooltip(Component.translatable("bingo.goal.ender_eye.hard.tooltip"))
            .difficulty(3)
            .build());
        goalAdder.accept(obtainItemGoal(hardId("netherite_ingot"), Items.NETHERITE_INGOT)
            .tags(BingoTags.NETHER)
            .difficulty(3)
            .build());
        goalAdder.accept(obtainItemGoal(hardId("wither_skeleton_skull"), Items.WITHER_SKELETON_SKULL)
            .reactant("pacifist")
            .tags(BingoTags.NETHER, BingoTags.COMBAT, BingoTags.RARE_BIOME)
            .difficulty(3)
            .build());
        goalAdder.accept(obtainItemGoal(hardId("gilded_blackstone"), Items.GILDED_BLACKSTONE)
            .tags(BingoTags.NETHER, BingoTags.RARE_BIOME)
            .difficulty(3)
            .build());
        // TODO: make compass point to lodestone
        // TODO: give piglin brute enchanted axe
        goalAdder.accept(BingoGoal.builder(hardId("6x6scaffolding"))
            .criterion("obtain", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(
                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.SCAFFOLDING),
                    BlockPatternCondition.builder().aisle(
                        "########",
                        "#      #",
                        "#      #",
                        "#      #",
                        "#      #",
                        "#      #",
                        "#      #"
                    ).where('#', BlockPredicate.Builder.block().of(Blocks.SCAFFOLDING).build())))
            .tags(BingoTags.OVERWORLD, BingoTags.BUILD)
            .name(Component.translatable("bingo.goal.6x6scaffolding"))
            .tooltip(Component.translatable("bingo.goal.6x6scaffolding.tooltip"))
            .tooltipIcon(new ResourceLocation("bingo:textures/gui/tooltips/6x6scaffolding.png"))
            .icon(Items.SCAFFOLDING)
            .difficulty(3)
            .build());
        goalAdder.accept(obtainItemGoal(hardId("honey_block"), Items.HONEY_BLOCK, 2, 5)
            .setAntisynergy("honey")
            .infrequency(2)
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .difficulty(3)
            .build());
        goalAdder.accept(obtainItemGoal(hardId("honeycomb_block"), Items.HONEYCOMB_BLOCK, 6, 15)
            .setAntisynergy("honeycomb")
            .infrequency(2)
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .difficulty(3)
            .build());
        // TODO: kill a wandering trader
        // TODO: cure a zombie villager
        // TODO: throw mending book into lava
        // TODO: never smelt with furnaces
        // TODO: throw huge nether fungus in overworld
        // TODO: 32-64 dirt, netherrack and end stone
        // TODO: tame a mule
        // TODO: convert carrot on a stick to fishing rod
        // TODO: skull charge banner pattern
        goalAdder.accept(obtainItemGoal(hardId("turtle_helmet"), Items.TURTLE_HELMET)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD)
            .difficulty(3)
            .build());
    }

    private static ResourceLocation hardId(String id) {
        return new ResourceLocation(Bingo.MOD_ID, "hard/" + id);
    }

    private static void addVeryHardGoals(Consumer<BingoGoal> goalAdder) {
        goalAdder.accept(obtainSomeItemsFromTag(veryHardId("ores"), Items.DIAMOND_ORE, BingoItemTags.ORES, "bingo.goal.ores", 5, 7)
            .tooltip(Component.translatable("bingo.goal.ores.tooltip"))
            .tags(BingoTags.OVERWORLD)
            .difficulty(4)
            .build());
        // TODO: different potions
        goalAdder.accept(BingoGoal.builder(veryHardId("all_chestplates"))
            .criterion("obtain", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.LEATHER_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE,
                Items.IRON_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE))
            .tags(BingoTags.ITEM, BingoTags.COMBAT)
            .name(Component.translatable("bingo.goal.all_chestplates"))
            .tooltip(Component.translatable("bingo.goal.all_chestplates.tooltip"))
            .icon(Items.NETHERITE_CHESTPLATE)
            .difficulty(4)
            .build());
        goalAdder.accept(obtainItemGoal(
                veryHardId("any_head"),
                new ItemStack(Items.ZOMBIE_HEAD),
                ItemPredicate.Builder.item().of(
                    Items.SKELETON_SKULL,
                    Items.PLAYER_HEAD,
                    Items.ZOMBIE_HEAD,
                    Items.CREEPER_HEAD,
                    Items.DRAGON_HEAD,
                    Items.PIGLIN_HEAD
                ))
            .tags(BingoTags.COMBAT, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.any_head"))
            .tooltip(Component.translatable("bingo.goal.any_head.tooltip"))
            .difficulty(4)
            .build());

        goalAdder.accept(BingoGoal.builder(veryHardId("all_dyes"))
            .criterion("obtain", InventoryChangeTrigger.TriggerInstance.hasItems(
                Arrays.stream(DyeColor.values()).map(DyeItem::byColor).toArray(ItemLike[]::new)))
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.all_dyes"))
            .tooltip(Component.translatable("bingo.sixteen_bang",
                Arrays.stream(DyeColor.values()).map(color -> Component.translatable("color.minecraft." + color.getName())).toArray(Object[]::new)))
            .icon(new ItemStack(Items.RED_DYE, 16))
            .antisynergy("every_color")
            .reactant("use_furnace")
            .difficulty(4)
            .build());
        goalAdder.accept(BingoGoal.builder(veryHardId("levels"))
            .criterion("obtain", ExperienceChangeTrigger.builder().levels(MinMaxBounds.Ints.atLeast(50)).build())
            .tags(BingoTags.STAT)
            .name(Component.translatable("bingo.goal.levels", 50))
            .icon(new ItemStack(Items.EXPERIENCE_BOTTLE, 50))
            .infrequency(2)
            .antisynergy("levels")
            .difficulty(4)
            .build());
        goalAdder.accept(obtainItemGoal(veryHardId("tipped_arrow"), Items.TIPPED_ARROW, 16, 32)
            .tags(BingoTags.NETHER, BingoTags.OVERWORLD)
            .icon(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), Potions.MUNDANE), subber -> subber.sub("count", "count"))
            .difficulty(4)
            .build());
        goalAdder.accept(mineralPillarGoal(veryHardId("all_mineral_blocks"), BingoBlockTags.ALL_MINERAL_BLOCKS)
            .name(Component.translatable("bingo.goal.all_mineral_blocks"))
            .tooltip(Component.translatable("bingo.goal.all_mineral_blocks.tooltip"))
            .difficulty(4)
            .tags(BingoTags.OVERWORLD, BingoTags.NETHER)
            .icon(Blocks.NETHERITE_BLOCK)
            .build()
        );
        goalAdder.accept(BingoGoal.builder(veryHardId("sleep_in_mansion"))
            .criterion("sleep", new PlayerTrigger.TriggerInstance(
                CriteriaTriggers.SLEPT_IN_BED.getId(),
                EntityPredicate.wrap(EntityPredicate.Builder.entity().located(LocationPredicate.inStructure(BuiltinStructures.WOODLAND_MANSION)).build())))
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.sleep_in_mansion"))
            .icon(Items.BROWN_BED)
            .difficulty(4)
            .build());
        goalAdder.accept(obtainItemGoal(veryHardId("mycelium"), Items.MYCELIUM, 10, 32)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .difficulty(4)
            .build());
        goalAdder.accept(BingoGoal.builder(veryHardId("coral_blocks"))
            .criterion("obtain", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.TUBE_CORAL_BLOCK, Items.BRAIN_CORAL_BLOCK, Items.BUBBLE_CORAL_BLOCK,
                Items.FIRE_CORAL_BLOCK, Items.HORN_CORAL_BLOCK
            ))
            .tags(BingoTags.ITEM, BingoTags.RARE_BIOME, BingoTags.OCEAN, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.coral_blocks"))
            .icon(Blocks.BRAIN_CORAL_BLOCK)
            .difficulty(4)
            .build()
        );
        goalAdder.accept(obtainItemGoal(veryHardId("blue_ice"), Items.BLUE_ICE, 32, 64)
            .tags(BingoTags.OVERWORLD)
            .difficulty(4)
            .build());
        // TODO: fully power conduit
        goalAdder.accept(BingoGoal.builder(veryHardId("all_diamond_craftables"))
            .criterion("obtain", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.DIAMOND_BLOCK, Items.DIAMOND_AXE, Items.DIAMOND_BOOTS,
                Items.DIAMOND_CHESTPLATE, Items.DIAMOND_HELMET, Items.DIAMOND_HOE,
                Items.DIAMOND_LEGGINGS, Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL,
                Items.DIAMOND_SWORD, Items.ENCHANTING_TABLE, Items.FIREWORK_STAR, Items.JUKEBOX))
            .name(Component.translatable("bingo.goal.all_diamond_craftables"))
            .tooltip(Component.translatable("bingo.goal.all_diamond_craftables.tooltip"))
            .icon(Items.DIAMOND_HOE)
            .antisynergy("diamond_items")
            .difficulty(4)
            .build());
        goalAdder.accept(BingoGoal.builder(veryHardId("shulker_in_overworld"))
            .criterion("kill", KilledTrigger.TriggerInstance.playerKilledEntity(
                EntityPredicate.Builder.entity().of(EntityType.SHULKER).located(LocationPredicate.inDimension(Level.OVERWORLD))))
            .tags(BingoTags.ACTION, BingoTags.COMBAT, BingoTags.END, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.shulker_in_overworld"))
            .icon(Items.SHULKER_SHELL)
            .difficulty(4)
            .build());
        goalAdder.accept(obtainItemGoal(veryHardId("diamond_block"), Items.DIAMOND_BLOCK, 5, 10)
            .infrequency(2)
            .difficulty(4)
            .build());
        goalAdder.accept(BingoGoal.builder(veryHardId("complete_full_size_end_map"))
            .criterion("complete", CompleteMapTrigger.TriggerInstance.completeMap(
                MinMaxBounds.Ints.atLeast(MapItemSavedData.MAX_SCALE),
                LocationPredicate.inDimension(Level.END)
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD, BingoTags.END)
            .name(Component.translatable("bingo.goal.complete_full_size_end_map"))
            .icon(Items.FILLED_MAP)
            .antisynergy("complete_map")
            .difficulty(4)
            .build()
        );
        goalAdder.accept(obtainItemGoal(veryHardId("wither_rose"), Items.WITHER_ROSE, 32, 64)
            .reactant("pacifist")
            .tags(BingoTags.NETHER, BingoTags.COMBAT)
            .difficulty(4)
            .build());
        goalAdder.accept(BingoGoal.builder(veryHardId("panda_slime_ball"))
            // Currently untested. They have a 1/175,000 or a 1/2,100,000 chance to drop one on a tick.
            .criterion("pickup", ItemPickedUpTrigger.TriggerInstance.pickedUpFrom(
                ItemPredicate.Builder.item().of(Items.SLIME_BALL).build(),
                EntityPredicate.Builder.entity().of(EntityType.PANDA).build()
            ))
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD, BingoTags.RARE_BIOME)
            .name(Component.translatable("bingo.goal.panda_slime_ball"))
            .icon(Items.SLIME_BALL)
            .difficulty(4)
            .build()
        );
        goalAdder.accept(obtainItemGoal(veryHardId("netherite_block"), Items.NETHERITE_BLOCK, 2, 2)
            .tags(BingoTags.NETHER)
            .difficulty(4)
            .build());
        // TODO: full netherite armor and tools
        // TODO: convert pig into zombified piglin
        goalAdder.accept(obtainItemGoal(veryHardId("trident"), Items.TRIDENT)
            .tags(BingoTags.OCEAN, BingoTags.COMBAT, BingoTags.OVERWORLD)
            .difficulty(4)
            .build());
        goalAdder.accept(BingoGoal.builder(veryHardId("tame_skeleton_horse"))
            .criterion("obtain", TameAnimalTrigger.TriggerInstance.tamedAnimal(EntityPredicate.Builder.entity().of(EntityType.SKELETON_HORSE).build()))
            .name(Component.translatable("bingo.goal.tame_skeleton_horse"))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .icon(Items.SKELETON_HORSE_SPAWN_EGG)
            .difficulty(4)
            .build());
        // TODO: all parrots dance
        goalAdder.accept(bedRowGoal(veryHardId("bed_row"), 16, 16)
            .reactant("use_furnace")
            .antisynergy("every_color")
            .infrequency(2)
            .tags(BingoTags.ACTION)
            .tooltip(Component.translatable("bingo.sixteen_bang",
                Arrays.stream(DyeColor.values()).map(color -> Component.translatable("color.minecraft." + color.getName())).toArray(Object[]::new)))
            .difficulty(4)
            .build());
        // TODO: kill enderman with only endermites
        goalAdder.accept(BingoGoal.builder(veryHardId("beacon_regen"))
            .criterion("effect", BeaconEffectTrigger.TriggerInstance.effectApplied(MobEffects.REGENERATION))
            .tags(BingoTags.ITEM, BingoTags.NETHER, BingoTags.OVERWORLD, BingoTags.COMBAT)
            .name(Component.translatable("bingo.goal.beacon_regen"))
            .icon(Blocks.BEACON)
            .reactant("pacifist")
            .difficulty(4)
            .build()
        );
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
            builder.criterion("obtain", TotalCountInventoryChangeTrigger.builder().items(items[0].build()).build());
        } else {
            List<String> requirements = new ArrayList<>(items.length);
            for (int i = 0; i < items.length; i++) {
                builder.criterion("obtain_" + i, TotalCountInventoryChangeTrigger.builder().items(items[i].build()).build());
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
                .criterion("obtain", TotalCountInventoryChangeTrigger.builder().items(item.withCount(MinMaxBounds.Ints.exactly(minCount)).build()).build())
                .tags(BingoTags.ITEM)
                .icon(new ItemStack(icon, minCount));
        }
        return BingoGoal.builder(id)
            .sub("count", BingoSub.random(minCount, maxCount))
            .criterion("obtain",
                TotalCountInventoryChangeTrigger.builder().items(item.withCount(MinMaxBounds.Ints.atLeast(0)).build()).build(),
                subber -> subber.sub("conditions.items.0.count.min", "count"))
            .tags(BingoTags.ITEM)
            .icon(icon, subber -> subber.sub("count", "count"));
    }

    private static BingoGoal.Builder obtainSomeItemsFromTag(ResourceLocation id, ItemLike icon, TagKey<Item> tag, String translationKey, int minCount, int maxCount) {
        return obtainSomeItemsFromTag(id, new ItemStack(icon), tag, translationKey, minCount, maxCount);
    }

    private static BingoGoal.Builder obtainSomeItemsFromTag(ResourceLocation id, ItemStack icon, TagKey<Item> tag, String translationKey, int minCount, int maxCount) {
        return BingoGoal.builder(id)
            .sub("count", BingoSub.random(minCount, maxCount))
            .criterion(
                "obtain",
                HasSomeItemsFromTagTrigger.builder().tag(tag).requiredCount(0).build(),
                subber -> subber.sub("conditions.required_count", "count")
            )
            .tags(BingoTags.ITEM)
            .name(Component.translatable(translationKey, 0), subber -> subber.sub("with.0", "count"))
            .icon(icon, subber -> subber.sub("count", "count"));
    }

    private static BingoGoal.Builder obtainLevelsGoal(ResourceLocation id, int minLevels, int maxLevels) {
        return BingoGoal.builder(id)
            .sub("count", BingoSub.random(minLevels, maxLevels))
            .criterion("obtain", ExperienceChangeTrigger.builder().levels(MinMaxBounds.Ints.atLeast(0)).build(),
                subber -> subber.sub("conditions.levels.min", "count"))
            .tags(BingoTags.STAT)
            .name(Component.translatable("bingo.goal.levels", 0), subber -> subber.sub("with.0", "count"))
            .icon(Items.EXPERIENCE_BOTTLE, subber -> subber.sub("count", "count"))
            .infrequency(2)
            .antisynergy("levels");
    }

    private static BingoGoal.Builder crouchDistanceGoal(ResourceLocation id, int minDistance, int maxDistance) {
        return BingoGoal.builder(id)
            .sub("distance", BingoSub.random(minDistance, maxDistance))
            .criterion("crouch",
                BingoTriggers.statChanged(Stats.CUSTOM.get(Stats.CROUCH_ONE_CM), MinMaxBounds.Ints.atLeast(0)),
                subber -> subber.sub(
                    "conditions.player.0.predicate.type_specific.bingo:relative_stats.0.value.min",
                    new BingoSub.CompoundBingoSub(
                        BingoSub.CompoundBingoSub.Operator.MULTIPLY,
                        new BingoSub.SubBingoSub("distance"),
                        new BingoSub.IntBingoSub(ConstantInt.of(100))
                    )
                )
            )
            .name(Component.translatable("bingo.goal.crouch_distance", 0), subber -> subber.sub("with.0", "distance"))
            .antisynergy("crouch_distance")
            .infrequency(2)
            .tags(BingoTags.ACTION, BingoTags.STAT)
            .icon(Items.LEATHER_BOOTS, subber -> subber.sub("count", "distance"));
    }

    private static BingoGoal.Builder bedRowGoal(ResourceLocation id, int minCount, int maxCount) {
        if (minCount == maxCount) {
            return BingoGoal.builder(id)
                .criterion("obtain", BedRowTrigger.create(minCount))
                .name(Component.translatable("bingo.goal.bed_row", minCount))
                .antisynergy("bed_color")
                .infrequency(4)
                .icon(new ItemStack(Items.PURPLE_BED, minCount))
                .tags(BingoTags.BUILD, BingoTags.COLOR, BingoTags.OVERWORLD);
        }
        return BingoGoal.builder(id)
            .sub("count", BingoSub.random(minCount, maxCount))
            .criterion("obtain", BedRowTrigger.create(0), subber -> subber.sub("conditions.count", "count"))
            .name(Component.translatable("bingo.goal.bed_row", 0), subber -> subber.sub("with.0", "count"))
            .antisynergy("bed_color")
            .infrequency(4)
            .icon(Items.PURPLE_BED, subber -> subber.sub("count", "count"))
            .tags(BingoTags.BUILD, BingoTags.COLOR, BingoTags.OVERWORLD);
    }

    private static BingoGoal.Builder mineralPillarGoal(ResourceLocation id, TagKey<Block> tag) {
        return BingoGoal.builder(id)
            .criterion("pillar", MineralPillarTrigger.TriggerInstance.pillar(tag))
            .tags(BingoTags.BUILD);
    }

    private static ItemStack makeItemWithGlint(ItemLike item) {
        ItemStack result = new ItemStack(item);
        ListTag enchantments = new ListTag();
        enchantments.add(new CompoundTag());
        result.getOrCreateTag().put("Enchantments", enchantments);
        return result;
    }
}
