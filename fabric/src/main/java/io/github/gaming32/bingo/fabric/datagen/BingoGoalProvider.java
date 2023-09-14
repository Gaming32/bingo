package io.github.gaming32.bingo.fabric.datagen;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.conditions.BlockPatternCondition;
import io.github.gaming32.bingo.conditions.EndermanHasOnlyBeenDamagedByEndermiteCondition;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoSub;
import io.github.gaming32.bingo.data.BingoTags;
import io.github.gaming32.bingo.data.tags.BingoBlockTags;
import io.github.gaming32.bingo.data.tags.BingoItemTags;
import io.github.gaming32.bingo.triggers.EnchantedItemTrigger;
import io.github.gaming32.bingo.triggers.*;
import io.github.gaming32.bingo.util.BlockPattern;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.tags.*;
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
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.*;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class BingoGoalProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;
    private int difficulty;
    private String prefix;

    public BingoGoalProvider(FabricDataOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "bingo/goals");
    }

    @Override
    @NotNull
    public CompletableFuture<?> run(CachedOutput output) {
        Set<ResourceLocation> existingGoals = new HashSet<>();
        List<CompletableFuture<?>> generators = new ArrayList<>();

        Consumer<BingoGoal.Builder> goalAdder = goalBuilder -> {
            BingoGoal goal = goalBuilder.difficulty(difficulty).build();
            if (!goal.getId().getPath().startsWith(prefix)) {
                throw new IllegalArgumentException("Goal ID path does not start with " + prefix);
            }
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

    private void addGoals(Consumer<BingoGoal.Builder> goalAdder) {
        difficulty = 0;
        prefix = "very_easy/";
        addVeryEasyGoals(goalAdder);

        difficulty = 1;
        prefix = "easy/";
        addEasyGoals(goalAdder);

        difficulty = 2;
        prefix = "medium/";
        addMediumGoals(goalAdder);

        difficulty = 3;
        prefix = "hard/";
        addHardGoals(goalAdder);

        difficulty = 4;
        prefix = "very_hard/";
        addVeryHardGoals(goalAdder);
    }

    private void addVeryEasyGoals(Consumer<BingoGoal.Builder> goalAdder) {
        goalAdder.accept(obtainItemGoal(id("cobblestone"), Items.COBBLESTONE, 32, 64)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("dirt"), Items.DIRT, 32, 64)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("redstone"), Items.REDSTONE).tags(BingoTags.OVERWORLD)
            .infrequency(2));
        goalAdder.accept(obtainItemGoal(id("lava_bucket"), Items.LAVA_BUCKET)
            .reactant("use_buckets")
            .infrequency(4));
        goalAdder.accept(obtainItemGoal(id("milk_bucket"), Items.MILK_BUCKET)
            .reactant("use_buckets")
            .tags(BingoTags.OVERWORLD)
            .infrequency(4));
        goalAdder.accept(obtainItemGoal(id("water_bucket"), Items.WATER_BUCKET)
            .reactant("use_buckets")
            .tags(BingoTags.OVERWORLD)
            .infrequency(4));
        goalAdder.accept(obtainItemGoal(id("fish_bucket"), Items.TROPICAL_FISH_BUCKET, ItemPredicate.Builder.item().of(BingoItemTags.FISH_BUCKETS))
            .name(Component.translatable("bingo.goal.fish_bucket"))
            .tooltip(Component.translatable("bingo.goal.fish_bucket.tooltip", Component.translatable("advancements.husbandry.tactical_fishing.title")))
            .antisynergy("fish_bucket")
            .reactant("use_buckets")
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD)
            .infrequency(4));
        goalAdder.accept(obtainItemGoal(id("andesite"), Items.ANDESITE, 16, 32)
            .infrequency(3)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("granite"), Items.GRANITE, 16, 32)
            .infrequency(3)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("diorite"), Items.DIORITE, 16, 32)
            .infrequency(3)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("iron_block"), Items.IRON_BLOCK)
            .infrequency(2));
        goalAdder.accept(BingoGoal.builder(id("poppies_dandelions"))
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
            .icon(Items.POPPY));
        goalAdder.accept(obtainLevelsGoal(id("levels"), 5, 7));
        goalAdder.accept(obtainItemGoal(id("note_block"), Items.NOTE_BLOCK, 5, 7)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("leaves"), Items.OAK_LEAVES, ItemPredicate.Builder.item().of(ItemTags.LEAVES), 32, 64)
            .name(Component.translatable("bingo.goal.leaves"))
            .tags(BingoTags.OVERWORLD));
        // TODO: leaf cube
        // TODO: colors of wool
        goalAdder.accept(obtainItemGoal(id("snowball"), Items.SNOWBALL, 8, 16)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD));
        // TODO: slabs
        // TODO: stairs
        goalAdder.accept(obtainItemGoal(id("diamond"), Items.DIAMOND));
        goalAdder.accept(obtainItemGoal(id("rotten_flesh"), Items.ROTTEN_FLESH, 5, 15)
            .infrequency(2));
        goalAdder.accept(obtainItemGoal(id("stone"), Items.STONE, 10, 32)
            .tooltip(Component.translatable("bingo.goal.stone.tooltip"))
            .infrequency(2)
            .reactant("use_furnace")
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("bread"), Items.BREAD, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("hay_block"), Items.HAY_BLOCK)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("flower_pot"), Items.FLOWER_POT)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("feather"), Items.FEATHER, 2, 10)
            .infrequency(2));
        goalAdder.accept(BingoGoal.builder(id("sleep_in_bed"))
            .criterion("sleep", PlayerTrigger.TriggerInstance.sleptInBed())
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.sleep_in_bed"))
            .icon(Items.RED_BED)
            .reactant("sleep"));
        goalAdder.accept(obtainItemGoal(id("charcoal"), Items.CHARCOAL)
            .reactant("use_furnace")
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("coal"), Items.COAL));
        goalAdder.accept(obtainItemGoal(id("fishing_rod"), Items.FISHING_ROD));
        goalAdder.accept(obtainItemGoal(id("apple"), Items.APPLE)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("stick"), Items.STICK, 32, 64)
            .infrequency(2));
        goalAdder.accept(obtainItemGoal(id("kelp"), Items.KELP, 32, 64)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("cod"), Items.COD, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("salmon"), Items.SALMON, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        // TODO: different edible items
        goalAdder.accept(BingoGoal.builder(id("breed_mobs"))
            .criterion("breed", BredAnimalsTrigger.TriggerInstance.bredAnimals())
            .name(Component.translatable("bingo.goal.breed_mobs"))
            .tooltip(Component.translatable("bingo.goal.breed_mobs.tooltip"))
            .antisynergy("breed_animals")
            .infrequency(2)
            .tags(BingoTags.ACTION, BingoTags.STAT)
            .icon(Items.WHEAT_SEEDS)
        );
        goalAdder.accept(crouchDistanceGoal(id("crouch_distance"), 50, 100));
        // TODO: fill all slots of campfire
        goalAdder.accept(BingoGoal.builder(id("dye_sign"))
            .criterion("dye", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(BlockTags.ALL_SIGNS).build()),
                ItemPredicate.Builder.item().of(BingoItemTags.DYES)
            ))
            .name(Component.translatable("bingo.goal.dye_sign"))
            .tags(BingoTags.ACTION)
            .icon(Items.OAK_SIGN)
        );
        goalAdder.accept(BingoGoal.builder(id("extinguish_campfire"))
            .criterion("extinguish", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(Blocks.CAMPFIRE).build()),
                ItemPredicate.Builder.item().of(ItemTags.SHOVELS)
            ))
            .name(Component.translatable("bingo.goal.extinguish_campfire"))
            .tags(BingoTags.ACTION)
            .icon(Items.CAMPFIRE)
        );
        goalAdder.accept(BingoGoal.builder(id("never_pickup_crafting_tables"))
            .criterion("pickup", PickedUpItemTrigger.TriggerInstance.thrownItemPickedUpByPlayer(
                ContextAwarePredicate.ANY, ItemPredicate.Builder.item().of(Items.CRAFTING_TABLE).build(), ContextAwarePredicate.ANY))
            .tags(BingoTags.NEVER)
            .name(Component.translatable("bingo.goal.never_pickup_crafting_tables"))
            .tooltip(Component.translatable("bingo.goal.never_pickup_crafting_tables.tooltip"))
            .icon(Items.CRAFTING_TABLE));
        // TODO: gold items
        goalAdder.accept(obtainItemGoal(id("sand"), Items.SAND, 10, 32)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("sandstone"), Items.SANDSTONE, 5, 10)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("cut_sandstone"), Items.CUT_SANDSTONE, 5, 10)
            .setAntisynergy("sandstone")
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("paper"), Items.PAPER, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(BingoGoal.builder(id("never_fish"))
            .criterion("use", TryUseItemTrigger.builder().item(ItemPredicate.Builder.item().of(Items.FISHING_ROD).build()).build())
            .tags(BingoTags.NEVER, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.never_fish"))
            .tooltip(Component.translatable("bingo.goal.never_fish.tooltip"))
            .icon(Items.FISHING_ROD)
            .catalyst("fishing"));
        goalAdder.accept(BingoGoal.builder(id("break_hoe"))
            .criterion("break", ItemBrokenTrigger.TriggerInstance.itemBroken(ItemPredicate.Builder.item().of(ItemTags.HOES)))
            .tags(BingoTags.ACTION, BingoTags.STAT)
            .name(Component.translatable("bingo.goal.break_hoe"))
            .icon(Items.STONE_HOE));
        goalAdder.accept(BingoGoal.builder(id("bounce_on_bed"))
            .criterion("bounce", BingoTriggers.bounceOnBed())
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.bounce_on_bed"))
            .icon(Blocks.WHITE_BED));
        goalAdder.accept(BingoGoal.builder(id("fill_composter"))
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
            .icon(Blocks.COMPOSTER));

        for (String woodType : List.of("oak", "spruce", "birch", "dark_oak", "acacia")) {
            Item planksItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_planks"));
            goalAdder.accept(obtainItemGoal(id(woodType + "_planks"), planksItem, 32, 64)
                .tags(BingoTags.OVERWORLD)
                .infrequency(25));

            Item logItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_log"));
            goalAdder.accept(obtainItemGoal(id(woodType + "_log"), logItem, 5, 15)
                .tags(BingoTags.OVERWORLD)
                .infrequency(25));

            Item woodItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_wood"));
            goalAdder.accept(obtainItemGoal(id(woodType + "_wood"), woodItem, 5, 10)
                .infrequency(25)
                .tags(BingoTags.OVERWORLD));

            Item strippedWoodItem = BuiltInRegistries.ITEM.get(new ResourceLocation("stripped_" + woodType + "_wood"));
            goalAdder.accept(obtainItemGoal(id("stripped_" + woodType + "_wood"), strippedWoodItem, 5, 10)
                .reactant("axe_use")
                .infrequency(25)
                .tags(BingoTags.OVERWORLD));

            Item strippedLogItem = BuiltInRegistries.ITEM.get(new ResourceLocation("stripped_" + woodType + "_log"));
            goalAdder.accept(obtainItemGoal(id("stripped_" + woodType + "_log"), strippedLogItem, 5, 15)
                .reactant("axe_use")
                .infrequency(25)
                .tags(BingoTags.OVERWORLD));
        }
    }

    private void addEasyGoals(Consumer<BingoGoal.Builder> goalAdder) {
        // TODO: different fish
        goalAdder.accept(BingoGoal.builder(id("grow_tree_in_nether"))
            .criterion("grow", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                LocationPredicate.Builder.location()
                    .setBlock(BlockPredicate.Builder.block().of(BlockTags.OVERWORLD_NATURAL_LOGS).build())
                    .setDimension(Level.NETHER),
                ItemPredicate.Builder.item().of(Items.BONE_MEAL)
            ))
            .tags(BingoTags.ACTION, BingoTags.NETHER, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.grow_tree_in_nether"))
            .tooltip(Component.translatable("bingo.goal.grow_tree_in_nether.tooltip"))
            .icon(Items.BONE_MEAL));
        // TODO: colors of terracotta
        goalAdder.accept(obtainItemGoal(id("mushroom_stew"), Items.MUSHROOM_STEW, 2, 5));
        goalAdder.accept(BingoGoal.builder(id("shoot_button"))
            .criterion("obtain", ArrowPressTrigger.builder()
                .arrow(EntityPredicate.Builder.entity().of(EntityTypeTags.ARROWS).build())
                .buttonOrPlate(BlockPredicate.Builder.block().of(BlockTags.BUTTONS).build())
                .build())
            .tags(BingoTags.ACTION)
            .name(Component.translatable("bingo.goal.shoot_button"))
            .icon(Items.OAK_BUTTON));
        goalAdder.accept(obtainItemGoal(id("writable_book"), Items.WRITABLE_BOOK)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("flint"), Items.FLINT, 16, 64));
        goalAdder.accept(eatEntireCake());
        goalAdder.accept(obtainItemGoal(id("pumpkin_pie"), Items.PUMPKIN_PIE)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(BingoGoal.builder(id("fish_treasure_junk"))
            .criterion("treasure", FishingRodHookedTrigger.TriggerInstance.fishedItem(
                ItemPredicate.ANY, EntityPredicate.ANY, ItemPredicate.Builder.item().of(BingoItemTags.FISHING_TREASURE).build()))
            .criterion("junk", FishingRodHookedTrigger.TriggerInstance.fishedItem(
                ItemPredicate.ANY, EntityPredicate.ANY, ItemPredicate.Builder.item().of(BingoItemTags.FISHING_JUNK).build()))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.fish_treasure_junk"))
            .tooltip(Component.translatable("bingo.goal.fish_treasure_junk.tooltip"))
            .icon(Items.FISHING_ROD)
            .reactant("fishing"));
        goalAdder.accept(obtainItemGoal(id("coarse_dirt"), Items.COARSE_DIRT, 16, 64)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("clock"), Items.CLOCK, 2, 3)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("iron_block"), Items.IRON_BLOCK, 2, 4)
            .infrequency(2));
        goalAdder.accept(obtainItemGoal(id("gold_block"), Items.GOLD_BLOCK)
            .infrequency(2));
        goalAdder.accept(obtainItemGoal(id("golden_apple"), Items.GOLDEN_APPLE));
        goalAdder.accept(obtainItemGoal(id("bookshelf"), Items.BOOKSHELF, 2, 4)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(BingoGoal.builder(id("never_wear_chestplates"))
            .criterion("equip", EquipItemTrigger.builder()
                .newItem(ItemPredicate.Builder.item().of(BingoItemTags.ARMOR_CHESTPLATES).build())
                .slots(EquipmentSlot.CHEST)
                .build())
            .tags(BingoTags.NEVER)
            .name(Component.translatable("bingo.goal.never_wear_chestplates"))
            .icon(Items.IRON_CHESTPLATE)
            .antisynergy("never_wear_armor")
            .catalyst("wear_armor"));
        goalAdder.accept(BingoGoal.builder(id("never_use_shields"))
            .criterion("use", new UsingItemTrigger.TriggerInstance(ContextAwarePredicate.ANY, ItemPredicate.Builder.item().of(BingoItemTags.SHIELDS).build()))
            .tags(BingoTags.NEVER)
            .name(Component.translatable("bingo.goal.never_use_shields"))
            .tooltip(Component.translatable("bingo.goal.never_use_shields.tooltip"))
            .icon(Items.SHIELD));
        goalAdder.accept(obtainItemGoal(id("jukebox"), Items.JUKEBOX));
        // TODO: 3x3x3 cube of glass with lava in middle
        goalAdder.accept(obtainItemGoal(id("mossy_cobblestone"), Items.MOSSY_COBBLESTONE, 16, 32)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("cactus"), Items.CACTUS, 5, 15)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("tnt"), Items.TNT, 2, 3)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainLevelsGoal(id("levels"), 8, 15));
        goalAdder.accept(BingoGoal.builder(id("create_snow_golem"))
            .criterion("summon", SummonedEntityTrigger.TriggerInstance.summonedEntity(
                EntityPredicate.Builder.entity().of(EntityType.SNOW_GOLEM)
            ))
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.create_snow_golem"))
            .icon(Blocks.CARVED_PUMPKIN));
        goalAdder.accept(obtainItemGoal(id("note_block"), Items.NOTE_BLOCK, 5, 15)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("ink_sac"), Items.INK_SAC, 5, 15)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("bread"), Items.BREAD, 6, 10)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("hay_block"), Items.HAY_BLOCK, 2, 3)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        // TODO: colors of wool
        goalAdder.accept(obtainItemGoal(id("piston"), Items.PISTON)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(BingoGoal.builder(id("full_iron_armor"))
            .criterion("armor", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS
            ))
            .tags(BingoTags.ITEM)
            .infrequency(3)
            .name(Component.translatable("bingo.goal.full_iron_armor"))
            .icon(Items.IRON_HELMET));
        goalAdder.accept(BingoGoal.builder(id("full_leather_armor"))
            .criterion("armor", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS
            ))
            .tags(BingoTags.ITEM)
            .infrequency(3)
            .name(Component.translatable("bingo.goal.full_leather_armor"))
            .icon(Items.LEATHER_HELMET));
        // TODO: fill cauldron with water
        goalAdder.accept(BingoGoal.builder(id("complete_map"))
            .criterion("complete", CompleteMapTrigger.TriggerInstance.completeMap())
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.complete_map"))
            .icon(Items.FILLED_MAP)
            .antisynergy("complete_map"));
        goalAdder.accept(obtainItemGoal(id("soul_sand"), Items.SOUL_SAND, 5, 10)
            .tags(BingoTags.NETHER));
        goalAdder.accept(obtainItemGoal(id("soul_soil"), Items.SOUL_SOIL, 5, 10)
            .tags(BingoTags.NETHER));
        goalAdder.accept(obtainItemGoal(id("pumpkin"), Items.PUMPKIN, 5, 10)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("carved_pumpkin"), Items.CARVED_PUMPKIN, 2, 5)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("vine"), Items.VINE, 10, 30)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME));
        // TODO: different slabs
        // TODO: every sword
        // TODO: every pickaxe
        goalAdder.accept(obtainItemGoal(id("bricks"), Items.BRICKS, 16, 64)
            .reactant("use_furnace")
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("nether_bricks"), Items.NETHER_BRICKS, 16, 32)
            .reactant("use_furnace")
            .tags(BingoTags.NETHER));
        goalAdder.accept(obtainItemGoal(id("arrow"), Items.ARROW, 16, 64));
        // TODO: try to sleep in nether
        goalAdder.accept(obtainItemGoal(id("fermented_spider_eye"), Items.FERMENTED_SPIDER_EYE)
            .tags(BingoTags.OVERWORLD));
        // TODO: different stairs
        goalAdder.accept(obtainItemGoal(id("ender_pearl"), Items.ENDER_PEARL, 2, 3)
            .infrequency(2));
        goalAdder.accept(obtainItemGoal(id("egg"), Items.EGG, 16, 16));
        // TODO: hang 3 different 4x4 paintings
        goalAdder.accept(obtainItemGoal(id("bone_block"), Items.BONE_BLOCK, 5, 10));
        // TODO: 2 creepers in the same boat
        // TODO: trade with a villager
        // TODO: different colored shields
        goalAdder.accept(obtainItemGoal(id("dead_bush"), Items.DEAD_BUSH)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME));
        goalAdder.accept(obtainItemGoal(id("grass"), Items.GRASS, 15, 32)
            .tooltip(Component.translatable("bingo.goal.grass.tooltip")));

        for (String dyeColor : List.of("cyan", "magenta", "red", "orange", "yellow", "green", "pink", "purple", "lime")) {
            Item dyeItem = BuiltInRegistries.ITEM.get(new ResourceLocation(dyeColor + "_dye"));
            goalAdder.accept(obtainItemGoal(id(dyeColor + "_dye"), dyeItem)
                .infrequency(10)
                .tags(BingoTags.OVERWORLD));
        }

        goalAdder.accept(BingoGoal.builder(id("never_sleep"))
            .criterion("sleep", PlayerTrigger.TriggerInstance.sleptInBed())
            .tags(BingoTags.NEVER, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.never_sleep"))
            .icon(Items.RED_BED)
            .catalyst("sleep"));
        goalAdder.accept(BingoGoal.builder(id("grow_huge_mushroom"))
            .criterion("grow", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                LocationPredicate.Builder.location()
                    .setBlock(BlockPredicate.Builder.block().of(Blocks.MUSHROOM_STEM).build()),
                ItemPredicate.Builder.item().of(Items.BONE_MEAL)
            ))
            .tags(BingoTags.ACTION)
            .name(Component.translatable("bingo.goal.grow_huge_mushroom"))
            .icon(Blocks.RED_MUSHROOM_BLOCK));
        goalAdder.accept(BingoGoal.builder(id("water_lava_milk"))
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
            .reactant("use_buckets"));
        // TODO: different flowers
        // TODO: colors of concrete
        // TODO: colors of glazed terracotta
        goalAdder.accept(bedRowGoal(id("bed_row"), 3, 6));
        // TODO: finish where you spawned using compass
        goalAdder.accept(obtainItemGoal(id("stone"), Items.STONE, 32, 64)
            .tooltip(Component.translatable("bingo.goal.stone.tooltip"))
            .infrequency(2)
            .reactant("use_furnace")
            .tags(BingoTags.OVERWORLD));
        // TODO: kill passive mobs with only fire
        // TODO: kill creeper with only fire
        goalAdder.accept(obtainItemGoal(id("iron_nugget"), Items.IRON_NUGGET, 32, 64));
        goalAdder.accept(obtainItemGoal(id("gold_nugget"), Items.GOLD_NUGGET, 32, 64));
        goalAdder.accept(obtainItemGoal(id("rotten_flesh"), Items.ROTTEN_FLESH, 16, 32)
            .infrequency(2));
        goalAdder.accept(obtainItemGoal(id("redstone"), Items.REDSTONE, 16, 64)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("golden_carrot"), Items.GOLDEN_CARROT)
            .tags(BingoTags.OVERWORLD));
        // TODO: rotten flesh, spider eye, bone, gunpowder and ender pearl
        goalAdder.accept(obtainItemGoal(id("feather"), Items.FEATHER, 32, 64)
            .infrequency(2));
        goalAdder.accept(obtainItemGoal(id("lily_pad"), Items.LILY_PAD, 2, 10)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME));
        goalAdder.accept(obtainItemGoal(id("stick"), Items.STICK, 65, 128)
            .infrequency(2));
        // TODO: 4 different colors of leather armor at the same time
        goalAdder.accept(obtainItemGoal(id("seagrass"), Items.SEAGRASS, 15, 32)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));

        for (String woodType : List.of("oak", "spruce", "birch", "dark_oak", "acacia")) {
            Item planksItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_planks"));
            goalAdder.accept(obtainItemGoal(id(woodType + "_planks"), planksItem, 65, 128)
                .tags(BingoTags.OVERWORLD)
                .infrequency(25));

            Item logItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_log"));
            goalAdder.accept(obtainItemGoal(id(woodType + "_log"), logItem, 16, 32)
                .tags(BingoTags.OVERWORLD)
                .infrequency(25));

            Item woodItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_wood"));
            goalAdder.accept(obtainItemGoal(id(woodType + "_wood"), woodItem, 11, 20)
                .infrequency(25)
                .tags(BingoTags.OVERWORLD));

            Item strippedWoodItem = BuiltInRegistries.ITEM.get(new ResourceLocation("stripped_" + woodType + "_wood"));
            goalAdder.accept(obtainItemGoal(id("stripped_" + woodType + "_wood"), strippedWoodItem, 11, 20)
                .reactant("axe_use")
                .infrequency(25)
                .tags(BingoTags.OVERWORLD));

            Item strippedLogItem = BuiltInRegistries.ITEM.get(new ResourceLocation("stripped_" + woodType + "_log"));
            goalAdder.accept(obtainItemGoal(id("stripped_" + woodType + "_log"), strippedLogItem, 16, 32)
                .reactant("axe_use")
                .infrequency(25)
                .tags(BingoTags.OVERWORLD));
        }

        goalAdder.accept(obtainItemGoal(id("tropical_fish"), Items.TROPICAL_FISH)
            .tags(BingoTags.OVERWORLD, BingoTags.OCEAN));
        goalAdder.accept(obtainItemGoal(id("pufferfish"), Items.PUFFERFISH)
            .tags(BingoTags.OVERWORLD, BingoTags.OCEAN));
        goalAdder.accept(obtainItemGoal(id("cod"), Items.COD, 6, 15)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD, BingoTags.OCEAN));
        goalAdder.accept(obtainItemGoal(id("salmon"), Items.SALMON, 4, 7)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD, BingoTags.OCEAN));
        goalAdder.accept(BingoGoal.builder(id("never_use_boat"))
            .criterion("use", new PlayerInteractTrigger.TriggerInstance(
                ContextAwarePredicate.ANY,
                ItemPredicate.ANY,
                EntityPredicate.wrap(EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(EntityType.BOAT)).build())))
            .tags(BingoTags.NEVER, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.never_use_boat"))
            .icon(Items.OAK_BOAT));
        // TODO: get a fish into the nether
        goalAdder.accept(obtainItemGoal(id("dried_kelp_block"), Items.DRIED_KELP_BLOCK, 11, 20)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        // TODO: drown a zombie
        goalAdder.accept(obtainItemGoal(id("gunpowder"), Items.GUNPOWDER, 2, 5)
            .infrequency(2));
        goalAdder.accept(obtainItemGoal(id("spider_eye"), Items.SPIDER_EYE, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        // TODO: different edible items
        // TODO: breed 2-4 sets of mobs
        goalAdder.accept(crouchDistanceGoal(id("crouch_distance"), 100, 200));
        // TODO: never use debug
        // TODO: ring bell from 10 blocks away
        // TODO: repair item with grindstone
        goalAdder.accept(obtainItemGoal(id("sweet_berries"), Items.SWEET_BERRIES, 2, 6)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME));
        // TODO: banner pattern
        // TODO: drink sussy stew
        goalAdder.accept(BingoGoal.builder(id("drink_sus_stew"))
            .criterion("drink", ConsumeItemTrigger.TriggerInstance.usedItem(Items.SUSPICIOUS_STEW))
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD)
            .reactant("eat_non_meat")
            .name(Component.translatable("bingo.goal.drink_sus_stew", Items.SUSPICIOUS_STEW.getDescription()))
            .icon(Items.SUSPICIOUS_STEW));
        // TODO: give fox sword
        goalAdder.accept(obtainItemGoal(id("honey_bottle"), Items.HONEY_BOTTLE)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("honeycomb"), Items.HONEYCOMB, 3, 3)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("basalt"), Items.BASALT, 2, 6)
            .tags(BingoTags.NETHER));
        goalAdder.accept(obtainItemGoal(id("blackstone"), Items.BLACKSTONE, 2, 6)
            .tags(BingoTags.NETHER));
        // TODO: fill 4 slots of soul campfire with porkchops
        goalAdder.accept(obtainItemGoal(id("soul_lantern"), Items.SOUL_LANTERN)
            .tags(BingoTags.NETHER));
        // TODO: open door with target block from 10 blocks away
        goalAdder.accept(obtainItemGoal(id("carrot_on_a_stick"), Items.CARROT_ON_A_STICK)
            .tags(BingoTags.OVERWORLD));
        // TODO: barter with piglin
        // TODO: become nauseous
        // TODO: enchanted item
        // TODO: remove enchantment with grindstone
        // TODO: never use sword
        // TODO: carnivore
        // TODO: clean banner
        // TODO: 5-7 different gold items
        goalAdder.accept(obtainItemGoal(id("sand"), Items.SAND, 33, 64)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("sandstone"), Items.SANDSTONE, 11, 32)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("cut_sandstone"), Items.CUT_SANDSTONE, 11, 32)
            .setAntisynergy("sandstone")
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("paper"), Items.PAPER, 6, 15)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("coal_block"), Items.COAL_BLOCK, 3, 6)
            .reactant("never_coal"));
        goalAdder.accept(obtainItemGoal(id("apple"), Items.APPLE, 2, 5)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(BingoGoal.builder(id("tame_horse"))
            .criterion("obtain", TameAnimalTrigger.TriggerInstance.tamedAnimal(EntityPredicate.Builder.entity().of(EntityType.HORSE).build()))
            .name(Component.translatable("bingo.goal.tame_horse"))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .icon(Items.HORSE_SPAWN_EGG));
        // TODO: hatch chicken from egg
        // TODO: empty cauldron without buckets or bottles
        // TODO: sleep in villager's bed
        // TODO: set fire to villager's house
        goalAdder.accept(obtainItemGoal(id("emerald"), Items.EMERALD)
            .tags(BingoTags.OVERWORLD));
    }

    private BingoGoal.Builder eatEntireCake() {
        BingoGoal.Builder builder = BingoGoal.builder(id("eat_entire_cake"));
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
            .reactant("eat_non_meat");
    }

    private void addMediumGoals(Consumer<BingoGoal.Builder> goalAdder) {
        // TODO: different edible items
        goalAdder.accept(obtainItemGoal(id("beetroot_soup"), Items.BEETROOT_SOUP)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(BingoGoal.builder(id("potted_cactus"))
            .criterion("pot", new ItemUsedOnLocationTrigger.TriggerInstance(
                CriteriaTriggers.ITEM_USED_ON_BLOCK.getId(),
                ContextAwarePredicate.ANY,
                ContextAwarePredicate.create(LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.POTTED_CACTUS).build())))
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.potted_cactus"))
            .icon(Items.CACTUS));
        // TODO: detonate TNT minecart
        goalAdder.accept(obtainItemGoal(id("magma_block"), Items.MAGMA_BLOCK, 10, 30));
        goalAdder.accept(obtainItemGoal(id("damaged_anvil"), Items.DAMAGED_ANVIL));
        goalAdder.accept(obtainItemGoal(id("melon_slice"), Items.MELON_SLICE, 16, 64)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(BingoGoal.builder(id("never_wear_armor"))
            .criterion("equip", EquipItemTrigger.builder().newItem(ItemPredicate.Builder.item().of(BingoItemTags.ARMOR).build()).build())
            .tags(BingoTags.NEVER)
            .name(Component.translatable("bingo.goal.never_wear_armor"))
            .icon(Items.DIAMOND_CHESTPLATE)
            .antisynergy("never_wear_armor")
            .catalyst("wear_armor"));
        goalAdder.accept(BingoGoal.builder(id("skeleton_bow"))
            .criterion("pickup", ItemPickedUpTrigger.TriggerInstance.pickedUpFrom(
                ItemPredicate.Builder.item().of(Items.BOW).build(),
                EntityPredicate.Builder.entity().of(EntityType.SKELETON).build()
            ))
            .reactant("pacifist")
            .tags(BingoTags.ITEM, BingoTags.COMBAT)
            .name(Component.translatable("bingo.goal.skeleton_bow"))
            .icon(Items.BOW));
        goalAdder.accept(obtainItemGoal(id("diamond_block"), Items.DIAMOND_BLOCK)
            .infrequency(2));
        goalAdder.accept(obtainItemGoal(id("lapis_block"), Items.LAPIS_BLOCK, 2, 3)
            .tags(BingoTags.OVERWORLD));
        // TODO: different saplings
        goalAdder.accept(BingoGoal.builder(id("tame_wolf"))
            .criterion("obtain", TameAnimalTrigger.TriggerInstance.tamedAnimal(EntityPredicate.Builder.entity().of(EntityType.WOLF).build()))
            .tags(BingoTags.STAT, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.tame_wolf"))
            .icon(Items.BONE));
        goalAdder.accept(obtainItemGoal(id("fire_charge"), Items.FIRE_CHARGE, 6, 6)
            .tags(BingoTags.NETHER, BingoTags.COMBAT));
        goalAdder.accept(obtainItemGoal(id("magma_cream"), Items.MAGMA_CREAM, 2, 3)
            .tags(BingoTags.NETHER, BingoTags.COMBAT));
        // TODO: create iron golem
        goalAdder.accept(obtainItemGoal(id("ender_eye"), Items.ENDER_EYE)
            .reactant("pacifist")
            .tags(BingoTags.NETHER, BingoTags.COMBAT));
        goalAdder.accept(obtainItemGoal(id("rabbit_stew"), Items.RABBIT_STEW)
            .tags(BingoTags.OVERWORLD));

        goalAdder.accept(potionGoal("fire_resistance_potion", Potions.FIRE_RESISTANCE, Potions.LONG_FIRE_RESISTANCE));
        goalAdder.accept(potionGoal("healing_potion", Potions.HEALING, Potions.STRONG_HEALING)
            .tags(BingoTags.OVERWORLD, BingoTags.COMBAT));
        goalAdder.accept(potionGoal("poison_potion", Potions.POISON, Potions.LONG_POISON, Potions.STRONG_POISON)
            .tags(BingoTags.OVERWORLD, BingoTags.COMBAT));
        goalAdder.accept(potionGoal("harming_potion", Potions.HARMING, Potions.STRONG_HARMING)
            .tags(BingoTags.OVERWORLD, BingoTags.COMBAT));
        goalAdder.accept(potionGoal("regeneration_potion", Potions.REGENERATION, Potions.LONG_REGENERATION, Potions.STRONG_REGENERATION)
            .tags(BingoTags.COMBAT));
        goalAdder.accept(potionGoal("slowness_potion", Potions.SLOWNESS, Potions.LONG_SLOWNESS, Potions.STRONG_SLOWNESS)
            .tags(BingoTags.OVERWORLD, BingoTags.COMBAT));
        goalAdder.accept(potionGoal("strength_potion", Potions.STRENGTH, Potions.LONG_STRENGTH, Potions.STRONG_STRENGTH)
            .tags(BingoTags.COMBAT));
        goalAdder.accept(potionGoal("swiftness_potion", Potions.SWIFTNESS, Potions.LONG_SWIFTNESS, Potions.STRONG_SWIFTNESS)
            .tags(BingoTags.OVERWORLD, BingoTags.COMBAT));
        goalAdder.accept(potionGoal("weakness_potion", Potions.WEAKNESS, Potions.LONG_WEAKNESS)
            .tags(BingoTags.OVERWORLD, BingoTags.COMBAT));
        goalAdder.accept(potionGoal("leaping_potion", Potions.LEAPING, Potions.LONG_LEAPING, Potions.STRONG_LEAPING)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD, BingoTags.COMBAT));
        goalAdder.accept(potionGoal("slow_falling_potion", Potions.SLOW_FALLING, Potions.LONG_SLOW_FALLING)
            .tags(BingoTags.COMBAT, BingoTags.OVERWORLD));
        goalAdder.accept(potionGoal("turtle_master_potion", Potions.TURTLE_MASTER, Potions.LONG_TURTLE_MASTER, Potions.STRONG_TURTLE_MASTER)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD, BingoTags.COMBAT));

        // TODO: finish by jumping from top to bottom of world
        // TODO: vegetarian
        goalAdder.accept(BingoGoal.builder(id("kill_self_with_arrow"))
            .criterion("kill", KillSelfTrigger.TriggerInstance.killSelf(
                DamageSourcePredicate.Builder.damageType()
                    .tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE))
                    .direct(EntityPredicate.Builder.entity().of(EntityTypeTags.ARROWS))
                    .build()
            ))
            .tags(BingoTags.ACTION)
            .name(Component.translatable("bingo.goal.kill_self_with_arrow"))
            .icon(Items.ARROW));
        goalAdder.accept(BingoGoal.builder(id("whilst_trying_to_escape"))
            .criterion("die", new EntityKilledPlayerTrigger.TriggerInstance(
                ContextAwarePredicate.ANY,
                ContextAwarePredicate.ANY,
                ContextAwarePredicate.ANY,
                ContextAwarePredicate.create(
                    LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.ANY).build(),
                    InvertedLootItemCondition.invert(LootItemEntityPropertyCondition.hasProperties(
                        LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().build()
                    )).build()
                ),
                DamageSourcePredicate.ANY
            ))
            .tags(BingoTags.ACTION)
            .name(Component.translatable("bingo.goal.whilst_trying_to_escape"))
            .tooltip(Component.translatable("bingo.goal.whilst_trying_to_escape.tooltip"))
            .icon(Items.ZOMBIE_HEAD)
        );
        // TODO: finish on top of the world
        // TODO: kill hostile mob with gravel
        // TODO: kill hostile mob with sand
        // TODO: put carpet on llama
        goalAdder.accept(BingoGoal.builder(id("sized_nether_portal"))
            .sub("width", BingoSub.random(4, 6))
            .sub("height", BingoSub.random(4, 6))
            .criterion("activate",
                ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(
                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.NETHER_PORTAL),
                    BlockPatternCondition.builder().aisle("P")
                        .where('P', BlockPredicate.Builder.block().of(Blocks.NETHER_PORTAL).build())
                ),
                subber -> subber.sub("conditions.location.1.aisles.0", new BingoSub.CompoundBingoSub(
                    BingoSub.CompoundBingoSub.ElementType.ARRAY,
                    BingoSub.CompoundBingoSub.Operator.MUL,
                    BingoSub.wrapInArray(
                        new BingoSub.CompoundBingoSub(
                            BingoSub.CompoundBingoSub.ElementType.STRING,
                            BingoSub.CompoundBingoSub.Operator.MUL,
                            BingoSub.literal("P"),
                            new BingoSub.SubBingoSub("width")
                        )
                    ),
                    new BingoSub.SubBingoSub("height")
                ))
            )
            .tags(BingoTags.ACTION, BingoTags.BUILD, BingoTags.NETHER)
            .name(
                Component.translatable("bingo.goal.sized_nether_portal", 0, 0),
                subber -> subber.sub("with.0", "width").sub("with.1", "height")
            )
            .icon(Blocks.OBSIDIAN));
        goalAdder.accept(obtainItemGoal(id("obsidian"), Items.OBSIDIAN, 3, 10));
        goalAdder.accept(obtainItemGoal(id("iron_block"), Items.IRON_BLOCK, 5, 7)
            .infrequency(2));
        goalAdder.accept(obtainItemGoal(id("gold_block"), Items.GOLD_BLOCK, 2, 4)
            .infrequency(2));
        goalAdder.accept(obtainItemGoal(id("daylight_detector"), Items.DAYLIGHT_DETECTOR)
            .tags(BingoTags.OVERWORLD, BingoTags.NETHER));
        // TODO: enchanted golden sword
        // TODO: colors of wool
        // TODO: colors of terracotta
        // TODO: colors of glazed terracotta
        // TODO: colors of concrete
        goalAdder.accept(bedRowGoal(id("bed_row"), 7, 10));
        // TODO: power redstone lamp
        // TODO: different flowers
        // TODO: put zombified piglin in water
        goalAdder.accept(mineralPillarGoal(id("basic_mineral_blocks"), BingoBlockTags.BASIC_MINERAL_BLOCKS)
            .name(Component.translatable("bingo.goal.basic_mineral_blocks"))
            .tags(BingoTags.OVERWORLD)
            .icon(Blocks.DIAMOND_BLOCK)
        );
        // TODO: kill hostile mob with anvil
        goalAdder.accept(obtainLevelsGoal(id("levels"), 16, 26)
            .infrequency(2));
        // TODO: different seeds
        // TODO: 4 different armor types
        // TODO: fill hopper with 320 items
        goalAdder.accept(obtainItemGoal(id("red_nether_bricks"), Items.RED_NETHER_BRICKS, 16, 32)
            .reactant("use_furnace")
            .tags(BingoTags.NETHER));
        goalAdder.accept(obtainItemGoal(id("spectral_arrow"), Items.SPECTRAL_ARROW, 16, 32)
            .tags(BingoTags.NETHER));
        goalAdder.accept(obtainItemGoal(id("rotten_flesh"), Items.ROTTEN_FLESH, 33, 64)
            .infrequency(2));
        goalAdder.accept(obtainItemGoal(id("ink_sac"), Items.INK_SAC, 16, 32)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("slime_ball"), Items.SLIME_BALL, 5, 9)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD));
        // TODO: lead on rabbit
        goalAdder.accept(obtainItemGoal(id("firework_star"), Items.FIREWORK_STAR)
            .tags(BingoTags.OVERWORLD));
        // TODO: hang mob with lead
        goalAdder.accept(obtainItemGoal(id("blaze_rod"), Items.BLAZE_ROD)
            .reactant("pacifist")
            .tags(BingoTags.NETHER, BingoTags.COMBAT));
        goalAdder.accept(obtainItemGoal(id("ghast_tear"), Items.GHAST_TEAR)
            .reactant("pacifist")
            .tags(BingoTags.NETHER, BingoTags.COMBAT));
        // TODO: never use coal
        goalAdder.accept(obtainItemGoal(id("glowstone_dust"), Items.GLOWSTONE_DUST, 32, 64)
            .tags(BingoTags.NETHER));
        goalAdder.accept(obtainItemGoal(id("item_frame"), Items.ITEM_FRAME, 10, 32));
        // TODO: different diamond items
        goalAdder.accept(obtainItemGoal(id("prismarine_crystals"), Items.PRISMARINE_CRYSTALS, 2, 4)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        // TODO: dig straight down to bedrock from sea level (1x1 hole)
        // TODO: deplete diamond sword
        goalAdder.accept(obtainItemGoal(id("saddle"), Items.SADDLE));
        // TODO: give mob hat
        goalAdder.accept(obtainItemGoal(id("heart_of_the_sea"), Items.HEART_OF_THE_SEA)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("phantom_membrane"), Items.PHANTOM_MEMBRANE)
            .reactant("pacifist")
            .tags(BingoTags.COMBAT, BingoTags.OVERWORLD));
        // TODO: add marker to map
        // TODO: water, lava, milk, fish bucket
        // TODO: leash dolphin to fence
        goalAdder.accept(obtainItemGoal(id("dried_kelp_block"), Items.DRIED_KELP_BLOCK, 21, 32)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("gunpowder"), Items.GUNPOWDER, 6, 15)
            .infrequency(2)
            .tags(BingoTags.COMBAT));
        goalAdder.accept(obtainItemGoal(id("spider_eye"), Items.SPIDER_EYE, 6, 15)
            .infrequency(2)
            .tags(BingoTags.COMBAT, BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("ender_pearl"), Items.ENDER_PEARL, 4, 6)
            .infrequency(2)
            .tags(BingoTags.COMBAT));
        // TODO: never use axe
        // TODO: enchant an item
        // TODO: blue shield with white flower charge pattern
        goalAdder.accept(BingoGoal.builder(id("tame_cat"))
            .criterion("obtain", TameAnimalTrigger.TriggerInstance.tamedAnimal(EntityPredicate.Builder.entity().of(EntityType.CAT).build()))
            .name(Component.translatable("bingo.goal.tame_cat"))
            .tags(BingoTags.ACTION, BingoTags.VILLAGE, BingoTags.OVERWORLD)
            .icon(Items.CAT_SPAWN_EGG));
        // TODO: breed mobs
        goalAdder.accept(crouchDistanceGoal(id("crouch_distance"), 200, 400));
        // TODO: kill n mobs
        goalAdder.accept(obtainItemGoal(id("seagrass"), Items.SEAGRASS, 33, 64)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        // TODO: kill iron golem
        // TODO: kill mob with end crystal
        goalAdder.accept(BingoGoal.builder(id("never_craft_sticks"))
            .criterion("craft", RecipeCraftedTrigger.TriggerInstance.craftedItem(new ResourceLocation("stick")))
            .tags(BingoTags.NEVER, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.never_craft_sticks"))
            .icon(Items.STICK));
        // TODO: light campfire from 10 blocks away
        // TODO: max scale map
        // TODO: ignite TNT with lectern
        // TODO: kill hostile mob with berry bush
        goalAdder.accept(BingoGoal.builder(id("pillager_crossbow"))
            .criterion("pickup", ItemPickedUpTrigger.TriggerInstance.pickedUpFrom(
                ItemPredicate.Builder.item().of(Items.CROSSBOW).build(),
                EntityPredicate.Builder.entity().of(EntityType.PILLAGER).build()
            ))
            .reactant("pacifist")
            .tags(BingoTags.ITEM, BingoTags.COMBAT, BingoTags.OVERWORLD, BingoTags.RARE_BIOME)
            .name(Component.translatable("bingo.goal.pillager_crossbow"))
            .icon(Items.CROSSBOW));
        ItemStack ominousBanner = Raid.getLeaderBannerInstance();
        goalAdder.accept(obtainItemGoal(id("ominous_banner"), ominousBanner, ItemPredicate.Builder.item().of(ominousBanner.getItem()).hasNbt(ominousBanner.getTag()))
            .antisynergy("ominous_banner")
            .name(ominousBanner.getHoverName())
            .reactant("pacifist")
            .tags(BingoTags.COMBAT, BingoTags.OVERWORLD));
        // TODO: gain a fox's trust
        goalAdder.accept(obtainItemGoal(id("honey_block"), Items.HONEY_BLOCK)
            .setAntisynergy("honey")
            .infrequency(2)
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("honeycomb_block"), Items.HONEYCOMB_BLOCK, 3, 3)
            .setAntisynergy("honeycomb")
            .infrequency(2)
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD));
        // TODO: repair iron golem
        // TODO: grow tree with benis attached

        for (String woodType : List.of("warped", "crimson")) {
            Item stemItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_stem"));
            goalAdder.accept(obtainItemGoal(id(woodType + "_stem"), stemItem, 16, 32)
                .infrequency(25)
                .tags(BingoTags.NETHER));

            Item strippedStemItem = BuiltInRegistries.ITEM.get(new ResourceLocation("stripped_" + woodType + "_stem"));
            goalAdder.accept(obtainItemGoal(id("stripped_" + woodType + "_stem"), strippedStemItem, 16, 32)
                .infrequency(25)
                .tags(BingoTags.NETHER));

            Item hyphaeItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_hyphae"));
            goalAdder.accept(obtainItemGoal(id(woodType + "_hyphae"), hyphaeItem, 16, 32)
                .infrequency(25)
                .tags(BingoTags.NETHER));

            Item strippedHyphaeItem = BuiltInRegistries.ITEM.get(new ResourceLocation("stripped_" + woodType + "_hyphae"));
            goalAdder.accept(obtainItemGoal(id("stripped_" + woodType + "_hyphae"), strippedHyphaeItem, 16, 32)
                .infrequency(25)
                .tags(BingoTags.NETHER));

            Item planksItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_planks"));
            goalAdder.accept(obtainItemGoal(id(woodType + "_planks"), planksItem, 16, 32)
                .infrequency(25)
                .tags(BingoTags.NETHER));
        }

        goalAdder.accept(obtainItemGoal(id("quartz_block"), Items.QUARTZ_BLOCK)
            .tags(BingoTags.NETHER));
        // TODO: try to use respawn anchor in overworld
        goalAdder.accept(obtainItemGoal(id("warped_fungus_on_a_stick"), Items.WARPED_FUNGUS_ON_A_STICK)
            .tags(BingoTags.NETHER));
        // TODO: convert hoglin into zoglin
        // TODO: ride strider
        // TODO: damage strider with water
        goalAdder.accept(obtainItemGoal(id("bamboo"), Items.BAMBOO, 6, 15)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME));
        goalAdder.accept(obtainItemGoal(id("crying_obsidian"), Items.CRYING_OBSIDIAN));
        // TODO: kill self with ender pearl
        goalAdder.accept(obtainItemGoal(id("grass_block"), Items.GRASS_BLOCK)
            .tooltip(Component.translatable("bingo.goal.grass_block.tooltip"))
            .tags(BingoTags.OVERWORLD));
        // TODO: bounce on slime block
        // TODO: full gold armor
        goalAdder.accept(obtainItemGoal(id("brown_wool"), Items.BROWN_WOOL)
            .tags(BingoTags.OVERWORLD));
        // TODO: grow huge nether fungus
        // TODO: put chest on donkey
        goalAdder.accept(BingoGoal.builder(id("never_place_torches"))
            .criterion("place", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(Blocks.TORCH))
            .tags(BingoTags.NEVER)
            .name(Component.translatable("bingo.goal.never_place_torches"))
            .tooltip(Component.translatable("bingo.goal.never_place_torches.tooltip"))
            .icon(Items.TORCH));
        goalAdder.accept(obtainItemGoal(id("scute"), Items.SCUTE)
            .setAntisynergy("turtle_helmet")
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
    }

    private BingoGoal.Builder potionGoal(String id, Potion... potions) {
        ItemStack potionItem = PotionUtils.setPotion(new ItemStack(Items.POTION), potions[0]);
        BingoGoal.Builder builder = obtainItemGoal(
                id(id),
                potionItem,
                Arrays.stream(potions).map(potion -> ItemPredicate.Builder.item().of(Items.POTION).isPotion(potion)).toArray(ItemPredicate.Builder[]::new))
            .antisynergy(id)
            .name(Items.POTION.getName(potionItem))
            .infrequency(12)
            .tags(BingoTags.NETHER);
        if (potions[0] != Potions.FIRE_RESISTANCE) {
            builder.reactant("pacifist");
        }
        return builder;
    }

    private void addHardGoals(Consumer<BingoGoal.Builder> goalAdder) {
        goalAdder.accept(BingoGoal.builder(id("level_10_enchant"))
            .criterion("enchant", EnchantedItemTrigger.builder().requiredLevels(MinMaxBounds.Ints.atLeast(10)).build())
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.level_10_enchant"))
            .icon(new ItemStack(Items.ENCHANTING_TABLE, 10)));
        goalAdder.accept(BingoGoal.builder(id("milk_mooshroom"))
            .criterion("obtain", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
                ItemPredicate.Builder.item().of(Items.BUCKET),
                EntityPredicate.wrap(EntityPredicate.Builder.entity().of(EntityType.MOOSHROOM).build())))
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.milk_mooshroom"))
            .icon(Items.MOOSHROOM_SPAWN_EGG)
            .infrequency(2));
        goalAdder.accept(BingoGoal.builder(id("shear_mooshroom"))
            .criterion("obtain", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
                ItemPredicate.Builder.item().of(Items.SHEARS),
                EntityPredicate.wrap(EntityPredicate.Builder.entity().of(EntityType.MOOSHROOM).build())))
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.shear_mooshroom"))
            .icon(Items.COW_SPAWN_EGG)
            .infrequency(2));
        goalAdder.accept(obtainItemGoal(id("sea_lantern"), Items.SEA_LANTERN, 2, 5)
            .tags(BingoTags.OVERWORLD, BingoTags.OCEAN));
        goalAdder.accept(obtainItemGoal(id("sponge"), Items.SPONGE)
            .tooltip(Component.translatable("bingo.goal.sponge.tooltip"))
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        goalAdder.accept(BingoGoal.builder(id("listen_to_music"))
            .criterion("obtain", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(Blocks.JUKEBOX).build()),
                ItemPredicate.Builder.item().of(ItemTags.MUSIC_DISCS)))
            .tags(BingoTags.ITEM)
            .name(Component.translatable("bingo.goal.listen_to_music"))
            .icon(Items.JUKEBOX));
        goalAdder.accept(obtainSomeItemsFromTag(id("flowers"), Items.AZURE_BLUET, BingoItemTags.FLOWERS, "bingo.goal.flowers", 11, 14)
            .antisynergy("flowers")
            .infrequency(3)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("diamond_block"), Items.DIAMOND_BLOCK, 2, 4)
            .infrequency(2));
        goalAdder.accept(BingoGoal.builder(id("zombified_piglin_sword"))
            .criterion("pickup", ItemPickedUpTrigger.TriggerInstance.pickedUpFrom(
                ItemPredicate.Builder.item().of(Items.GOLDEN_SWORD).build(),
                EntityPredicate.Builder.entity().of(EntityType.ZOMBIFIED_PIGLIN).build()
            ))
            .reactant("pacifist")
            .tags(BingoTags.ITEM, BingoTags.COMBAT, BingoTags.NETHER)
            .name(Component.translatable("bingo.goal.zombified_piglin_sword"))
            .icon(Items.GOLDEN_SWORD));
        // TODO: finish by launching foreworks of n different colors
        goalAdder.accept(BingoGoal.builder(id("nametag_enderman"))
            .criterion("nametag", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
                ItemPredicate.Builder.item().of(Items.NAME_TAG),
                EntityPredicate.wrap(EntityPredicate.Builder.entity().of(EntityType.ENDERMAN).build())))
            .tags(BingoTags.ACTION, BingoTags.COMBAT, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.nametag_enderman"))
            .icon(Items.NAME_TAG));
        // TODO: finish on top of blaze spawner
        goalAdder.accept(obtainSomeItemsFromTag(id("wool"), Items.PURPLE_WOOL, ItemTags.WOOL, "bingo.goal.wool", 12, 14)
            .antisynergy("wool_color")
            .infrequency(4)
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD));
        goalAdder.accept(obtainSomeItemsFromTag(id("terracotta"), Items.PURPLE_TERRACOTTA, ItemTags.TERRACOTTA, "bingo.goal.terracotta", 12, 14)
            .reactant("use_furnace")
            .antisynergy("terracotta_color")
            .infrequency(4)
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD));
        goalAdder.accept(obtainSomeItemsFromTag(id("glazed_terracotta"), Items.PURPLE_GLAZED_TERRACOTTA, BingoItemTags.GLAZED_TERRACOTTA, "bingo.goal.glazed_terracotta", 11, 14)
            .reactant("use_furnace")
            .antisynergy("glazed_terracotta_color")
            .infrequency(4)
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD));
        goalAdder.accept(obtainSomeItemsFromTag(id("concrete"), Items.PURPLE_CONCRETE, BingoItemTags.CONCRETE, "bingo.goal.concrete", 12, 14)
            .antisynergy("concrete_color")
            .infrequency(4)
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD));
        goalAdder.accept(bedRowGoal(id("bed_row"), 11, 14));
        goalAdder.accept(BingoGoal.builder(id("poison_parrot"))
            .criterion("poison", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
                ItemPredicate.Builder.item().of(Items.COOKIE),
                EntityPredicate.wrap(EntityPredicate.Builder.entity().of(EntityType.PARROT).build())))
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.poison_parrot"))
            .icon(Items.COOKIE)
            .infrequency(2)
            .reactant("pacifist"));
        goalAdder.accept(BingoGoal.builder(id("tame_parrot"))
            .criterion("tame", TameAnimalTrigger.TriggerInstance.tamedAnimal(EntityPredicate.Builder.entity().of(EntityType.PARROT).build()))
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.tame_parrot"))
            .icon(Items.PARROT_SPAWN_EGG)
            .infrequency(2));
        goalAdder.accept(BingoGoal.builder(id("ice_on_magma"))
            .criterion("obtain", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(
                AnyOfCondition.anyOf(LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.ICE),
                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.MAGMA_BLOCK)),
                BlockPatternCondition.builder().aisle("i", "m")
                    .where('i', BlockPredicate.Builder.block().of(Blocks.ICE).build())
                    .where('m', BlockPredicate.Builder.block().of(Blocks.MAGMA_BLOCK).build())
                    .rotations(BlockPattern.Rotations.NONE)))
            .tags(BingoTags.ITEM, BingoTags.BUILD, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.ice_on_magma"))
            .icon(Items.BASALT));
        goalAdder.accept(obtainLevelsGoal(id("levels"), 27, 37));
        // TODO: build an ice cube
        // TODO: finish on top of stairway to heaven
        goalAdder.accept(BingoGoal.builder(id("kill_ghast_in_overworld"))
            .criterion("murder", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(EntityType.GHAST)
                .located(LocationPredicate.inDimension(Level.OVERWORLD))))
            .name(Component.translatable("bingo.goal.kill_ghast_in_overworld"))
            .tags(BingoTags.ACTION, BingoTags.NETHER, BingoTags.OVERWORLD)
            .icon(Items.GHAST_TEAR)
            .reactant("pacifist"));
        goalAdder.accept(obtainItemGoal(id("enchanted_golden_apple"), Items.ENCHANTED_GOLDEN_APPLE));

        goalAdder.accept(BingoGoal.builder(id("never_wear_armor_or_use_shields"))
            .criterion("equip", EquipItemTrigger.builder().newItem(ItemPredicate.Builder.item().of(BingoItemTags.ARMOR).build()).build())
            .criterion("use", new UsingItemTrigger.TriggerInstance(ContextAwarePredicate.ANY, ItemPredicate.Builder.item().of(BingoItemTags.SHIELDS).build()))
            .requirements(List.of("equip", "use"))
            .tags(BingoTags.NEVER)
            .name(Component.translatable("bingo.goal.never_wear_armor_or_use_shields"))
            .tooltip(Component.translatable("bingo.goal.never_wear_armor_or_use_shields.tooltip"))
            .icon(makeItemWithGlint(Items.SHIELD))
            .antisynergy("never_wear_armor")
            .catalyst("wear_armor"));
        // TODO: kill mob that is wearing full armor
        // TODO: enchant 5 items
        // TODO: never use buckets
        goalAdder.accept(obtainItemGoal(id("conduit"), Items.CONDUIT)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        // TODO: 2-5 types of dead coral blocks
        goalAdder.accept(obtainItemGoal(id("sea_pickle"), Items.SEA_PICKLE, 16, 32)
            .tags(BingoTags.OCEAN, BingoTags.RARE_BIOME, BingoTags.OVERWORLD));
        // TODO: didn't want to live in the same world as death message
        goalAdder.accept(obtainItemGoal(id("cookie"), Items.COOKIE)
            .tags(BingoTags.OVERWORLD));
        // TODO: grow full jungle tree
        goalAdder.accept(obtainItemGoal(id("prismarine_shard"), Items.PRISMARINE_SHARD, 2, 10)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("jungle_log"), Items.JUNGLE_LOG, 16, 32)
            .infrequency(4)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("jungle_wood"), Items.JUNGLE_WOOD, 11, 20)
            .infrequency(4)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("stripped_jungle_wood"), Items.STRIPPED_JUNGLE_WOOD, 11, 20)
            .reactant("axe_use")
            .infrequency(4)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("stripped_jungle_log"), Items.STRIPPED_JUNGLE_LOG, 11, 20)
            .reactant("axe_use")
            .infrequency(4)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD));
        // TODO: different diamond items
        // TODO: destroy mob spawner
        goalAdder.accept(obtainItemGoal(id("popped_chorus_fruit"), Items.POPPED_CHORUS_FRUIT, 32, 64)
            .tags(BingoTags.END));
        // TODO: get villager into the end
        goalAdder.accept(obtainItemGoal(id("dragon_breath"), Items.DRAGON_BREATH, 5, 16)
            .tags(BingoTags.COMBAT, BingoTags.END));
        goalAdder.accept(obtainItemGoal(id("dragon_egg"), Items.DRAGON_EGG)
            .tags(BingoTags.COMBAT, BingoTags.END));
        goalAdder.accept(BingoGoal.builder(id("complete_full_size_map"))
            .criterion("complete", CompleteMapTrigger.TriggerInstance.completeMap(
                MinMaxBounds.Ints.atLeast(MapItemSavedData.MAX_SCALE)
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.complete_full_size_map"))
            .icon(Items.FILLED_MAP)
            .antisynergy("complete_map"));
        // TODO: be killed by a villager
        // TODO: pop a totem
        // TODO: every type of sword
        // TODO: every type of pickaxe
        goalAdder.accept(BingoGoal.builder(id("pacifist"))
            .criterion("kill", KilledTrigger.TriggerInstance.playerKilledEntity())
            .tags(BingoTags.NEVER, BingoTags.STAT)
            .name(Component.translatable("bingo.goal.pacifist"))
            .tooltip(Component.translatable("bingo.goal.pacifist.tooltip"))
            .icon(Items.DIAMOND_SWORD)
            .catalyst("pacifist"));
        // TODO: finish by scaffolding tower then removing it
        // TODO: feed panda cake
        // TODO: breed pandas
        // TODO: disarm pillager
        // TODO: stun ravager
        // TODO: hero of the village
        // TODO: gail ocelot's trust
        goalAdder.accept(obtainItemGoal(id("ender_eye"), Items.ENDER_EYE, 12, 12)
            .reactant("pacifist")
            .tags(BingoTags.NETHER, BingoTags.COMBAT)
            .tooltip(Component.translatable("bingo.goal.ender_eye.hard.tooltip")));
        goalAdder.accept(obtainItemGoal(id("netherite_ingot"), Items.NETHERITE_INGOT)
            .tags(BingoTags.NETHER));
        goalAdder.accept(obtainItemGoal(id("wither_skeleton_skull"), Items.WITHER_SKELETON_SKULL)
            .reactant("pacifist")
            .tags(BingoTags.NETHER, BingoTags.COMBAT, BingoTags.RARE_BIOME));
        goalAdder.accept(obtainItemGoal(id("gilded_blackstone"), Items.GILDED_BLACKSTONE)
            .tags(BingoTags.NETHER, BingoTags.RARE_BIOME));
        // TODO: make compass point to lodestone
        // TODO: give piglin brute enchanted axe
        goalAdder.accept(BingoGoal.builder(id("6x6scaffolding"))
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
            .icon(Items.SCAFFOLDING));
        goalAdder.accept(obtainItemGoal(id("honey_block"), Items.HONEY_BLOCK, 2, 5)
            .setAntisynergy("honey")
            .infrequency(2)
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD));
        goalAdder.accept(obtainItemGoal(id("honeycomb_block"), Items.HONEYCOMB_BLOCK, 6, 15)
            .setAntisynergy("honeycomb")
            .infrequency(2)
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD));
        // TODO: kill a wandering trader
        // TODO: cure a zombie villager
        // TODO: throw mending book into lava
        // TODO: never smelt with furnaces
        // TODO: throw huge nether fungus in overworld
        // TODO: 32-64 dirt, netherrack and end stone
        goalAdder.accept(BingoGoal.builder(id("tame_mule"))
            .criterion("obtain", TameAnimalTrigger.TriggerInstance.tamedAnimal(EntityPredicate.Builder.entity().of(EntityType.MULE).build()))
            .name(Component.translatable("bingo.goal.tame_mule"))
            .icon(Items.MULE_SPAWN_EGG)
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD));
        goalAdder.accept(BingoGoal.builder(id("carrot_stick_to_rod"))
            .criterion("break", ItemBrokenTrigger.TriggerInstance.itemBroken(ItemPredicate.Builder.item().of(Items.CARROT_ON_A_STICK)))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name(Component.translatable(
                "bingo.goal.carrot_stick_to_rod",
                Items.CARROT_ON_A_STICK.getDescription(),
                Items.FISHING_ROD.getDescription()
            ))
            .icon(Items.CARROT_ON_A_STICK));
        goalAdder.accept(obtainItemGoal(id("skull_banner_pattern"), Items.SKULL_BANNER_PATTERN)
            .tags(BingoTags.NETHER, BingoTags.COMBAT, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable(
                "bingo.goal.skull_banner_pattern",
                Component.translatable("item.minecraft.skull_banner_pattern.desc"),
                Component.translatable("item.minecraft.skull_banner_pattern")
            ))
            .icon(makeBannerWithPattern(Items.WHITE_BANNER, BannerPatterns.SKULL, DyeColor.BLACK)));
        goalAdder.accept(obtainItemGoal(id("turtle_helmet"), Items.TURTLE_HELMET)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
    }

    private void addVeryHardGoals(Consumer<BingoGoal.Builder> goalAdder) {
        goalAdder.accept(obtainSomeItemsFromTag(id("ores"), Items.DIAMOND_ORE, BingoItemTags.ORES, "bingo.goal.ores", 5, 7)
            .tooltip(Component.translatable("bingo.goal.ores.tooltip"))
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(BingoGoal.builder(id("different_potions"))
            .sub("count", BingoSub.random(12, 15))
            .criterion("potions",
                DifferentPotionsTrigger.TriggerInstance.differentPotions(0),
                subber -> subber.sub("conditions.min_count", "count")
            )
            .tags(BingoTags.ITEM, BingoTags.NETHER, BingoTags.COMBAT, BingoTags.OVERWORLD)
            .reactant("pacifist")
            .name(
                Component.translatable("bingo.goal.different_potions", 0),
                subber -> subber.sub("with.0", "count")
            )
            .tooltip(Component.translatable("bingo.goal.different_potions.tooltip"))
            .icon(
                PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.MUNDANE),
                subber -> subber.sub("count", "count")
            ));
        goalAdder.accept(BingoGoal.builder(id("all_chestplates"))
            .criterion("obtain", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.LEATHER_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE,
                Items.IRON_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE))
            .tags(BingoTags.ITEM, BingoTags.COMBAT)
            .name(Component.translatable("bingo.goal.all_chestplates"))
            .tooltip(Component.translatable("bingo.goal.all_chestplates.tooltip"))
            .icon(Items.NETHERITE_CHESTPLATE));
        goalAdder.accept(obtainItemGoal(
                id("any_head"),
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
            .tooltip(Component.translatable("bingo.goal.any_head.tooltip")));

        goalAdder.accept(BingoGoal.builder(id("all_dyes"))
            .criterion("obtain", InventoryChangeTrigger.TriggerInstance.hasItems(
                Arrays.stream(DyeColor.values()).map(DyeItem::byColor).toArray(ItemLike[]::new)))
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.all_dyes"))
            .tooltip(Component.translatable("bingo.sixteen_bang",
                Arrays.stream(DyeColor.values()).map(color -> Component.translatable("color.minecraft." + color.getName())).toArray(Object[]::new)))
            .icon(new ItemStack(Items.RED_DYE, 16))
            .antisynergy("every_color")
            .reactant("use_furnace"));
        goalAdder.accept(BingoGoal.builder(id("levels"))
            .criterion("obtain", ExperienceChangeTrigger.builder().levels(MinMaxBounds.Ints.atLeast(50)).build())
            .tags(BingoTags.STAT)
            .name(Component.translatable("bingo.goal.levels", 50))
            .icon(new ItemStack(Items.EXPERIENCE_BOTTLE, 50))
            .infrequency(2)
            .antisynergy("levels"));
        goalAdder.accept(obtainItemGoal(id("tipped_arrow"), Items.TIPPED_ARROW, 16, 32)
            .tags(BingoTags.NETHER, BingoTags.OVERWORLD)
            .icon(PotionUtils.setPotion(new ItemStack(Items.TIPPED_ARROW), Potions.MUNDANE), subber -> subber.sub("count", "count")));
        goalAdder.accept(mineralPillarGoal(id("all_mineral_blocks"), BingoBlockTags.ALL_MINERAL_BLOCKS)
            .name(Component.translatable("bingo.goal.all_mineral_blocks"))
            .tooltip(Component.translatable("bingo.goal.all_mineral_blocks.tooltip"))
            .tags(BingoTags.OVERWORLD, BingoTags.NETHER)
            .icon(Blocks.NETHERITE_BLOCK)
        );
        goalAdder.accept(BingoGoal.builder(id("sleep_in_mansion"))
            .criterion("sleep", new PlayerTrigger.TriggerInstance(
                CriteriaTriggers.SLEPT_IN_BED.getId(),
                EntityPredicate.wrap(EntityPredicate.Builder.entity().located(LocationPredicate.inStructure(BuiltinStructures.WOODLAND_MANSION)).build())))
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.sleep_in_mansion"))
            .icon(Items.BROWN_BED));
        goalAdder.accept(obtainItemGoal(id("mycelium"), Items.MYCELIUM, 10, 32)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD));
        goalAdder.accept(BingoGoal.builder(id("coral_blocks"))
            .criterion("obtain", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.TUBE_CORAL_BLOCK, Items.BRAIN_CORAL_BLOCK, Items.BUBBLE_CORAL_BLOCK,
                Items.FIRE_CORAL_BLOCK, Items.HORN_CORAL_BLOCK
            ))
            .tags(BingoTags.ITEM, BingoTags.RARE_BIOME, BingoTags.OCEAN, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.coral_blocks"))
            .icon(Blocks.BRAIN_CORAL_BLOCK));
        goalAdder.accept(obtainItemGoal(id("blue_ice"), Items.BLUE_ICE, 32, 64)
            .tags(BingoTags.OVERWORLD));
        goalAdder.accept(BingoGoal.builder(id("full_power_conduit"))
            .criterion("power", PowerConduitTrigger.TriggerInstance.powerConduit(MinMaxBounds.Ints.exactly(6)))
            .tags(BingoTags.BUILD, BingoTags.OCEAN, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.full_power_conduit"))
            .icon(Blocks.CONDUIT));
        goalAdder.accept(BingoGoal.builder(id("all_diamond_craftables"))
            .criterion("obtain", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.DIAMOND_BLOCK, Items.DIAMOND_AXE, Items.DIAMOND_BOOTS,
                Items.DIAMOND_CHESTPLATE, Items.DIAMOND_HELMET, Items.DIAMOND_HOE,
                Items.DIAMOND_LEGGINGS, Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL,
                Items.DIAMOND_SWORD, Items.ENCHANTING_TABLE, Items.FIREWORK_STAR, Items.JUKEBOX))
            .name(Component.translatable("bingo.goal.all_diamond_craftables"))
            .tooltip(Component.translatable("bingo.goal.all_diamond_craftables.tooltip"))
            .icon(Items.DIAMOND_HOE)
            .antisynergy("diamond_items"));
        goalAdder.accept(BingoGoal.builder(id("shulker_in_overworld"))
            .criterion("kill", KilledTrigger.TriggerInstance.playerKilledEntity(
                EntityPredicate.Builder.entity().of(EntityType.SHULKER).located(LocationPredicate.inDimension(Level.OVERWORLD))))
            .tags(BingoTags.ACTION, BingoTags.COMBAT, BingoTags.END, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.shulker_in_overworld"))
            .icon(Items.SHULKER_SHELL)
            .reactant("pacifist"));
        goalAdder.accept(obtainItemGoal(id("diamond_block"), Items.DIAMOND_BLOCK, 5, 10)
            .infrequency(2));
        goalAdder.accept(BingoGoal.builder(id("complete_full_size_end_map"))
            .criterion("complete", CompleteMapTrigger.TriggerInstance.completeMap(
                MinMaxBounds.Ints.atLeast(MapItemSavedData.MAX_SCALE),
                LocationPredicate.inDimension(Level.END)
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD, BingoTags.END)
            .name(Component.translatable("bingo.goal.complete_full_size_end_map"))
            .icon(Items.FILLED_MAP)
            .antisynergy("complete_map"));
        goalAdder.accept(obtainItemGoal(id("wither_rose"), Items.WITHER_ROSE, 32, 64)
            .reactant("pacifist")
            .tags(BingoTags.NETHER, BingoTags.COMBAT));
        goalAdder.accept(BingoGoal.builder(id("panda_slime_ball"))
            // Currently untested. They have a 1/175,000 or a 1/2,100,000 chance to drop one on a tick.
            .criterion("pickup", ItemPickedUpTrigger.TriggerInstance.pickedUpFrom(
                ItemPredicate.Builder.item().of(Items.SLIME_BALL).build(),
                EntityPredicate.Builder.entity().of(EntityType.PANDA).build()
            ))
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD, BingoTags.RARE_BIOME)
            .name(Component.translatable("bingo.goal.panda_slime_ball"))
            .icon(Items.SLIME_BALL));
        goalAdder.accept(obtainItemGoal(id("netherite_block"), Items.NETHERITE_BLOCK, 2, 2)
            .tags(BingoTags.NETHER));
        goalAdder.accept(BingoGoal.builder(id("full_netherite_armor_and_tools"))
            .criterion("obtain", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.NETHERITE_BOOTS, Items.NETHERITE_LEGGINGS, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_HELMET,
                Items.NETHERITE_SWORD, Items.NETHERITE_SHOVEL, Items.NETHERITE_PICKAXE, Items.NETHERITE_AXE, Items.NETHERITE_HOE
            ))
            .tags(BingoTags.ITEM, BingoTags.NETHER)
            .name(Component.translatable("bingo.goal.full_netherite_armor_and_tools"))
            .icon(Items.NETHERITE_HOE));
        goalAdder.accept(BingoGoal.builder(id("zombify_pig"))
            .criterion("channel", ZombifyPigTrigger.zombifyPig()
                .direct(true)
                .build()
            )
            .criterion("nearby", ZombifyPigTrigger.zombifyPig()
                .pig(EntityPredicate.Builder.entity()
                    .distance(DistancePredicate.absolute(MinMaxBounds.Doubles.atMost(16)))
                    .build()
                )
                .direct(false)
                .build()
            )
            .requirements(List.of("channel", "nearby"))
            .name(Component.translatable("bingo.goal.zombify_pig"))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .icon(Items.COOKED_PORKCHOP));
        goalAdder.accept(obtainItemGoal(id("trident"), Items.TRIDENT)
            .tags(BingoTags.OCEAN, BingoTags.COMBAT, BingoTags.OVERWORLD));
        goalAdder.accept(BingoGoal.builder(id("tame_skeleton_horse"))
            .criterion("obtain", TameAnimalTrigger.TriggerInstance.tamedAnimal(EntityPredicate.Builder.entity().of(EntityType.SKELETON_HORSE).build()))
            .name(Component.translatable("bingo.goal.tame_skeleton_horse"))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .icon(Items.SKELETON_HORSE_SPAWN_EGG));
        goalAdder.accept(BingoGoal.builder(id("party_parrots"))
            .criterion("party", PartyParrotsTrigger.TriggerInstance.partyParrots())
            .name(Component.translatable("bingo.goal.party_parrots"))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD, BingoTags.RARE_BIOME)
            .icon(Items.MUSIC_DISC_CAT));
        goalAdder.accept(bedRowGoal(id("bed_row"), 16, 16)
            .reactant("use_furnace")
            .antisynergy("every_color")
            .infrequency(2)
            .tags(BingoTags.ACTION)
            .tooltip(Component.translatable("bingo.sixteen_bang",
                Arrays.stream(DyeColor.values()).map(color -> Component.translatable("color.minecraft." + color.getName())).toArray(Object[]::new))));
        goalAdder.accept(BingoGoal.builder(id("kill_enderman_with_endermites"))
            .criterion("obtain", EntityDieNearPlayerTrigger.builder()
                .entity(ContextAwarePredicate.create(
                    LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().of(EntityType.ENDERMAN)).build(),
                    new EndermanHasOnlyBeenDamagedByEndermiteCondition()))
                .killingBlow(DamagePredicate.Builder.damageInstance().sourceEntity(EntityPredicate.Builder.entity().of(EntityType.ENDERMITE).build()).build())
                .build())
            .name(Component.translatable("bingo.goal.kill_enderman_with_endermites"))
            .tooltip(Component.translatable("bingo.goal.kill_enderman_with_endermites.tooltip"))
            .icon(Items.ENDERMITE_SPAWN_EGG)
            .tags(BingoTags.ACTION, BingoTags.COMBAT, BingoTags.END));
        goalAdder.accept(BingoGoal.builder(id("beacon_regen"))
            .criterion("effect", BeaconEffectTrigger.TriggerInstance.effectApplied(MobEffects.REGENERATION))
            .tags(BingoTags.ITEM, BingoTags.NETHER, BingoTags.OVERWORLD, BingoTags.COMBAT)
            .name(Component.translatable("bingo.goal.beacon_regen"))
            .icon(Blocks.BEACON)
            .reactant("pacifist"));
    }

    private ResourceLocation id(String id) {
        return new ResourceLocation(Bingo.MOD_ID, prefix + id);
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
                        BingoSub.CompoundBingoSub.ElementType.INT,
                        BingoSub.CompoundBingoSub.Operator.MUL,
                        new BingoSub.SubBingoSub("distance"),
                        BingoSub.literal(100)
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

    private static ItemStack makeBannerWithPattern(Item base, ResourceKey<BannerPattern> pattern, DyeColor color) {
        final ItemStack result = new ItemStack(base);
        final CompoundTag compound = new CompoundTag();
        compound.put("Patterns", new BannerPattern.Builder().addPattern(pattern, color).toListTag());
        BlockItem.setBlockEntityData(result, BlockEntityType.BANNER, compound);
        return result;
    }
}
