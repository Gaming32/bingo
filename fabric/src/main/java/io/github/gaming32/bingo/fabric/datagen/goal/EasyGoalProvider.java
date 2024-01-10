package io.github.gaming32.bingo.fabric.datagen.goal;

import io.github.gaming32.bingo.conditions.*;
import io.github.gaming32.bingo.data.BingoDifficulties;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoTags;
import io.github.gaming32.bingo.data.icons.*;
import io.github.gaming32.bingo.data.progresstrackers.AchievedRequirementsProgressTracker;
import io.github.gaming32.bingo.data.subs.BingoSub;
import io.github.gaming32.bingo.data.tags.BingoEntityTypeTags;
import io.github.gaming32.bingo.data.tags.BingoFeatureTags;
import io.github.gaming32.bingo.data.tags.BingoItemTags;
import io.github.gaming32.bingo.data.tags.BingoPaintingVariantTags;
import io.github.gaming32.bingo.triggers.*;
import io.github.gaming32.bingo.util.BingoUtil;
import io.github.gaming32.bingo.util.BlockPattern;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class EasyGoalProvider extends DifficultyGoalProvider {
    public EasyGoalProvider(Consumer<BingoGoal.Holder> goalAdder) {
        super(BingoDifficulties.EASY, goalAdder);
    }

    @Override
    public void addGoals() {
        addGoal(BingoGoal.builder(id("different_fish"))
            .sub("count", BingoSub.random(2, 4))
            .criterion("obtain",
                HasSomeFoodItemsTrigger.builder().requiredCount(1).tag(ItemTags.FISHES).build(),
                subber -> subber.sub("conditions.required_count", "count"))
            .progress("obtain")
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.different_fish", 0), subber -> subber.sub("with.0", "count"))
            .tooltip("different_fish")
            .icon(new ItemTagCycleIcon(ItemTags.FISHES), subber -> subber.sub("count", "count")));
        addGoal(BingoGoal.builder(id("grow_tree_in_nether"))
            .criterion("grow", GrowFeatureTrigger.builder()
                .feature(BingoFeatureTags.TREES)
                .location(LocationPredicate.Builder.inDimension(Level.NETHER).build())
                .build()
            )
            .tags(BingoTags.ACTION, BingoTags.NETHER, BingoTags.OVERWORLD)
            .name("grow_tree_in_nether")
            .tooltip("grow_tree_in_nether")
            .icon(CycleIcon.infer(Items.BONE_MEAL, Items.OAK_SAPLING)));
        addGoal(obtainSomeItemsFromTag(id("terracotta"), ItemTags.TERRACOTTA, "bingo.goal.colors_of_terracotta", 4, 7)
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD)
            .reactant("use_furnace")
            .antisynergy("terracotta_color")
            .infrequency(4)
        );
        addGoal(obtainItemGoal(id("mushroom_stew"), Items.MUSHROOM_STEW, 2, 5));
        addGoal(BingoGoal.builder(id("shoot_button"))
            .criterion("obtain", ArrowPressTrigger.builder()
                .arrow(EntityPredicate.Builder.entity().of(EntityTypeTags.ARROWS).build())
                .buttonOrPlate(BlockPredicate.Builder.block().of(BlockTags.BUTTONS).build())
                .build()
            )
            .tags(BingoTags.ACTION)
            .name("shoot_button")
            .icon(Items.OAK_BUTTON));
        addGoal(obtainItemGoal(id("writable_book"), Items.WRITABLE_BOOK)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("flint"), Items.FLINT, 16, 64));
        addGoal(eatEntireCake());
        addGoal(obtainItemGoal(id("pumpkin_pie"), Items.PUMPKIN_PIE)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("fish_treasure_junk"))
            .criterion("treasure", FishingRodHookedTrigger.TriggerInstance.fishedItem(
                Optional.empty(), Optional.empty(),
                Optional.of(ItemPredicate.Builder.item().of(BingoItemTags.FISHING_TREASURE).build())
            ))
            .criterion("junk", FishingRodHookedTrigger.TriggerInstance.fishedItem(
                Optional.empty(), Optional.empty(),
                Optional.of(ItemPredicate.Builder.item().of(BingoItemTags.FISHING_JUNK).build())
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name("fish_treasure_junk").tooltip("fish_treasure_junk")
            .icon(makeItemWithGlint(Items.FISHING_ROD))
            .reactant("fishing")
        );
        addGoal(obtainItemGoal(id("coarse_dirt"), Items.COARSE_DIRT, 16, 64)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("clock"), Items.CLOCK, 2, 3)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("iron_block"), Items.IRON_BLOCK, 2, 4)
            .infrequency(2));
        addGoal(obtainItemGoal(id("gold_block"), Items.GOLD_BLOCK)
            .infrequency(2));
        addGoal(obtainItemGoal(id("golden_apple"), Items.GOLDEN_APPLE));
        addGoal(obtainItemGoal(id("bookshelf"), Items.BOOKSHELF, 2, 4)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("never_wear_chestplates"))
            .criterion("equip", EquipItemTrigger.builder()
                .newItem(ItemPredicate.Builder.item().of(BingoItemTags.ARMOR_CHESTPLATES).build())
                .slots(EquipmentSlot.CHEST)
                .build()
            )
            .tags(BingoTags.NEVER)
            .name("never_wear_chestplates")
            .icon(Items.IRON_CHESTPLATE)
            .antisynergy("never_wear_armor")
            .catalyst("wear_armor"));
        addGoal(BingoGoal.builder(id("never_use_shields"))
            .criterion("use", CriteriaTriggers.USING_ITEM.createCriterion(
                new UsingItemTrigger.TriggerInstance(
                    Optional.empty(),
                    Optional.of(ItemPredicate.Builder.item().of(BingoItemTags.SHIELDS).build())
                )
            ))
            .tags(BingoTags.NEVER)
            .name("never_use_shields")
            .tooltip("never_use_shields")
            .icon(Items.SHIELD));
        addGoal(obtainItemGoal(id("jukebox"), Items.JUKEBOX));
        addGoal(BingoGoal.builder(id("3x3x3_glass_cube"))
            .criterion("build", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(
                LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.GLASS)
                    .or(LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.LAVA)),
                BlockPatternCondition.builder()
                    .aisle("###", "###", "###")
                    .aisle("###", "#+#", "###")
                    .aisle("###", "###", "###")
                    .where('#', BlockPredicate.Builder.block().of(Blocks.GLASS))
                    .where('+', BlockPredicate.Builder.block().of(Blocks.LAVA))
                    .rotations(BlockPattern.Rotations.NONE)
            ))
            .name("3x3x3_glass_cube")
            .icon(new CycleIcon(
                new ItemIcon(new ItemStack(Items.GLASS, 26)),
                ItemIcon.ofItem(Items.LAVA_BUCKET)
            ))
            .tags(BingoTags.BUILD, BingoTags.OVERWORLD)
        );
        addGoal(obtainItemGoal(id("mossy_cobblestone"), Items.MOSSY_COBBLESTONE, 16, 32)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("cactus"), Items.CACTUS, 5, 15)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("tnt"), Items.TNT, 2, 3)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainLevelsGoal(id("levels"), 8, 15));
        addGoal(BingoGoal.builder(id("create_snow_golem"))
            .criterion("summon", SummonedEntityTrigger.TriggerInstance.summonedEntity(
                EntityPredicate.Builder.entity().of(EntityType.SNOW_GOLEM)
            ))
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.create_snow_golem", EntityType.SNOW_GOLEM.getDescription()))
            .icon(EntityIcon.ofSpawnEgg(EntityType.SNOW_GOLEM))
        );
        addGoal(obtainItemGoal(id("note_block"), Items.NOTE_BLOCK, 5, 15)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("ink_sac"), Items.INK_SAC, 5, 15)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("bread"), Items.BREAD, 6, 10)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("hay_block"), Items.HAY_BLOCK, 2, 3)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainSomeItemsFromTag(id("wool_colors"), ItemTags.WOOL, "bingo.goal.wool_colors", 5, 8)
            .antisynergy("wool_color")
            .infrequency(4)
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("piston"), Items.PISTON)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("full_iron_armor"))
            .criterion("armor", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS
            ))
            .tags(BingoTags.ITEM)
            .infrequency(3)
            .reactant("wear_armor")
            .name("full_iron_armor")
            .icon(new CycleIcon(
                ItemIcon.ofItem(Items.IRON_HELMET),
                ItemIcon.ofItem(Items.IRON_CHESTPLATE),
                ItemIcon.ofItem(Items.IRON_LEGGINGS),
                ItemIcon.ofItem(Items.IRON_BOOTS)
            ))
        );
        addGoal(BingoGoal.builder(id("full_leather_armor"))
            .criterion("armor", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS
            ))
            .tags(BingoTags.ITEM)
            .infrequency(3)
            .name("full_leather_armor")
            .icon(new CycleIcon(
                ItemIcon.ofItem(Items.LEATHER_HELMET),
                ItemIcon.ofItem(Items.LEATHER_CHESTPLATE),
                ItemIcon.ofItem(Items.LEATHER_LEGGINGS),
                ItemIcon.ofItem(Items.LEATHER_BOOTS)
            ))
        );
        addGoal(BingoGoal.builder(id("fill_water_cauldron"))
            .criterion("fill", CriteriaTriggers.ITEM_USED_ON_BLOCK.createCriterion(
                new ItemUsedOnLocationTrigger.TriggerInstance(
                    Optional.empty(),
                    Optional.of(ContextAwarePredicate.create(
                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.WATER_CAULDRON)
                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                .hasProperty(LayeredCauldronBlock.LEVEL, 3)
                            )
                            .build()
                    ))
                )
            ))
            .name(Component.translatable(
                "bingo.goal.fill_water_cauldron",
                Blocks.CAULDRON.getName(), Blocks.WATER.getName()
            ))
            .icon(BlockIcon.ofBlock(Blocks.WATER_CAULDRON.defaultBlockState()
                .setValue(LayeredCauldronBlock.LEVEL, 3)
            ))
            .tags(BingoTags.OVERWORLD)
        );
        addGoal(BingoGoal.builder(id("complete_map"))
            .criterion("complete", CompleteMapTrigger.TriggerInstance.completeMap())
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name("complete_map")
            .icon(Items.FILLED_MAP)
            .antisynergy("complete_map"));
        addGoal(obtainItemGoal(id("soul_sand"), Items.SOUL_SAND, 5, 10)
            .tags(BingoTags.NETHER));
        addGoal(obtainItemGoal(id("soul_soil"), Items.SOUL_SOIL, 5, 10)
            .tags(BingoTags.NETHER));
        addGoal(obtainItemGoal(id("pumpkin"), Items.PUMPKIN, 5, 10)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("carved_pumpkin"), Items.CARVED_PUMPKIN, 2, 5)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("vine"), Items.VINE, 10, 30)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME));
        addGoal(obtainSomeItemsFromTag(id("different_slabs"), BingoItemTags.SLABS, "bingo.goal.different_slabs", 5, 7)
            .antisynergy("slabs")
            .infrequency(2));
        addGoal(BingoGoal.builder(id("almost_every_sword"))
            .criterion("wood", InventoryChangeTrigger.TriggerInstance.hasItems(Items.WOODEN_SWORD))
            .criterion("stone", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STONE_SWORD))
            .criterion("gold", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GOLDEN_SWORD))
            .criterion("iron", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_SWORD))
            .criterion("diamond", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND_SWORD))
            .progress(AchievedRequirementsProgressTracker.INSTANCE)
            .name("almost_every_sword")
            .icon(CycleIcon.infer(Items.WOODEN_SWORD, Items.STONE_SWORD, Items.GOLDEN_SWORD, Items.IRON_SWORD, Items.DIAMOND_SWORD))
            .antisynergy("all_swords")
            .infrequency(2)
            .tags(BingoTags.ITEM));
        addGoal(BingoGoal.builder(id("almost_every_pickaxe"))
            .criterion("wood", InventoryChangeTrigger.TriggerInstance.hasItems(Items.WOODEN_PICKAXE))
            .criterion("stone", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STONE_PICKAXE))
            .criterion("gold", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GOLDEN_PICKAXE))
            .criterion("iron", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_PICKAXE))
            .criterion("diamond", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND_PICKAXE))
            .progress(AchievedRequirementsProgressTracker.INSTANCE)
            .name("almost_every_pickaxe")
            .icon(CycleIcon.infer(Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.GOLDEN_PICKAXE, Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE))
            .antisynergy("all_pickaxes")
            .infrequency(2)
            .tags(BingoTags.ITEM));
        addGoal(obtainItemGoal(id("bricks"), Items.BRICKS, 16, 64)
            .reactant("use_furnace")
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("nether_bricks"), Items.NETHER_BRICKS, 16, 32)
            .reactant("use_furnace")
            .tags(BingoTags.NETHER));
        addGoal(obtainItemGoal(id("arrow"), Items.ARROW, 16, 64));
        addGoal(BingoGoal.builder(id("sleep_in_nether"))
            .criterion("sleep", IntentionalGameDesignTrigger.TriggerInstance.clicked(
                LocationPredicate.Builder.inDimension(Level.NETHER).build()
            ))
            .tags(BingoTags.ACTION, BingoTags.NETHER, BingoTags.OVERWORLD)
            .name("sleep_in_nether")
            .icon(Items.PURPLE_BED)
        );
        addGoal(obtainItemGoal(id("fermented_spider_eye"), Items.FERMENTED_SPIDER_EYE)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainSomeItemsFromTag(id("different_stairs"), BingoItemTags.STAIRS, "bingo.goal.different_stairs", 5, 7)
            .antisynergy("stairs")
            .infrequency(2));
        addGoal(obtainItemGoal(id("ender_pearl"), Items.ENDER_PEARL, 2, 3)
            .infrequency(2));
        addGoal(obtainItemGoal(id("egg"), Items.EGG, 16, 16));
        addGoal(BingoGoal.builder(id("4x4_paintings"))
            .criterion("paintings", AdjacentPaintingTrigger.builder()
                .paintingVariant(BingoPaintingVariantTags.SIZE_4X4)
                .count(MinMaxBounds.Ints.atLeast(3))
                .build())
            .name("4x4_paintings")
            .icon(Items.PAINTING)
            .antisynergy("painting")
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("bone_block"), Items.BONE_BLOCK, 5, 10));
        addGoal(BingoGoal.builder(id("double_creeper_boat"))
            .criterion("break", BingoTriggers.DESTROY_VEHICLE.get().createCriterion(new DestroyVehicleTrigger.TriggerInstance(
                Optional.empty(),
                Optional.of(ContextAwarePredicate.create(
                    LootItemEntityPropertyCondition.hasProperties(
                        LootContext.EntityTarget.THIS,
                        EntityPredicate.Builder.entity()
                            .of(EntityType.BOAT)
                            .build()
                    ).build(),
                    new PassengersCondition(List.of(
                        EntityPredicate.wrap(EntityPredicate.Builder.entity().of(EntityType.CREEPER)),
                        EntityPredicate.wrap(EntityPredicate.Builder.entity().of(EntityType.CREEPER))
                    ), false)
                )),
                Optional.empty()
            )))
            .name("double_creeper_boat")
            .icon(new CycleIcon(
                EntityIcon.of(EntityType.BOAT, new ItemStack(Items.OAK_BOAT)),
                EntityIcon.ofSpawnEgg(EntityType.CREEPER, 2)
            ))
            .tags(BingoTags.ACTION, BingoTags.COMBAT, BingoTags.OVERWORLD)
        );
        addGoal(BingoGoal.builder(id("villager_trade"))
            .criterion("obtain", TradeTrigger.TriggerInstance.tradedWithVillager())
            .name("villager_trade")
            .icon(EntityIcon.ofSpawnEgg(EntityType.VILLAGER))
            .tags(BingoTags.VILLAGE, BingoTags.ACTION, BingoTags.OVERWORLD));
        // TODO: different colored shields
        addGoal(obtainItemGoal(id("dead_bush"), Items.DEAD_BUSH)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME));
        addGoal(obtainItemGoal(id("grass"), Items.SHORT_GRASS, 15, 32) // FIXME: Support TALL_GRASS too
            .tooltip("grass"));

        for (String dyeColor : List.of("cyan", "magenta", "red", "orange", "yellow", "green", "pink", "purple", "lime")) {
            Item dyeItem = BuiltInRegistries.ITEM.get(new ResourceLocation(dyeColor + "_dye"));
            addGoal(obtainItemGoal(id(dyeColor + "_dye"), dyeItem)
                .infrequency(10)
                .tags(BingoTags.OVERWORLD));
        }

        addGoal(BingoGoal.builder(id("never_sleep"))
            .criterion("sleep", PlayerTrigger.TriggerInstance.sleptInBed())
            .tags(BingoTags.NEVER, BingoTags.OVERWORLD)
            .name("never_sleep")
            .icon(Items.RED_BED)
            .catalyst("sleep"));
        addGoal(BingoGoal.builder(id("grow_huge_mushroom"))
            .criterion("grow", GrowFeatureTrigger.builder().feature(BingoFeatureTags.HUGE_MUSHROOMS).build())
            .tags(BingoTags.ACTION)
            .name("grow_huge_mushroom")
            .icon(Blocks.RED_MUSHROOM_BLOCK));
        addGoal(BingoGoal.builder(id("water_lava_milk"))
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
            .icon(new CycleIcon(
                ItemIcon.ofItem(Items.WATER_BUCKET),
                ItemIcon.ofItem(Items.LAVA_BUCKET),
                ItemIcon.ofItem(Items.MILK_BUCKET)
            ))
            .antisynergy("bucket_types", "water_bucket", "lava_bucket", "milk_bucket")
            .reactant("use_buckets"));
        addGoal(obtainSomeItemsFromTag(id("different_flowers"), ItemTags.FLOWERS, "bingo.goal.different_flowers", 5, 7)
            .antisynergy("flowers")
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainSomeItemsFromTag(id("concrete"), BingoItemTags.CONCRETE, "bingo.goal.concrete", 3, 6)
            .antisynergy("concrete_color")
            .infrequency(4)
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD));
        addGoal(obtainSomeItemsFromTag(id("glazed_terracotta"), BingoItemTags.GLAZED_TERRACOTTA, "bingo.goal.glazed_terracotta", 3, 6)
            .reactant("use_furnace")
            .antisynergy("glazed_terracotta_color")
            .infrequency(4)
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD));
        addGoal(bedRowGoal(id("bed_row"), 3, 6));
        addGoal(BingoGoal.builder(id("finish_at_spawn"))
            .criterion("nearby", CriteriaTriggers.LOCATION.createCriterion(
                new PlayerTrigger.TriggerInstance(
                    Optional.of(ContextAwarePredicate.create(new DistanceFromSpawnCondition(
                        Optional.of(DistancePredicate.horizontal(MinMaxBounds.Doubles.atMost(3)))
                    )))
                )
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD, BingoTags.FINISH)
            .name(Component.translatable("bingo.goal.finish_at_spawn", Items.COMPASS.getDescription()))
            .tooltip("finish_at_spawn")
            .icon(Items.COMPASS)
        );
        addGoal(obtainItemGoal(id("stone"), Items.STONE, 32, 64)
            .tooltip("stone")
            .infrequency(2)
            .reactant("use_furnace")
            .tags(BingoTags.OVERWORLD));
        // TODO: kill passive mobs with only fire
        // TODO: kill creeper with only fire
        addGoal(obtainItemGoal(id("iron_nugget"), Items.IRON_NUGGET, 32, 64));
        addGoal(obtainItemGoal(id("gold_nugget"), Items.GOLD_NUGGET, 32, 64));
        addGoal(obtainItemGoal(id("rotten_flesh"), Items.ROTTEN_FLESH, 16, 32)
            .infrequency(2));
        addGoal(obtainItemGoal(id("redstone"), Items.REDSTONE, 16, 64)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("golden_carrot"), Items.GOLDEN_CARROT)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("common_mob_drops"))
            .criterion("rotten_flesh", InventoryChangeTrigger.TriggerInstance.hasItems(Items.ROTTEN_FLESH))
            .criterion("spider_eye", InventoryChangeTrigger.TriggerInstance.hasItems(Items.SPIDER_EYE))
            .criterion("bone", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BONE))
            .criterion("gunpowder", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GUNPOWDER))
            .criterion("ender_pearl", InventoryChangeTrigger.TriggerInstance.hasItems(Items.ENDER_PEARL))
            .progress(AchievedRequirementsProgressTracker.INSTANCE)
            .name(Component.translatable("bingo.goal.common_mob_drops",
                Items.ROTTEN_FLESH.getDescription(),
                Items.SPIDER_EYE.getDescription(),
                Items.BONE.getDescription(),
                Items.GUNPOWDER.getDescription(),
                Items.ENDER_PEARL.getDescription()))
            .icon(CycleIcon.infer(Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.BONE, Items.GUNPOWDER, Items.ENDER_PEARL))
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("feather"), Items.FEATHER, 32, 64)
            .infrequency(2));
        addGoal(obtainItemGoal(id("lily_pad"), Items.LILY_PAD, 2, 10)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME));
        addGoal(obtainItemGoal(id("stick"), Items.STICK, 65, 128)
            .infrequency(2));
        // TODO: 4 different colors of leather armor at the same time
        addGoal(obtainItemGoal(id("seagrass"), Items.SEAGRASS, 15, 32)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));

        for (String woodType : List.of("oak", "spruce", "birch", "dark_oak", "acacia", "cherry")) {
            Item planksItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_planks"));
            addGoal(obtainItemGoal(id(woodType + "_planks"), planksItem, 65, 128)
                .tags(BingoTags.OVERWORLD)
                .infrequency(25));

            Item logItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_log"));
            addGoal(obtainItemGoal(id(woodType + "_log"), logItem, 16, 32)
                .tags(BingoTags.OVERWORLD)
                .infrequency(25));

            Item woodItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_wood"));
            addGoal(obtainItemGoal(id(woodType + "_wood"), woodItem, 11, 20)
                .infrequency(25)
                .tags(BingoTags.OVERWORLD));

            Item strippedWoodItem = BuiltInRegistries.ITEM.get(new ResourceLocation("stripped_" + woodType + "_wood"));
            addGoal(obtainItemGoal(id("stripped_" + woodType + "_wood"), strippedWoodItem, 11, 20)
                .reactant("axe_use")
                .infrequency(25)
                .tags(BingoTags.OVERWORLD));

            Item strippedLogItem = BuiltInRegistries.ITEM.get(new ResourceLocation("stripped_" + woodType + "_log"));
            addGoal(obtainItemGoal(id("stripped_" + woodType + "_log"), strippedLogItem, 16, 32)
                .reactant("axe_use")
                .infrequency(25)
                .tags(BingoTags.OVERWORLD));
        }

        addGoal(obtainItemGoal(id("tropical_fish"), Items.TROPICAL_FISH)
            .tags(BingoTags.OVERWORLD, BingoTags.OCEAN));
        addGoal(obtainItemGoal(id("pufferfish"), Items.PUFFERFISH)
            .tags(BingoTags.OVERWORLD, BingoTags.OCEAN));
        addGoal(obtainItemGoal(id("cod"), Items.COD, 6, 15)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD, BingoTags.OCEAN));
        addGoal(obtainItemGoal(id("salmon"), Items.SALMON, 4, 7)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD, BingoTags.OCEAN));
        addGoal(BingoGoal.builder(id("never_use_boat"))
            .criterion("use", CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.createCriterion(
                new PlayerInteractTrigger.TriggerInstance(
                    Optional.empty(), Optional.empty(),
                    Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity()
                        .entityType(EntityTypePredicate.of(BingoEntityTypeTags.BOATS))
                        .build()
                    ))
                )
            ))
            .tags(BingoTags.NEVER, BingoTags.OVERWORLD)
            .name("never_use_boat")
            .icon(Items.OAK_BOAT));
        addGoal(BingoGoal.builder(id("place_fish_in_nether"))
            .criterion("place", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(
                MatchTool.toolMatches(ItemPredicate.Builder.item().of(BingoItemTags.FISH_BUCKETS)),
                LocationCheck.checkLocation(LocationPredicate.Builder.location().setDimension(Level.NETHER))
            ))
            .name("place_fish_in_nether")
            .icon(new CycleIcon(
                ItemIcon.ofItem(Items.NETHERRACK),
                ItemIcon.ofItem(Items.TROPICAL_FISH_BUCKET)
            ))
            .antisynergy("pacifist")
            .tags(BingoTags.NETHER, BingoTags.ACTION, BingoTags.OVERWORLD)
        );
        addGoal(obtainItemGoal(id("dried_kelp_block"), Items.DRIED_KELP_BLOCK, 11, 20)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("drown_zombie"))
            .criterion("drown", ZombieDrownedTrigger.builder().build())
            .name("drown_zombie")
            .tooltip("drown_zombie")
            .icon(CycleIcon.infer(EntityType.ZOMBIE, EntityType.DROWNED))
            .tags(BingoTags.ACTION, BingoTags.COMBAT, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("gunpowder"), Items.GUNPOWDER, 2, 5)
            .infrequency(2));
        addGoal(obtainItemGoal(id("spider_eye"), Items.SPIDER_EYE, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainSomeEdibleItems(id("edible_items"), 4, 5).tags(BingoTags.OVERWORLD));
        // TODO: breed 2-4 sets of mobs
        addGoal(crouchDistanceGoal(id("crouch_distance"), 100, 200));
        // TODO: never use debug
        // TODO: ring bell from 10 blocks away
        // TODO: repair item with grindstone
        addGoal(obtainItemGoal(id("sweet_berries"), Items.SWEET_BERRIES, 2, 6)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME));
        addGoal(
            obtainItemGoal(
                id("banner_pattern"),
                new ItemTagCycleIcon(BingoItemTags.BANNER_PATTERNS),
                ItemPredicate.Builder.item().of(BingoItemTags.BANNER_PATTERNS)
            )
                .name("banner_pattern")
                .tags(BingoTags.OVERWORLD)
        );
        addGoal(BingoGoal.builder(id("drink_sus_stew"))
            .criterion("drink", ConsumeItemTrigger.TriggerInstance.usedItem(Items.SUSPICIOUS_STEW))
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD)
            .reactant("eat_non_meat")
            .name(Component.translatable("bingo.goal.drink_sus_stew", Items.SUSPICIOUS_STEW.getDescription()))
            .icon(Items.SUSPICIOUS_STEW));
        addGoal(BingoGoal.builder(id("give_fox_sword"))
            .criterion("pickup", CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.createCriterion(
                new PickedUpItemTrigger.TriggerInstance(
                    Optional.empty(),
                    Optional.of(ItemPredicate.Builder.item().of(ItemTags.SWORDS).build()),
                    Optional.of(ContextAwarePredicate.create(
                        LootItemEntityPropertyCondition.hasProperties(
                            LootContext.EntityTarget.THIS,
                            EntityPredicate.Builder.entity().of(EntityType.FOX)
                        ).build()
                    ))
                )
            ))
            .name(Component.translatable("bingo.goal.give_fox_sword", EntityType.FOX.getDescription()))
            .icon(new CycleIcon(
                EntityIcon.ofSpawnEgg(EntityType.FOX),
                ItemIcon.ofItem(Items.IRON_SWORD)
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD, BingoTags.RARE_BIOME)
        );
        addGoal(obtainItemGoal(id("honey_bottle"), Items.HONEY_BOTTLE)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("honeycomb"), Items.HONEYCOMB, 3, 3)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("basalt"), Items.BASALT, 2, 6)
            .tags(BingoTags.NETHER));
        addGoal(obtainItemGoal(id("blackstone"), Items.BLACKSTONE, 2, 6)
            .tags(BingoTags.NETHER));
        {
            final List<CompoundTag> slots = IntStream.range(0, 4).mapToObj(slot -> BingoUtil.compound(Map.of(
                "id", StringTag.valueOf(BuiltInRegistries.ITEM.getKey(Items.PORKCHOP).toString()),
                "Slot", ByteTag.valueOf((byte) slot)
            ))).toList();
            addGoal(BingoGoal.builder(id("porkchops_in_soul_campfire"))
                .criterion("place", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                    LocationPredicate.Builder.location().setBlock(
                        BlockPredicate.Builder.block()
                            .of(Blocks.SOUL_CAMPFIRE)
                            .hasNbt(BingoUtil.compound(Map.of(
                                "Items", BingoUtil.list(slots)
                            )))
                    ),
                    ItemPredicate.Builder.item().of(Items.PORKCHOP)
                ))
                .name(Component.translatable(
                    "bingo.goal.porkchops_in_soul_campfire",
                    Blocks.SOUL_CAMPFIRE.getName()
                ))
                .icon(new CycleIcon(
                    ItemIcon.ofItem(Items.SOUL_CAMPFIRE),
                    new ItemIcon(new ItemStack(Items.PORKCHOP, 4))
                ))
                .reactant("pacifist")
                .tags(BingoTags.ACTION, BingoTags.NETHER, BingoTags.COMBAT)
            );
        }
        addGoal(obtainItemGoal(id("soul_lantern"), Items.SOUL_LANTERN)
            .tags(BingoTags.NETHER));
        // TODO: open door with target block from 10 blocks away
        addGoal(obtainItemGoal(id("carrot_on_a_stick"), Items.CARROT_ON_A_STICK)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("barter_with_piglin"))
            .criterion("barter", CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.createCriterion(new PickedUpItemTrigger.TriggerInstance(
                Optional.empty(),
                Optional.of(ItemPredicate.Builder.item().of(PiglinAi.BARTERING_ITEM).build()),
                Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(EntityType.PIGLIN).flags(EntityFlagsPredicate.Builder.flags().setIsBaby(false)))))))
            .criterion("barter_directly", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
                ItemPredicate.Builder.item().of(PiglinAi.BARTERING_ITEM),
                Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(EntityType.PIGLIN).flags(EntityFlagsPredicate.Builder.flags().setIsBaby(false))))))
            .requirements(AdvancementRequirements.Strategy.OR)
            .name("barter_with_piglin")
            .icon(EntityIcon.ofSpawnEgg(EntityType.PIGLIN))
            .tags(BingoTags.ACTION, BingoTags.NETHER));
        addGoal(BingoGoal.builder(id("nausea"))
            .criterion("obtain", EffectsChangedTrigger.TriggerInstance.hasEffects(MobEffectsPredicate.Builder.effects().and(MobEffects.CONFUSION)))
            .name("nausea")
            .icon(EffectIcon.of(MobEffects.CONFUSION))
            .reactant("eat_meat")
            .tags(BingoTags.ITEM, BingoTags.OCEAN, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(
                id("obtain_enchanted_item"),
                Items.ENCHANTED_BOOK,
                ItemPredicate.Builder.item().hasNbt(BingoUtil.compound(Map.of("Enchantments", BingoUtil.list(List.of(new CompoundTag()))))),
                ItemPredicate.Builder.item().hasNbt(BingoUtil.compound(Map.of("StoredEnchantments", BingoUtil.list(List.of(new CompoundTag())))))
            )
            .name("obtain_enchanted_item")
            .tooltip("obtain_enchanted_item")
            .antisynergy("enchant")
        );
        // TODO: remove enchantment with grindstone
        // TODO: never use sword
        addGoal(BingoGoal.builder(id("carnivore"))
            .criterion("not_meat", ConsumeItemTrigger.TriggerInstance.usedItem(
                ItemPredicate.Builder.item()
                    .of(BingoItemTags.NOT_MEAT)
            ))
            .tags(BingoTags.NEVER, BingoTags.ACTION)
            .antisynergy("food")
            .catalyst("eat_non_meat")
            .name("carnivore")
            .tooltip("carnivore")
            .icon(new ItemTagCycleIcon(BingoItemTags.MEAT))
        );
        // TODO: clean banner
        addGoal(obtainSomeItemsFromTag(id("gold_in_name"), BingoItemTags.GOLD_IN_NAME, "bingo.goal.gold_in_name", 5, 7)
            .tooltip("gold_in_name")
            .antisynergy("gold_items"));
        addGoal(obtainItemGoal(id("sand"), Items.SAND, 33, 64)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("sandstone"), Items.SANDSTONE, 11, 32)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("cut_sandstone"), Items.CUT_SANDSTONE, 11, 32)
            .setAntisynergy("sandstone")
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("paper"), Items.PAPER, 6, 15)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("coal_block"), Items.COAL_BLOCK, 3, 6)
            .reactant("never_coal"));
        addGoal(obtainItemGoal(id("apple"), Items.APPLE, 2, 5)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("tame_horse"))
            .criterion("obtain", TameAnimalTrigger.TriggerInstance.tamedAnimal(
                EntityPredicate.Builder.entity().of(EntityType.HORSE)
            ))
            .name("tame_horse")
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .icon(EntityIcon.ofSpawnEgg(EntityType.HORSE))
        );
        addGoal(BingoGoal.builder(id("hatch_chicken"))
            .criterion("hatch", ChickenHatchTrigger.builder().build())
            .name("hatch_chicken")
            .icon(CycleIcon.infer(Items.EGG, EntityType.CHICKEN))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD));
        // TODO: empty cauldron without buckets or bottles
        addGoal(BingoGoal.builder(id("sleep_in_villager_bed"))
            .criterion("sleep", BingoTriggers.slept(
                new VillagerOwnershipCondition(PoiManager.Occupancy.IS_OCCUPIED, Optional.empty())
            ))
            .name(Component.translatable(
                "bingo.goal.sleep_in_villager_bed",
                EntityType.VILLAGER.getDescription()
            ))
            .icon(new CycleIcon(
                ItemIcon.ofItem(Items.RED_BED),
                EntityIcon.ofSpawnEgg(EntityType.VILLAGER)
            ))
            .reactant("sleep")
            .tags(BingoTags.ACTION, BingoTags.VILLAGE, BingoTags.OVERWORLD)
        );
        // TODO: set fire to villager's house
        addGoal(obtainItemGoal(id("emerald"), Items.EMERALD)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("milk_cure"))
            .criterion("cure", CriteriaTriggers.CONSUME_ITEM.createCriterion(
                new ConsumeItemTrigger.TriggerInstance(
                    Optional.of(ContextAwarePredicate.create(
                        new HasAnyEffectCondition(LootContext.EntityTarget.THIS)
                    )),
                    Optional.of(ItemPredicate.Builder.item()
                        .of(Items.MILK_BUCKET)
                        .build()
                    )
                )
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name("milk_cure")
            .icon(Items.MILK_BUCKET)
        );
        addGoal(obtainItemGoal(id("pottery_sherd"), new ItemTagCycleIcon(ItemTags.DECORATED_POT_SHERDS), ItemPredicate.Builder.item().of(ItemTags.DECORATED_POT_SHERDS))
            .tags(BingoTags.OVERWORLD)
            .name("pottery_sherd"));
        final KeyMapping walkBackwards = Minecraft.getInstance().options.keyDown;
        addGoal(BingoGoal.builder(id("never_walk_backwards"))
            .criterion("walk", KeyPressedTrigger.TriggerInstance.keyPressed(walkBackwards))
            .tags(BingoTags.ACTION, BingoTags.NEVER)
            .name("never_walk_backwards")
            .infrequency(4)
            .icon(Items.DIRT_PATH)
        );
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
            builder.criterion("level_" + level, CriteriaTriggers.ITEM_USED_ON_BLOCK.createCriterion(
                new ItemUsedOnLocationTrigger.TriggerInstance(Optional.empty(), Optional.of(location))
            ));
        }
        return builder
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name("eat_entire_cake")
            .icon(Items.CAKE)
            .reactant("eat_non_meat");
    }
}
