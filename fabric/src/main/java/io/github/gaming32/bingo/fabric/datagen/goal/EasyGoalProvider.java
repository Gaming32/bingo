package io.github.gaming32.bingo.fabric.datagen.goal;

import io.github.gaming32.bingo.conditions.DistanceFromSpawnCondition;
import io.github.gaming32.bingo.conditions.HasAnyEffectCondition;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoTags;
import io.github.gaming32.bingo.data.tags.BingoFeatureTags;
import io.github.gaming32.bingo.data.tags.BingoItemTags;
import io.github.gaming32.bingo.triggers.*;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class EasyGoalProvider extends DifficultyGoalProvider {
    public EasyGoalProvider(Consumer<BingoGoal> goalAdder) {
        super(1, "easy/", goalAdder);
    }

    @Override
    public void addGoals() {
        // TODO: different fish
        addGoal(BingoGoal.builder(id("grow_tree_in_nether"))
            .criterion("grow", GrowFeatureTrigger.builder()
                .feature(BingoFeatureTags.TREES)
                .location(LocationPredicate.Builder.inDimension(Level.NETHER).build())
                .build()
            )
            .tags(BingoTags.ACTION, BingoTags.NETHER, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.grow_tree_in_nether"))
            .tooltip(Component.translatable("bingo.goal.grow_tree_in_nether.tooltip"))
            .icon(Items.BONE_MEAL));
        addGoal(obtainSomeItemsFromTag(id("colors_of_terracotta"), Items.TERRACOTTA, ItemTags.TERRACOTTA, "bingo.goal.colors_of_terracotta", 4, 7)
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
            .name(Component.translatable("bingo.goal.shoot_button"))
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
            .name(Component.translatable("bingo.goal.fish_treasure_junk"))
            .tooltip(Component.translatable("bingo.goal.fish_treasure_junk.tooltip"))
            .icon(Items.FISHING_ROD)
            .reactant("fishing"));
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
                .build())
            .tags(BingoTags.NEVER)
            .name(Component.translatable("bingo.goal.never_wear_chestplates"))
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
            .name(Component.translatable("bingo.goal.never_use_shields"))
            .tooltip(Component.translatable("bingo.goal.never_use_shields.tooltip"))
            .icon(Items.SHIELD));
        addGoal(obtainItemGoal(id("jukebox"), Items.JUKEBOX));
        // TODO: 3x3x3 cube of glass with lava in middle
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
            .name(Component.translatable("bingo.goal.create_snow_golem"))
            .icon(Blocks.CARVED_PUMPKIN));
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
        // TODO: colors of wool
        addGoal(obtainItemGoal(id("piston"), Items.PISTON)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("full_iron_armor"))
            .criterion("armor", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS
            ))
            .tags(BingoTags.ITEM)
            .infrequency(3)
            .name(Component.translatable("bingo.goal.full_iron_armor"))
            .icon(Items.IRON_HELMET));
        addGoal(BingoGoal.builder(id("full_leather_armor"))
            .criterion("armor", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS
            ))
            .tags(BingoTags.ITEM)
            .infrequency(3)
            .name(Component.translatable("bingo.goal.full_leather_armor"))
            .icon(Items.LEATHER_HELMET));
        // TODO: fill cauldron with water
        addGoal(BingoGoal.builder(id("complete_map"))
            .criterion("complete", CompleteMapTrigger.TriggerInstance.completeMap())
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.complete_map"))
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
        // TODO: different slabs
        // TODO: every sword
        // TODO: every pickaxe
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
            .name(Component.translatable("bingo.goal.sleep_in_nether"))
            .icon(Items.PURPLE_BED)
        );
        addGoal(obtainItemGoal(id("fermented_spider_eye"), Items.FERMENTED_SPIDER_EYE)
            .tags(BingoTags.OVERWORLD));
        // TODO: different stairs
        addGoal(obtainItemGoal(id("ender_pearl"), Items.ENDER_PEARL, 2, 3)
            .infrequency(2));
        addGoal(obtainItemGoal(id("egg"), Items.EGG, 16, 16));
        // TODO: hang 3 different 4x4 paintings
        addGoal(obtainItemGoal(id("bone_block"), Items.BONE_BLOCK, 5, 10));
        // TODO: 2 creepers in the same boat
        // TODO: trade with a villager
        // TODO: different colored shields
        addGoal(obtainItemGoal(id("dead_bush"), Items.DEAD_BUSH)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME));
        addGoal(obtainItemGoal(id("grass"), Items.GRASS, 15, 32)
            .tooltip(Component.translatable("bingo.goal.grass.tooltip")));

        for (String dyeColor : List.of("cyan", "magenta", "red", "orange", "yellow", "green", "pink", "purple", "lime")) {
            Item dyeItem = BuiltInRegistries.ITEM.get(new ResourceLocation(dyeColor + "_dye"));
            addGoal(obtainItemGoal(id(dyeColor + "_dye"), dyeItem)
                .infrequency(10)
                .tags(BingoTags.OVERWORLD));
        }

        addGoal(BingoGoal.builder(id("never_sleep"))
            .criterion("sleep", PlayerTrigger.TriggerInstance.sleptInBed())
            .tags(BingoTags.NEVER, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.never_sleep"))
            .icon(Items.RED_BED)
            .catalyst("sleep"));
        addGoal(BingoGoal.builder(id("grow_huge_mushroom"))
            .criterion("grow", GrowFeatureTrigger.builder().feature(BingoFeatureTags.HUGE_MUSHROOMS).build())
            .tags(BingoTags.ACTION)
            .name(Component.translatable("bingo.goal.grow_huge_mushroom"))
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
            .icon(Items.LAVA_BUCKET)
            .antisynergy("bucket_types", "water_bucket", "lava_bucket", "milk_bucket")
            .reactant("use_buckets"));
        // TODO: different flowers
        // TODO: colors of concrete
        // TODO: colors of glazed terracotta
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
            .tooltip(Component.translatable("bingo.goal.finish_at_spawn.tooltip"))
            .icon(Items.COMPASS)
        );
        addGoal(obtainItemGoal(id("stone"), Items.STONE, 32, 64)
            .tooltip(Component.translatable("bingo.goal.stone.tooltip"))
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
        // TODO: rotten flesh, spider eye, bone, gunpowder and ender pearl
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
                    Optional.of(EntityPredicate.wrap(
                        EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(EntityType.BOAT)).build()
                    ))
                )
            ))
            .tags(BingoTags.NEVER, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.never_use_boat"))
            .icon(Items.OAK_BOAT));
        // TODO: get a fish into the nether
        addGoal(obtainItemGoal(id("dried_kelp_block"), Items.DRIED_KELP_BLOCK, 11, 20)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        // TODO: drown a zombie
        addGoal(obtainItemGoal(id("gunpowder"), Items.GUNPOWDER, 2, 5)
            .infrequency(2));
        addGoal(obtainItemGoal(id("spider_eye"), Items.SPIDER_EYE, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        // TODO: different edible items
        // TODO: breed 2-4 sets of mobs
        addGoal(crouchDistanceGoal(id("crouch_distance"), 100, 200));
        // TODO: never use debug
        // TODO: ring bell from 10 blocks away
        // TODO: repair item with grindstone
        addGoal(obtainItemGoal(id("sweet_berries"), Items.SWEET_BERRIES, 2, 6)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME));
        // TODO: banner pattern
        // TODO: drink sussy stew
        addGoal(BingoGoal.builder(id("drink_sus_stew"))
            .criterion("drink", ConsumeItemTrigger.TriggerInstance.usedItem(Items.SUSPICIOUS_STEW))
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD)
            .reactant("eat_non_meat")
            .name(Component.translatable("bingo.goal.drink_sus_stew", Items.SUSPICIOUS_STEW.getDescription()))
            .icon(Items.SUSPICIOUS_STEW));
        // TODO: give fox sword
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
        // TODO: fill 4 slots of soul campfire with porkchops
        addGoal(obtainItemGoal(id("soul_lantern"), Items.SOUL_LANTERN)
            .tags(BingoTags.NETHER));
        // TODO: open door with target block from 10 blocks away
        addGoal(obtainItemGoal(id("carrot_on_a_stick"), Items.CARROT_ON_A_STICK)
            .tags(BingoTags.OVERWORLD));
        // TODO: barter with piglin
        // TODO: become nauseous
        // TODO: enchanted item
        // TODO: remove enchantment with grindstone
        // TODO: never use sword
        // TODO: carnivore
        // TODO: clean banner
        // TODO: 5-7 different gold items
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
            .name(Component.translatable("bingo.goal.tame_horse"))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .icon(Items.HORSE_SPAWN_EGG));
        // TODO: hatch chicken from egg
        // TODO: empty cauldron without buckets or bottles
        // TODO: sleep in villager's bed
        // TODO: set fire to villager's house
        addGoal(obtainItemGoal(id("emerald"), Items.EMERALD)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("milk_cure"))
            .criterion("cure", BingoTriggers.CONSUME_MILK_BUCKET.createCriterion(
                new ConsumeMilkBucketTrigger.TriggerInstance(
                    Optional.of(ContextAwarePredicate.create(
                        new HasAnyEffectCondition(LootContext.EntityTarget.THIS)
                    )),
                    Optional.empty()
                )
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD) // TODO
            .name(Component.translatable("bingo.goal.milk_cure"))
            .icon(Items.MILK_BUCKET)
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
            .name(Component.translatable("bingo.goal.eat_entire_cake"))
            .icon(Items.CAKE)
            .reactant("eat_non_meat");
    }
}
