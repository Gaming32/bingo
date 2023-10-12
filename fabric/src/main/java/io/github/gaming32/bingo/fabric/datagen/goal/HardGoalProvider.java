package io.github.gaming32.bingo.fabric.datagen.goal;

import io.github.gaming32.bingo.conditions.BlockPatternCondition;
import io.github.gaming32.bingo.conditions.PillarCondition;
import io.github.gaming32.bingo.conditions.StairwayToHeavenCondition;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoTags;
import io.github.gaming32.bingo.data.icons.BlockIcon;
import io.github.gaming32.bingo.data.icons.CycleIcon;
import io.github.gaming32.bingo.data.icons.EntityIcon;
import io.github.gaming32.bingo.data.subs.BingoSub;
import io.github.gaming32.bingo.data.tags.BingoDamageTypeTags;
import io.github.gaming32.bingo.data.tags.BingoFeatureTags;
import io.github.gaming32.bingo.data.tags.BingoItemTags;
import io.github.gaming32.bingo.triggers.EnchantedItemTrigger;
import io.github.gaming32.bingo.triggers.*;
import io.github.gaming32.bingo.util.BlockPattern;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.*;

import java.util.Optional;
import java.util.function.Consumer;

public class HardGoalProvider extends DifficultyGoalProvider {
    public HardGoalProvider(Consumer<BingoGoal> goalAdder) {
        super(3, "hard/", goalAdder);
    }

    @Override
    public void addGoals() {
        addGoal(BingoGoal.builder(id("level_10_enchant"))
            .criterion("enchant", EnchantedItemTrigger.builder().requiredLevels(MinMaxBounds.Ints.atLeast(10)).build())
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.level_10_enchant"))
            .icon(new ItemStack(Items.ENCHANTING_TABLE, 10)));
        addGoal(BingoGoal.builder(id("milk_mooshroom"))
            .criterion("obtain", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
                ItemPredicate.Builder.item().of(Items.BUCKET),
                Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(EntityType.MOOSHROOM).build()))
            ))
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.milk_mooshroom"))
            .icon(EntityIcon.ofSpawnEgg(EntityType.MOOSHROOM))
            .infrequency(2)
        );
        addGoal(BingoGoal.builder(id("shear_mooshroom"))
            .criterion("obtain", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
                ItemPredicate.Builder.item().of(Items.SHEARS),
                Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(EntityType.MOOSHROOM).build()))
            ))
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.shear_mooshroom"))
            .icon(EntityIcon.ofSpawnEgg(EntityType.COW))
            .infrequency(2)
        );
        addGoal(obtainItemGoal(id("sea_lantern"), Items.SEA_LANTERN, 2, 5)
            .tags(BingoTags.OVERWORLD, BingoTags.OCEAN));
        addGoal(obtainItemGoal(id("sponge"), Items.SPONGE)
            .tooltip(Component.translatable("bingo.goal.sponge.tooltip"))
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("listen_to_music"))
            .criterion("obtain", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(Blocks.JUKEBOX)),
                ItemPredicate.Builder.item().of(ItemTags.MUSIC_DISCS)
            ))
            .tags(BingoTags.ITEM)
            .name(Component.translatable("bingo.goal.listen_to_music"))
            .icon(Items.JUKEBOX));
        addGoal(obtainSomeItemsFromTag(id("flowers"), BingoItemTags.FLOWERS, "bingo.goal.flowers", 11, 14)
            .antisynergy("flowers")
            .infrequency(3)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("diamond_block"), Items.DIAMOND_BLOCK, 2, 4)
            .infrequency(2));
        addGoal(BingoGoal.builder(id("zombified_piglin_sword"))
            .criterion("pickup", ItemPickedUpTrigger.TriggerInstance.pickedUpFrom(
                ItemPredicate.Builder.item().of(Items.GOLDEN_SWORD).build(),
                EntityPredicate.Builder.entity().of(EntityType.ZOMBIFIED_PIGLIN).build()
            ))
            .reactant("pacifist")
            .tags(BingoTags.ITEM, BingoTags.COMBAT, BingoTags.NETHER)
            .name(Component.translatable("bingo.goal.zombified_piglin_sword"))
            .icon(Items.GOLDEN_SWORD));
        // TODO: finish by launching fireworks of n different colors
        addGoal(BingoGoal.builder(id("nametag_enderman"))
            .criterion("nametag", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
                ItemPredicate.Builder.item().of(Items.NAME_TAG),
                Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(EntityType.ENDERMAN).build()))
            ))
            .tags(BingoTags.ACTION, BingoTags.COMBAT, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.nametag_enderman"))
            .icon(Items.NAME_TAG));
        addGoal(BingoGoal.builder(id("finish_on_blaze_spawner"))
            .criterion("spawner", PlayerTrigger.TriggerInstance.located(EntityPredicate.Builder.entity()
                .steppingOn(LocationPredicate.Builder.location()
                    .setBlock(spawnerPredicate(EntityType.BLAZE))
                )
            ))
            .tags(BingoTags.ACTION, BingoTags.NETHER, BingoTags.COMBAT, BingoTags.FINISH)
            .name(Component.translatable(
                "bingo.goal.finish_on_top_of_a",
                Component.translatable(
                    "bingo.spawner",
                    EntityType.BLAZE.getDescription(),
                    Blocks.SPAWNER.getName()
                )
            ))
            .icon(Blocks.SPAWNER)
        );
        addGoal(obtainSomeItemsFromTag(id("wool"), ItemTags.WOOL, "bingo.goal.wool", 12, 14)
            .antisynergy("wool_color")
            .infrequency(4)
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD));
        addGoal(obtainSomeItemsFromTag(id("terracotta"), ItemTags.TERRACOTTA, "bingo.goal.terracotta", 12, 14)
            .reactant("use_furnace")
            .antisynergy("terracotta_color")
            .infrequency(4)
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD));
        addGoal(obtainSomeItemsFromTag(id("glazed_terracotta"), BingoItemTags.GLAZED_TERRACOTTA, "bingo.goal.glazed_terracotta", 11, 14)
            .reactant("use_furnace")
            .antisynergy("glazed_terracotta_color")
            .infrequency(4)
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD));
        addGoal(obtainSomeItemsFromTag(id("concrete"), BingoItemTags.CONCRETE, "bingo.goal.concrete", 12, 14)
            .antisynergy("concrete_color")
            .infrequency(4)
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD));
        addGoal(bedRowGoal(id("bed_row"), 11, 14));
        addGoal(BingoGoal.builder(id("poison_parrot"))
            .criterion("poison", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
                ItemPredicate.Builder.item().of(Items.COOKIE),
                Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(EntityType.PARROT).build()))
            ))
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.poison_parrot"))
            .icon(Items.COOKIE)
            .infrequency(2)
            .reactant("pacifist"));
        addGoal(BingoGoal.builder(id("tame_parrot"))
            .criterion("tame", TameAnimalTrigger.TriggerInstance.tamedAnimal(EntityPredicate.Builder.entity().of(EntityType.PARROT)))
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.tame_parrot"))
            .icon(EntityIcon.ofSpawnEgg(EntityType.PARROT))
            .infrequency(2)
        );
        addGoal(BingoGoal.builder(id("ice_on_magma"))
            .criterion("obtain", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(
                AnyOfCondition.anyOf(
                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.ICE),
                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.MAGMA_BLOCK)
                ),
                BlockPatternCondition.builder().aisle("i", "m")
                    .where('i', BlockPredicate.Builder.block().of(Blocks.ICE))
                    .where('m', BlockPredicate.Builder.block().of(Blocks.MAGMA_BLOCK))
                    .rotations(BlockPattern.Rotations.NONE)))
            .tags(BingoTags.ITEM, BingoTags.BUILD, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.ice_on_magma"))
            .icon(Items.BASALT));
        addGoal(obtainLevelsGoal(id("levels"), 27, 37));
        addGoal(blockCubeGoal(id("ice_cube"), Blocks.ICE, BlockTags.ICE, Blocks.ICE.getName()));
        addGoal(BingoGoal.builder(id("stairway_to_heaven"))
            .criterion("stairway", CriteriaTriggers.LOCATION.createCriterion(
                new PlayerTrigger.TriggerInstance(
                    Optional.of(ContextAwarePredicate.create(
                        LocationCheck.checkLocation(
                            LocationPredicate.Builder.location().setY(MinMaxBounds.Doubles.atLeast(319))
                        ).build(),
                        StairwayToHeavenCondition.INSTANCE
                    ))
                )
            ))
            .name(Component.translatable("bingo.goal.stairway_to_heaven"))
            .tooltip(Component.translatable("bingo.goal.stairway_to_heaven.tooltip"))
            .tags(BingoTags.BUILD, BingoTags.OVERWORLD, BingoTags.FINISH)
            .icon(Blocks.COBBLESTONE_STAIRS)
        );
        addGoal(BingoGoal.builder(id("kill_ghast_in_overworld"))
            .criterion("murder", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity()
                .of(EntityType.GHAST)
                .located(LocationPredicate.Builder.inDimension(Level.OVERWORLD))
            ))
            .name(Component.translatable("bingo.goal.kill_ghast_in_overworld"))
            .tags(BingoTags.ACTION, BingoTags.NETHER, BingoTags.OVERWORLD)
            .icon(Items.GHAST_TEAR)
            .reactant("pacifist"));
        addGoal(obtainItemGoal(id("enchanted_golden_apple"), Items.ENCHANTED_GOLDEN_APPLE));

        addGoal(BingoGoal.builder(id("never_wear_armor_or_use_shields"))
            .criterion("equip", EquipItemTrigger.builder()
                .newItem(ItemPredicate.Builder.item().of(BingoItemTags.ARMOR).build())
                .slots(EquipmentSlot.Type.ARMOR)
                .build()
            )
            .criterion("use", CriteriaTriggers.USING_ITEM.createCriterion(
                new UsingItemTrigger.TriggerInstance(
                    Optional.empty(),
                    Optional.of(ItemPredicate.Builder.item().of(BingoItemTags.SHIELDS).build())
                )
            ))
            .requirements(AdvancementRequirements.Strategy.OR)
            .tags(BingoTags.NEVER)
            .name(Component.translatable("bingo.goal.never_wear_armor_or_use_shields"))
            .tooltip(Component.translatable("bingo.goal.never_wear_armor_or_use_shields.tooltip"))
            .icon(makeItemWithGlint(Items.SHIELD))
            .antisynergy("never_wear_armor")
            .catalyst("wear_armor")
        );
        addGoal(BingoGoal.builder(id("full_iron_mob"))
            .criterion("kill", CriteriaTriggers.PLAYER_KILLED_ENTITY.createCriterion(
                new KilledTrigger.TriggerInstance(
                    Optional.empty(),
                    Optional.of(ContextAwarePredicate.create(
                        LootItemEntityPropertyCondition.hasProperties(
                            LootContext.EntityTarget.THIS,
                            EntityPredicate.Builder.entity().of(EntityType.PLAYER).build()
                        ).invert().build(),
                        LootItemEntityPropertyCondition.hasProperties(
                            LootContext.EntityTarget.THIS,
                            EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment()
                                .head(ItemPredicate.Builder.item().of(Items.IRON_HELMET))
                                .chest(ItemPredicate.Builder.item().of(Items.IRON_CHESTPLATE))
                                .legs(ItemPredicate.Builder.item().of(Items.IRON_LEGGINGS))
                                .feet(ItemPredicate.Builder.item().of(Items.IRON_BOOTS))
                                .build()
                            )
                        ).build()
                    )),
                    Optional.empty()
                )
            ))
            .tags(BingoTags.ACTION, BingoTags.COMBAT)
            .reactant("pacifist")
            .name(Component.translatable("bingo.goal.full_iron_mob"))
            .icon(makeItemWithGlint(Items.IRON_SWORD))
        );
        // TODO: enchant 5 items
        addGoal(BingoGoal.builder(id("never_use_buckets"))
            .criterion("filled_bucket", CriteriaTriggers.FILLED_BUCKET.createCriterion(
                new FilledBucketTrigger.TriggerInstance(Optional.empty(), Optional.empty())
            ))
            .criterion("placed_block", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(MatchTool.toolMatches(ItemPredicate.Builder.item().of(BingoItemTags.BUCKETS))))
            .criterion("use_on_entity", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(ItemPredicate.Builder.item().of(BingoItemTags.BUCKETS), Optional.empty()))
            .criterion("consume", ConsumeItemTrigger.TriggerInstance.usedItem(ItemPredicate.Builder.item().of(BingoItemTags.BUCKETS)))
            .requirements(AdvancementRequirements.Strategy.OR)
            .tags(BingoTags.NEVER)
            .catalyst("use_buckets")
            .name(Component.translatable("bingo.goal.never_use_buckets"))
            .icon(Items.BUCKET));
        addGoal(obtainItemGoal(id("conduit"), Items.CONDUIT)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        addGoal(obtainSomeItemsFromTag(id("dead_coral_blocks"), BingoItemTags.DEAD_CORAL_BLOCKS, "bingo.goal.dead_coral_blocks", 2, 5)
            .tags(BingoTags.OCEAN, BingoTags.RARE_BIOME, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("sea_pickle"), Items.SEA_PICKLE, 16, 32)
            .tags(BingoTags.OCEAN, BingoTags.RARE_BIOME, BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("void_death_to_entity"))
            .criterion("death", EntityKilledPlayerTrigger.builder()
                .source(DamageSourcePredicate.Builder.damageType()
                    .tag(TagPredicate.is(BingoDamageTypeTags.VOID))
                    .build()
                )
                .build()
            )
            .tags(BingoTags.ACTION)
            .name(Component.translatable("bingo.goal.void_death_to_entity"))
            .icon(Blocks.BLACK_CONCRETE)
        );
        addGoal(obtainItemGoal(id("cookie"), Items.COOKIE)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("mega_jungle_tree"))
            .criterion("grow", GrowFeatureTrigger.builder()
                .feature(BingoFeatureTags.MEGA_JUNGLE_TREES)
                .build()
            )
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.mega_jungle_tree"))
            .icon(new ItemStack(Blocks.JUNGLE_SAPLING, 4))
        );
        addGoal(obtainItemGoal(id("prismarine_shard"), Items.PRISMARINE_SHARD, 2, 10)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("jungle_log"), Items.JUNGLE_LOG, 16, 32)
            .infrequency(4)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("jungle_wood"), Items.JUNGLE_WOOD, 11, 20)
            .infrequency(4)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("stripped_jungle_wood"), Items.STRIPPED_JUNGLE_WOOD, 11, 20)
            .reactant("axe_use")
            .infrequency(4)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("stripped_jungle_log"), Items.STRIPPED_JUNGLE_LOG, 11, 20)
            .reactant("axe_use")
            .infrequency(4)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD));
        addGoal(obtainSomeItemsFromTag(id("diamond_in_name"), BingoItemTags.DIAMOND_IN_NAME, "bingo.goal.diamond_in_name", 5, 7)
            .antisynergy("diamond_items")
            .tooltip(Component.translatable("bingo.goal.diamond_in_name.tooltip")));
        addGoal(BingoGoal.builder(id("destroy_spawner"))
            .criterion("destroy", BreakBlockTrigger.builder().block(Blocks.SPAWNER).build())
            .name(Component.translatable("bingo.goal.destroy_spawner"))
            .icon(Blocks.SPAWNER)
            .tags(BingoTags.ACTION, BingoTags.COMBAT));
        addGoal(obtainItemGoal(id("popped_chorus_fruit"), Items.POPPED_CHORUS_FRUIT, 32, 64)
            .tags(BingoTags.END));
        addGoal(BingoGoal.builder(id("villager_in_end"))
            .criterion("kill", KilledTrigger.TriggerInstance.playerKilledEntity(
                EntityPredicate.Builder.entity()
                    .of(EntityType.VILLAGER)
                    .located(LocationPredicate.Builder.inDimension(Level.END))
            ))
            .tags(BingoTags.ACTION, BingoTags.END, BingoTags.VILLAGE, BingoTags.COMBAT)
            .reactant("pacifist")
            .name(Component.translatable("bingo.goal.villager_in_end", EntityType.VILLAGER.getDescription()))
            .icon(Blocks.CAULDRON)
        );
        addGoal(obtainItemGoal(id("dragon_breath"), Items.DRAGON_BREATH, 5, 16)
            .tags(BingoTags.COMBAT, BingoTags.END));
        addGoal(obtainItemGoal(id("dragon_egg"), Items.DRAGON_EGG)
            .tags(BingoTags.COMBAT, BingoTags.END));
        addGoal(BingoGoal.builder(id("complete_full_size_map"))
            .criterion("complete", CompleteMapTrigger.TriggerInstance.completeMap(
                MinMaxBounds.Ints.atLeast(MapItemSavedData.MAX_SCALE)
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.complete_full_size_map"))
            .icon(Items.FILLED_MAP)
            .antisynergy("complete_map"));
        addGoal(BingoGoal.builder(id("die_to_villager"))
            .criterion("die", EntityKilledPlayerTrigger.builder()
                .creditedEntity(EntityPredicate.Builder.entity().of(EntityType.VILLAGER).build())
                .build()
            )
            .tags(BingoTags.VILLAGE, BingoTags.OVERWORLD, BingoTags.STAT, BingoTags.COMBAT)
            .name(Component.translatable("bingo.goal.die_to_villager", EntityType.VILLAGER.getDescription()))
            .tooltip(Component.translatable("bingo.goal.die_to_villager.tooltip"))
            .icon(EntityIcon.ofSpawnEgg(EntityType.VILLAGER))
        );
        addGoal(BingoGoal.builder(id("pop_totem"))
            .criterion("totem", UsedTotemTrigger.TriggerInstance.usedTotem(Items.TOTEM_OF_UNDYING))
            .name(Component.translatable("bingo.goal.pop_totem"))
            .icon(Items.TOTEM_OF_UNDYING)
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD));
        addGoal(obtainAllItemsFromTag(ItemTags.SWORDS, "swords")
            .tags(BingoTags.NETHER)
            .tooltip(Component.translatable("bingo.goal.all_somethings.tooltip")));
        addGoal(obtainAllItemsFromTag(ItemTags.PICKAXES, "pickaxes")
            .tags(BingoTags.NETHER)
            .tooltip(Component.translatable("bingo.goal.all_somethings.tooltip")));
        addGoal(BingoGoal.builder(id("pacifist"))
            .criterion("kill", KilledTrigger.TriggerInstance.playerKilledEntity())
            .tags(BingoTags.NEVER, BingoTags.STAT)
            .name(Component.translatable("bingo.goal.pacifist"))
            .tooltip(Component.translatable("bingo.goal.pacifist.tooltip"))
            .icon(Items.DIAMOND_SWORD)
            .catalyst("pacifist"));
        addGoal(BingoGoal.builder(id("scaffolding_tower"))
            .criterion("destroy", BreakBlockTrigger.builder()
                .location(
                    new LocationCheck(
                        Optional.of(LocationPredicate.Builder.atYLocation(MinMaxBounds.Doubles.atMost(-58)).build()),
                        BlockPos.ZERO
                    ),
                    new PillarCondition(379, Optional.of(BlockPredicate.Builder.block().of(Blocks.SCAFFOLDING).build()))
                )
                .build()
            )
            .tags(BingoTags.BUILD, BingoTags.OVERWORLD, BingoTags.FINISH)
            .name(Component.translatable("bingo.goal.scaffolding_tower"))
            .tooltip(Component.translatable("bingo.goal.scaffolding_tower.tooltip"))
            .icon(makeItemWithGlint(Blocks.SCAFFOLDING))
        );
        addGoal(BingoGoal.builder(id("feed_panda_cake"))
            .criterion("feed", CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.createCriterion(
                new PickedUpItemTrigger.TriggerInstance(
                    Optional.empty(),
                    Optional.of(ItemPredicate.Builder.item().of(Blocks.CAKE).build()),
                    Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(EntityType.PANDA).build()))
                )
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD, BingoTags.RARE_BIOME)
            .name(Component.translatable(
                "bingo.goal.feed_panda_cake",
                EntityType.PANDA.getDescription(), Blocks.CAKE.getName()
            ))
            .icon(EntityIcon.ofSpawnEgg(EntityType.PANDA))
        );
        addGoal(BingoGoal.builder(id("breed_pandas"))
            .criterion("breed", BredAnimalsTrigger.TriggerInstance.bredAnimals(
                EntityPredicate.Builder.entity().of(EntityType.PANDA)
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD, BingoTags.RARE_BIOME)
            .name(Component.translatable("bingo.goal.breed_pandas"))
            .icon(Items.BAMBOO)
        );
        addGoal(BingoGoal.builder(id("disarm_pillager"))
            .criterion("break", EntityTrigger.TriggerInstance.brokeCrossbow(
                EntityPredicate.Builder.entity().of(EntityType.PILLAGER).build()
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD, BingoTags.COMBAT, BingoTags.RARE_BIOME)
            .name(Component.translatable(
                "bingo.goal.disarm_pillager",
                EntityType.PILLAGER.getDescription()
            ))
            .icon(Items.CROSSBOW)
        );
        addGoal(BingoGoal.builder(id("stun_ravager"))
            .criterion("stun", EntityTrigger.TriggerInstance.stunnedRavager())
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD, BingoTags.COMBAT, BingoTags.VILLAGE)
            .name(Component.translatable(
                "bingo.goal.stun_ravager",
                EntityType.RAVAGER.getDescription()
            ))
            .icon(EntityIcon.ofSpawnEgg(EntityType.RAVAGER))
        );
        addGoal(BingoGoal.builder(id("hero_of_the_village"))
            .criterion("raid_won", PlayerTrigger.TriggerInstance.raidWon())
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD, BingoTags.COMBAT, BingoTags.VILLAGE)
            .reactant("pacifist")
            .name(Component.translatable("advancements.adventure.hero_of_the_village.title"))
            .icon(Raid.getLeaderBannerInstance())
        );
        addGoal(BingoGoal.builder(id("ocelot_trust"))
            .criterion("trust", TameAnimalTrigger.TriggerInstance.tamedAnimal(
                EntityPredicate.Builder.entity().of(EntityType.OCELOT)
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD, BingoTags.RARE_BIOME)
            .name(Component.translatable(
                "bingo.goal.ocelot_trust",
                EntityType.OCELOT.getDescription()
            ))
            .icon(Items.SALMON)
        );
        addGoal(obtainItemGoal(id("ender_eye"), Items.ENDER_EYE, 12, 12)
            .reactant("pacifist")
            .tags(BingoTags.NETHER, BingoTags.COMBAT)
            .tooltip(Component.translatable("bingo.goal.ender_eye.hard.tooltip")));
        addGoal(obtainItemGoal(id("netherite_ingot"), Items.NETHERITE_INGOT)
            .tags(BingoTags.NETHER));
        addGoal(obtainItemGoal(id("wither_skeleton_skull"), Items.WITHER_SKELETON_SKULL)
            .reactant("pacifist")
            .tags(BingoTags.NETHER, BingoTags.COMBAT, BingoTags.RARE_BIOME));
        addGoal(obtainItemGoal(id("gilded_blackstone"), Items.GILDED_BLACKSTONE)
            .tags(BingoTags.NETHER, BingoTags.RARE_BIOME));
        addGoal(BingoGoal.builder(id("use_lodestone"))
            .criterion("use", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                LocationPredicate.Builder.location().setBlock(
                    BlockPredicate.Builder.block().of(Blocks.LODESTONE)
                ),
                ItemPredicate.Builder.item().of(Items.COMPASS)
            ))
            .tags(BingoTags.ACTION, BingoTags.NETHER)
            .name(Component.translatable(
                "bingo.goal.use_lodestone",
                Items.COMPASS.getDescription(), Blocks.LODESTONE.getName()
            ))
            .icon(Blocks.LODESTONE)
        );
        addGoal(BingoGoal.builder(id("piglin_brute_axe"))
            .criterion("give", CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.createCriterion(
                new PickedUpItemTrigger.TriggerInstance(
                    Optional.empty(),
                    Optional.of(ItemPredicate.Builder.item()
                        .of(ItemTags.AXES)
                        .hasEnchantment(new EnchantmentPredicate(Optional.empty(), MinMaxBounds.Ints.atLeast(1)))
                        .build()
                    ),
                    Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity()
                        .of(EntityType.PIGLIN_BRUTE)
                        .build()
                    ))
                )
            ))
            .tags(BingoTags.ACTION, BingoTags.NETHER, BingoTags.COMBAT)
            .name(Component.translatable("bingo.goal.piglin_brute_axe", EntityType.PIGLIN_BRUTE.getDescription()))
            .icon(makeItemWithGlint(Items.GOLDEN_AXE))
        );
        addGoal(BingoGoal.builder(id("6x6scaffolding"))
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
                ).where('#', BlockPredicate.Builder.block().of(Blocks.SCAFFOLDING))))
            .tags(BingoTags.OVERWORLD, BingoTags.BUILD)
            .name(Component.translatable("bingo.goal.6x6scaffolding"))
            .tooltip(Component.translatable("bingo.goal.6x6scaffolding.tooltip"))
            .tooltipIcon(new ResourceLocation("bingo:textures/gui/tooltips/6x6scaffolding.png"))
            .icon(Items.SCAFFOLDING));
        addGoal(obtainItemGoal(id("honey_block"), Items.HONEY_BLOCK, 2, 5)
            .setAntisynergy("honey")
            .infrequency(2)
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("honeycomb_block"), Items.HONEYCOMB_BLOCK, 6, 15)
            .setAntisynergy("honeycomb")
            .infrequency(2)
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("kill_wandering_trader"))
            .criterion("kill", KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(EntityType.WANDERING_TRADER)))
            .name(Component.translatable("bingo.goal.kill_wandering_trader"))
            .icon(EntityIcon.ofSpawnEgg(EntityType.WANDERING_TRADER))
            .reactant("pacifist")
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD, BingoTags.COMBAT));
        addGoal(BingoGoal.builder(id("cure_zombie_villager"))
            .criterion("cure", CuredZombieVillagerTrigger.TriggerInstance.curedZombieVillager())
            .name(Component.translatable("bingo.goal.cure_zombie_villager"))
            .icon(Items.GOLDEN_APPLE)
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("burn_mending_book"))
            .criterion("obtain", KillItemTrigger.builder()
                .item(ItemPredicate.Builder.item().of(Items.ENCHANTED_BOOK)
                    .hasStoredEnchantment(new EnchantmentPredicate(Enchantments.MENDING, MinMaxBounds.Ints.ANY)).build())
                .damage(DamagePredicate.Builder.damageInstance().type(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_FIRE))).build())
                .build()
            )
            .name(Component.translatable("bingo.goal.burn_mending_book"))
            .icon(Blocks.FIRE, Items.ENCHANTED_BOOK)
            .tags(BingoTags.ACTION));
        // TODO: never smelt with furnaces
        addGoal(BingoGoal.builder(id("huge_fungus_in_overworld"))
            .criterion("grow", GrowFeatureTrigger.builder()
                .feature(BingoFeatureTags.HUGE_FUNGI)
                .location(LocationPredicate.Builder.inDimension(Level.OVERWORLD).build())
                .build()
            )
            .name(Component.translatable("bingo.goal.huge_fungus_in_overworld"))
            .icon(Items.WARPED_FUNGUS)
            .antisynergy("grow_fungus")
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD, BingoTags.NETHER));
        addGoal(BingoGoal.builder(id("dirt_netherrack_end_stone"))
            .sub("count", BingoSub.random(32, 64))
            .criterion("items", TotalCountInventoryChangeTrigger.builder()
                .items(
                    ItemPredicate.Builder.item().of(Blocks.DIRT).withCount(MinMaxBounds.Ints.atLeast(0)).build(),
                    ItemPredicate.Builder.item().of(Blocks.NETHERRACK).withCount(MinMaxBounds.Ints.atLeast(0)).build(),
                    ItemPredicate.Builder.item().of(Blocks.END_STONE).withCount(MinMaxBounds.Ints.atLeast(0)).build()
                )
                .build(),
                subber -> subber.multiSub("conditions.items.*.count.min", "count")
            )
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD, BingoTags.NETHER, BingoTags.END)
            .name(
                Component.translatable(
                    "bingo.goal.dirt_netherrack_end_stone",
                    0, Blocks.DIRT.getName(), Blocks.NETHERRACK.getName(), Blocks.END_STONE.getName()
                ),
                subber -> subber.sub("with.0", "count")
            )
            .icon(new CycleIcon(
                BlockIcon.ofBlock(Blocks.DIRT),
                BlockIcon.ofBlock(Blocks.NETHERRACK),
                BlockIcon.ofBlock(Blocks.END_STONE)
            ), subber -> subber.multiSub("value.*.item.Count", "count"))
        );
        addGoal(BingoGoal.builder(id("tame_mule"))
            .criterion("obtain", TameAnimalTrigger.TriggerInstance.tamedAnimal(
                EntityPredicate.Builder.entity().of(EntityType.MULE)
            ))
            .name(Component.translatable("bingo.goal.tame_mule"))
            .icon(EntityIcon.ofSpawnEgg(EntityType.MULE))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
        );
        addGoal(BingoGoal.builder(id("carrot_stick_to_rod"))
            .criterion("break", ItemBrokenTrigger.TriggerInstance.itemBroken(
                ItemPredicate.Builder.item().of(Items.CARROT_ON_A_STICK).build()
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name(Component.translatable(
                "bingo.goal.carrot_stick_to_rod",
                Items.CARROT_ON_A_STICK.getDescription(),
                Items.FISHING_ROD.getDescription()
            ))
            .icon(Items.CARROT_ON_A_STICK));
        addGoal(obtainItemGoal(id("skull_banner_pattern"), Items.SKULL_BANNER_PATTERN)
            .tags(BingoTags.NETHER, BingoTags.COMBAT, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable(
                "bingo.goal.skull_banner_pattern",
                Component.translatable("item.minecraft.skull_banner_pattern.desc"),
                Component.translatable("item.minecraft.skull_banner_pattern")
            ))
            .icon(makeBannerWithPattern(Items.WHITE_BANNER, BannerPatterns.SKULL, DyeColor.BLACK)));
        addGoal(obtainItemGoal(id("turtle_helmet"), Items.TURTLE_HELMET)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("sniffer_egg"), Items.SNIFFER_EGG)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        addGoal(obtainSomeItemsFromTag(id("armor_trims"), ItemTags.TRIM_TEMPLATES, "bingo.goal.armor_trims", 3, 3)
            .antisynergy("armor_trims"));
    }
}
