package io.github.gaming32.bingo.fabric.datagen.goal;

import io.github.gaming32.bingo.conditions.BlockPatternCondition;
import io.github.gaming32.bingo.conditions.DistanceFromSpawnCondition;
import io.github.gaming32.bingo.conditions.FlammableCondition;
import io.github.gaming32.bingo.conditions.HasAnyEffectCondition;
import io.github.gaming32.bingo.conditions.HasOnlyBeenDamagedByCondition;
import io.github.gaming32.bingo.conditions.InStructureCondition;
import io.github.gaming32.bingo.conditions.PassengersCondition;
import io.github.gaming32.bingo.conditions.ToolDamageCondition;
import io.github.gaming32.bingo.conditions.ToolIsEnchantedCondition;
import io.github.gaming32.bingo.conditions.VillagerOwnershipCondition;
import io.github.gaming32.bingo.data.BingoDifficulties;
import io.github.gaming32.bingo.data.BingoTags;
import io.github.gaming32.bingo.data.goal.BingoGoal;
import io.github.gaming32.bingo.data.goal.GoalBuilder;
import io.github.gaming32.bingo.data.icons.BlockIcon;
import io.github.gaming32.bingo.data.icons.CycleIcon;
import io.github.gaming32.bingo.data.icons.EffectIcon;
import io.github.gaming32.bingo.data.icons.EntityIcon;
import io.github.gaming32.bingo.data.icons.EntityTypeTagCycleIcon;
import io.github.gaming32.bingo.data.icons.IndicatorIcon;
import io.github.gaming32.bingo.data.icons.ItemIcon;
import io.github.gaming32.bingo.data.icons.ItemTagCycleIcon;
import io.github.gaming32.bingo.data.progresstrackers.AchievedRequirementsProgressTracker;
import io.github.gaming32.bingo.data.progresstrackers.GoalAchievedCountProgressTracker;
import io.github.gaming32.bingo.data.subs.BingoSub;
import io.github.gaming32.bingo.data.subs.SubBingoSub;
import io.github.gaming32.bingo.data.tags.bingo.BingoEntityTypeTags;
import io.github.gaming32.bingo.data.tags.bingo.BingoFeatureTags;
import io.github.gaming32.bingo.data.tags.bingo.BingoItemTags;
import io.github.gaming32.bingo.subpredicates.PaintingPredicate;
import io.github.gaming32.bingo.triggers.AdjacentPaintingTrigger;
import io.github.gaming32.bingo.triggers.ArrowPressTrigger;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import io.github.gaming32.bingo.triggers.ChickenHatchTrigger;
import io.github.gaming32.bingo.triggers.CompleteMapTrigger;
import io.github.gaming32.bingo.triggers.DestroyVehicleTrigger;
import io.github.gaming32.bingo.triggers.DifferentColoredShieldsTrigger;
import io.github.gaming32.bingo.triggers.DoorOpenedByTargetTrigger;
import io.github.gaming32.bingo.triggers.EntityDieNearPlayerTrigger;
import io.github.gaming32.bingo.triggers.EquipItemTrigger;
import io.github.gaming32.bingo.triggers.FillBundleTrigger;
import io.github.gaming32.bingo.triggers.GrowFeatureTrigger;
import io.github.gaming32.bingo.triggers.HasSomeFoodItemsTrigger;
import io.github.gaming32.bingo.triggers.IntentionalGameDesignTrigger;
import io.github.gaming32.bingo.triggers.RelativeStatsTrigger;
import io.github.gaming32.bingo.triggers.ShootBellTrigger;
import io.github.gaming32.bingo.triggers.UseGrindstoneTrigger;
import io.github.gaming32.bingo.triggers.WearDifferentColoredArmorTrigger;
import io.github.gaming32.bingo.triggers.ZombieDrownedTrigger;
import io.github.gaming32.bingo.util.BingoUtil;
import io.github.gaming32.bingo.util.BlockPattern;
import io.github.gaming32.bingo.util.ResourceLocations;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEntityTypeTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.ConsumeItemTrigger;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DamagePredicate;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.DataComponentMatchers;
import net.minecraft.advancements.critereon.DefaultBlockInteractionTrigger;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.EffectsChangedTrigger;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntityTypePredicate;
import net.minecraft.advancements.critereon.FishingRodHookedTrigger;
import net.minecraft.advancements.critereon.InputPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.advancements.critereon.PickedUpItemTrigger;
import net.minecraft.advancements.critereon.PlayerInteractTrigger;
import net.minecraft.advancements.critereon.PlayerPredicate;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.advancements.critereon.SummonedEntityTrigger;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.advancements.critereon.TameAnimalTrigger;
import net.minecraft.advancements.critereon.TradeTrigger;
import net.minecraft.advancements.critereon.UsingItemTrigger;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class EasyGoalProvider extends DifficultyGoalProvider {
    public EasyGoalProvider(BiConsumer<ResourceLocation, BingoGoal> goalAdder, HolderLookup.Provider registries) {
        super(BingoDifficulties.EASY, goalAdder, registries);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void addGoals() {
        final var entityTypes = registries.lookupOrThrow(Registries.ENTITY_TYPE);
        final var blocks = registries.lookupOrThrow(Registries.BLOCK);
        final var items = registries.lookupOrThrow(Registries.ITEM);

        addGoal(BingoGoal.builder(id("different_fish"))
            .sub("count", BingoSub.random(2, 4))
            .criterion("obtain",
                HasSomeFoodItemsTrigger.builder().requiredCount(1).tag(ItemTags.FISHES).build(),
                subber -> subber.sub("conditions.required_count", "count"))
            .progress("obtain")
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.different_fish", 0), subber -> subber.sub("with.0", "count"))
            .tooltip("different_fish")
            .icon(new ItemTagCycleIcon(ItemTags.FISHES), subber -> subber.sub("+count", "count")));
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
        addGoal(obtainItemGoal(id("mushroom_stew"), items, Items.MUSHROOM_STEW, 2, 5));
        addGoal(BingoGoal.builder(id("shoot_button"))
            .criterion("obtain", ArrowPressTrigger.builder()
                .arrow(EntityPredicate.Builder.entity().of(entityTypes, EntityTypeTags.ARROWS).build())
                .buttonOrPlate(BlockPredicate.Builder.block().of(blocks, BlockTags.BUTTONS).build())
                .build()
            )
            .tags(BingoTags.ACTION)
            .name("shoot_button")
            .icon(Items.OAK_BUTTON));
        addGoal(obtainItemGoal(id("writable_book"), items, Items.WRITABLE_BOOK)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("flint"), items, Items.FLINT, 16, 64));
        addGoal(eatEntireCake());
        addGoal(obtainItemGoal(id("pumpkin_pie"), items, Items.PUMPKIN_PIE)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("fish_treasure_junk"))
            .criterion("treasure", FishingRodHookedTrigger.TriggerInstance.fishedItem(
                Optional.empty(), Optional.empty(),
                Optional.of(ItemPredicate.Builder.item().of(items, BingoItemTags.FISHING_TREASURE).build())
            ))
            .criterion("junk", FishingRodHookedTrigger.TriggerInstance.fishedItem(
                Optional.empty(), Optional.empty(),
                Optional.of(ItemPredicate.Builder.item().of(items, BingoItemTags.FISHING_JUNK).build())
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name("fish_treasure_junk").tooltip("fish_treasure_junk")
            .icon(makeItemWithGlint(Items.FISHING_ROD))
            .reactant("fishing")
        );
        addGoal(obtainItemGoal(id("coarse_dirt"), items, Items.COARSE_DIRT, 16, 64)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("clock"), items, Items.CLOCK, 2, 3)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("iron_block"), items, Items.IRON_BLOCK, 2, 4)
            .infrequency(2));
        addGoal(obtainItemGoal(id("gold_block"), items, Items.GOLD_BLOCK)
            .infrequency(2));
        addGoal(obtainItemGoal(id("golden_apple"), items, Items.GOLDEN_APPLE));
        addGoal(obtainItemGoal(id("bookshelf"), items, Items.BOOKSHELF, 2, 4)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("never_wear_chestplates"))
            .criterion("equip", EquipItemTrigger.builder()
                .newItem(ItemPredicate.Builder.item().of(items, ItemTags.CHEST_ARMOR).build())
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
                    Optional.of(ItemPredicate.Builder.item().of(items, ConventionalItemTags.SHIELD_TOOLS).build())
                )
            ))
            .tags(BingoTags.NEVER)
            .name("never_use_shields")
            .tooltip("never_use_shields")
            .icon(Items.SHIELD));
        addGoal(obtainItemGoal(id("jukebox"), items, Items.JUKEBOX));
        addGoal(BingoGoal.builder(id("3x3x3_glass_cube"))
            .criterion("build", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(
                LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.GLASS)
                    .or(LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.LAVA)),
                BlockPatternCondition.builder()
                    .aisle("###", "###", "###")
                    .aisle("###", "#+#", "###")
                    .aisle("###", "###", "###")
                    .where('#', BlockPredicate.Builder.block().of(blocks, Blocks.GLASS))
                    .where('+', BlockPredicate.Builder.block().of(blocks, Blocks.LAVA))
                    .rotations(BlockPattern.Rotations.NONE)
            ))
            .name("3x3x3_glass_cube")
            .icon(new CycleIcon(
                new ItemIcon(new ItemStack(Items.GLASS, 26)),
                ItemIcon.ofItem(Items.LAVA_BUCKET)
            ))
            .tags(BingoTags.BUILD, BingoTags.OVERWORLD)
        );
        addGoal(obtainItemGoal(id("mossy_cobblestone"), items, Items.MOSSY_COBBLESTONE, 16, 32)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("cactus"), items, Items.CACTUS, 5, 15)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("tnt"), items, Items.TNT, 2, 3)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainLevelsGoal(id("levels"), 8, 15));
        addGoal(BingoGoal.builder(id("create_snow_golem"))
            .criterion("summon", SummonedEntityTrigger.TriggerInstance.summonedEntity(
                EntityPredicate.Builder.entity().of(entityTypes, EntityType.SNOW_GOLEM)
            ))
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.create_snow_golem", EntityType.SNOW_GOLEM.getDescription()))
            .icon(EntityIcon.ofSpawnEgg(EntityType.SNOW_GOLEM))
        );
        addGoal(obtainItemGoal(id("note_block"), items, Items.NOTE_BLOCK, 5, 15)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("ink_sac"), items, Items.INK_SAC, 5, 15)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("bread"), items, Items.BREAD, 6, 10)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("hay_block"), items, Items.HAY_BLOCK, 2, 3)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainSomeItemsFromTag(id("wool_colors"), ItemTags.WOOL, "bingo.goal.wool_colors", 5, 8)
            .antisynergy("wool_color")
            .infrequency(4)
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("piston"), items, Items.PISTON)
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
        addGoal(obtainItemGoal(id("soul_sand"), items, Items.SOUL_SAND, 5, 10)
            .tags(BingoTags.NETHER));
        addGoal(obtainItemGoal(id("soul_soil"), items, Items.SOUL_SOIL, 5, 10)
            .tags(BingoTags.NETHER));
        addGoal(obtainItemGoal(id("pumpkin"), items, Items.PUMPKIN, 5, 10)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("carved_pumpkin"), items, Items.CARVED_PUMPKIN, 2, 5)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("vine"), items, Items.VINE, 10, 30)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME));
        addGoal(obtainSomeItemsFromTag(id("different_slabs"), ItemTags.SLABS, "bingo.goal.different_slabs", 5, 7)
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
        addGoal(obtainItemGoal(id("bricks"), items, Items.BRICKS, 16, 64)
            .reactant("use_furnace")
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("nether_bricks"), items, Items.NETHER_BRICKS, 16, 32)
            .reactant("use_furnace")
            .tags(BingoTags.NETHER));
        addGoal(obtainItemGoal(id("arrow"), items, Items.ARROW, 16, 64));
        addGoal(BingoGoal.builder(id("sleep_in_nether"))
            .criterion("sleep", IntentionalGameDesignTrigger.TriggerInstance.clicked(
                LocationPredicate.Builder.inDimension(Level.NETHER).build()
            ))
            .tags(BingoTags.ACTION, BingoTags.NETHER, BingoTags.OVERWORLD)
            .name("sleep_in_nether")
            .icon(Items.PURPLE_BED)
        );
        addGoal(obtainItemGoal(id("fermented_spider_eye"), items, Items.FERMENTED_SPIDER_EYE)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainSomeItemsFromTag(id("different_stairs"), ItemTags.STAIRS, "bingo.goal.different_stairs", 5, 7)
            .antisynergy("stairs")
            .infrequency(2));
        addGoal(obtainItemGoal(id("ender_pearl"), items, Items.ENDER_PEARL, 2, 3)
            .infrequency(2));
        addGoal(obtainItemGoal(id("egg"), items, Items.EGG, 16, 16));
        addGoal(BingoGoal.builder(id("4x4_paintings"))
            .criterion("paintings", AdjacentPaintingTrigger.builder()
                .placedPainting(PaintingPredicate.builder()
                    .width(MinMaxBounds.Ints.exactly(4))
                    .height(MinMaxBounds.Ints.exactly(4))
                    .build()
                )
                .adjacentPaintings(PaintingPredicate.builder()
                    .width(MinMaxBounds.Ints.exactly(4))
                    .height(MinMaxBounds.Ints.exactly(4))
                    .build()
                )
                .count(MinMaxBounds.Ints.atLeast(3))
                .build()
            )
            .name("4x4_paintings")
            .icon(new ItemStack(Items.PAINTING, 3))
            .antisynergy("painting")
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("bone_block"), items, Items.BONE_BLOCK, 5, 10));
        addGoal(BingoGoal.builder(id("double_creeper_boat"))
            .criterion("break", BingoTriggers.DESTROY_VEHICLE.get().createCriterion(new DestroyVehicleTrigger.TriggerInstance(
                Optional.empty(),
                Optional.of(ContextAwarePredicate.create(
                    LootItemEntityPropertyCondition.hasProperties(
                        LootContext.EntityTarget.THIS,
                        EntityPredicate.Builder.entity()
                            .of(entityTypes, EntityTypeTags.BOAT)
                            .build()
                    ).build(),
                    new PassengersCondition(List.of(
                        EntityPredicate.wrap(EntityPredicate.Builder.entity().of(entityTypes, EntityType.CREEPER)),
                        EntityPredicate.wrap(EntityPredicate.Builder.entity().of(entityTypes, EntityType.CREEPER))
                    ), false)
                )),
                Optional.empty()
            )))
            .name("double_creeper_boat")
            .icon(new CycleIcon(
                new EntityTypeTagCycleIcon(EntityTypeTags.BOAT, Items.OAK_BOAT.builtInRegistryHolder()),
                EntityIcon.ofSpawnEgg(EntityType.CREEPER, 2)
            ))
            .tags(BingoTags.ACTION, BingoTags.COMBAT, BingoTags.OVERWORLD)
        );
        addGoal(BingoGoal.builder(id("villager_trade"))
            .criterion("obtain", TradeTrigger.TriggerInstance.tradedWithVillager())
            .name("villager_trade")
            .icon(EntityIcon.ofSpawnEgg(EntityType.VILLAGER))
            .tags(BingoTags.VILLAGE, BingoTags.ACTION, BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("different_colored_shields"))
            .sub("count", BingoSub.random(2, 3))
            .criterion("obtain", DifferentColoredShieldsTrigger.builder(1).build(), subber -> subber.sub("conditions.min_count", "count"))
            .progress("obtain")
            .name(Component.translatable("bingo.goal.different_colored_shields", 0), subber -> subber.sub("with.0", "count"))
            .tooltip("different_colored_shields")
            .icon(CycleIcon.infer(Arrays.stream(DyeColor.values()).map(DifficultyGoalProvider::makeShieldWithColor)), subber -> {
                for (int i = 0; i < DyeColor.values().length; i++) {
                    subber.sub("icons." + i + ".item.count", "count");
                }
            })
            .tags(BingoTags.ITEM, BingoTags.COLOR, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("dead_bush"), items, Items.DEAD_BUSH)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME));
        addGoal(obtainItemGoal(id("grass"), items, Items.SHORT_GRASS, 15, 32) // FIXME: Support TALL_GRASS too
            .tooltip("grass"));

        for (String dyeColor : List.of("cyan", "magenta", "red", "orange", "yellow", "green", "pink", "purple", "lime")) {
            addGoal(obtainItemGoal(id(dyeColor + "_dye"), items, itemResource(dyeColor + "_dye"))
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
                Items.WATER_BUCKET.getName(),
                Items.LAVA_BUCKET.getName(),
                Items.MILK_BUCKET.getName()
            ))
            .icon(new CycleIcon(
                ItemIcon.ofItem(Items.WATER_BUCKET),
                ItemIcon.ofItem(Items.LAVA_BUCKET),
                ItemIcon.ofItem(Items.MILK_BUCKET)
            ))
            .antisynergy("bucket_types", "water_bucket", "lava_bucket", "milk_bucket")
            .reactant("use_buckets"));
        addGoal(obtainSomeItemsFromTag(id("different_flowers"), BingoItemTags.FLOWERS, "bingo.goal.different_flowers", 5, 7)
            .antisynergy("flowers")
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainSomeItemsFromTag(id("concrete"), ConventionalItemTags.CONCRETES, "bingo.goal.concrete", 3, 6)
            .antisynergy("concrete_color")
            .infrequency(4)
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD));
        addGoal(obtainSomeItemsFromTag(id("glazed_terracotta"), ConventionalItemTags.GLAZED_TERRACOTTAS, "bingo.goal.glazed_terracotta", 3, 6)
            .reactant("use_furnace")
            .antisynergy("glazed_terracotta_color")
            .infrequency(4)
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD));
        addGoal(bedRowGoal(id("bed_row"), 3, 6));
        addGoal(BingoGoal.builder(id("finish_at_spawn"))
            .criterion("nearby", CriteriaTriggers.LOCATION.createCriterion(
                new PlayerTrigger.TriggerInstance(Optional.of(ContextAwarePredicate.create(
                    new LocationCheck(
                        Optional.of(LocationPredicate.Builder.inDimension(Level.OVERWORLD).build()),
                        BlockPos.ZERO
                    ),
                    new DistanceFromSpawnCondition(
                        Optional.of(DistancePredicate.horizontal(MinMaxBounds.Doubles.atMost(3)))
                    ),
                    AnyOfCondition.anyOf(
                        LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity()
                            .equipment(EntityEquipmentPredicate.Builder.equipment()
                                .mainhand(ItemPredicate.Builder.item()
                                    .of(items, Items.COMPASS)
                                )
                            )
                        ),
                        LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity()
                            .equipment(EntityEquipmentPredicate.Builder.equipment()
                                .offhand(ItemPredicate.Builder.item()
                                    .of(items, Items.COMPASS)
                                )
                            )
                        )
                    ).build()
                )))
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD, BingoTags.FINISH)
            .name(Component.translatable("bingo.goal.finish_at_spawn", Items.COMPASS.getName()))
            .tooltip("finish_at_spawn")
            .icon(Items.COMPASS)
        );
        addGoal(obtainItemGoal(id("stone"), items, Items.STONE, 32, 64)
            .tooltip("stone")
            .infrequency(2)
            .reactant("use_furnace")
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("kill_passive_mobs_with_only_fire"))
            .sub("count", BingoSub.random(4, 8))
            .criterion("kill", EntityDieNearPlayerTrigger.builder()
                .entity(ContextAwarePredicate.create(
                    LootItemEntityPropertyCondition.hasProperties(
                        LootContext.EntityTarget.THIS,
                        EntityPredicate.Builder.entity().of(entityTypes, BingoEntityTypeTags.PASSIVE)
                    ).build(),
                    HasOnlyBeenDamagedByCondition.builder().damageTypeTag(DamageTypeTags.IS_FIRE).build()
                ))
                .killingBlow(DamagePredicate.Builder.damageInstance().type(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_FIRE))).build())
                .build())
            .requiredCount(new SubBingoSub("count"))
            .progress(GoalAchievedCountProgressTracker.INSTANCE)
            .name(Component.translatable("bingo.goal.kill_passive_mobs_with_only_fire", 0), subber -> subber.sub("with.0", "count"))
            .tooltip("kill_passive_mobs_with_only_fire")
            .icon(new CycleIcon(
                new EntityTypeTagCycleIcon(BingoEntityTypeTags.PASSIVE, 0),
                new BlockIcon(Blocks.FIRE.defaultBlockState(), new ItemStack(Items.FLINT_AND_STEEL, 1))
            ), subber -> subber.sub("icons.0.count", "count").sub("icons.1.item.count", "count"))
            .reactant("pacifist")
            .tags(BingoTags.ACTION, BingoTags.COMBAT));
        addGoal(BingoGoal.builder(id("kill_creeper_with_only_fire"))
            .criterion("kill", EntityDieNearPlayerTrigger.builder()
                .entity(ContextAwarePredicate.create(
                    LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().of(entityTypes, EntityType.CREEPER)).build(),
                    HasOnlyBeenDamagedByCondition.builder().damageTypeTag(DamageTypeTags.IS_FIRE).build()
                ))
                .killingBlow(DamagePredicate.Builder.damageInstance().type(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_FIRE))).build())
                .build()
            )
            .name("kill_creeper_with_only_fire")
            .icon(CycleIcon.infer(EntityType.CREEPER, Blocks.FIRE))
            .reactant("pacifist")
            .tags(BingoTags.ACTION, BingoTags.COMBAT, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("iron_nugget"), items, Items.IRON_NUGGET, 32, 64));
        addGoal(obtainItemGoal(id("gold_nugget"), items, Items.GOLD_NUGGET, 32, 64));
        addGoal(obtainItemGoal(id("rotten_flesh"), items, Items.ROTTEN_FLESH, 16, 32)
            .infrequency(2));
        addGoal(obtainItemGoal(id("redstone"), items, Items.REDSTONE, 16, 64)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("golden_carrot"), items, Items.GOLDEN_CARROT)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("common_mob_drops"))
            .criterion("rotten_flesh", InventoryChangeTrigger.TriggerInstance.hasItems(Items.ROTTEN_FLESH))
            .criterion("spider_eye", InventoryChangeTrigger.TriggerInstance.hasItems(Items.SPIDER_EYE))
            .criterion("bone", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BONE))
            .criterion("gunpowder", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GUNPOWDER))
            .criterion("ender_pearl", InventoryChangeTrigger.TriggerInstance.hasItems(Items.ENDER_PEARL))
            .progress(AchievedRequirementsProgressTracker.INSTANCE)
            .name(Component.translatable("bingo.goal.common_mob_drops",
                Items.ROTTEN_FLESH.getName(),
                Items.SPIDER_EYE.getName(),
                Items.BONE.getName(),
                Items.GUNPOWDER.getName(),
                Items.ENDER_PEARL.getName()))
            .icon(CycleIcon.infer(Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.BONE, Items.GUNPOWDER, Items.ENDER_PEARL))
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("feather"), items, Items.FEATHER, 32, 64)
            .infrequency(2));
        addGoal(obtainItemGoal(id("lily_pad"), items, Items.LILY_PAD, 2, 10)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME));
        addGoal(obtainItemGoal(id("stick"), items, Items.STICK, 65, 128)
            .infrequency(2));
        addGoal(BingoGoal.builder(id("wear_different_colored_armor"))
            .criterion("obtain", WearDifferentColoredArmorTrigger.builder(4).build())
            .progress("obtain")
            .name(Component.translatable("bingo.goal.wear_different_colored_armor", 4))
            .icon(CycleIcon.infer(
                Stream.concat(Stream.of((DyeColor) null), Arrays.stream(DyeColor.values()))
                    .flatMap(color -> Stream.of(Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS)
                        .map(item -> {
                            if (color == null) {
                                return new ItemStack(item, 4);
                            }
                            ItemStack stack = DyedItemColor.applyDyes(new ItemStack(item), List.of(DyeItem.byColor(color)));
                            stack.setCount(4);
                            return stack;
                        })
                    )
            ))
            .reactant("wear_armor")
            .tags(BingoTags.ITEM, BingoTags.COLOR, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("seagrass"), items, Items.SEAGRASS, 15, 32)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));

        for (String woodType : List.of("oak", "spruce", "birch", "dark_oak", "acacia", "cherry")) {
            Item planksItem = items.getOrThrow(ResourceKey.create(
                Registries.ITEM, ResourceLocations.minecraft(woodType + "_planks")
            )).value();
            addGoal(obtainItemGoal(id(woodType + "_planks"), items, planksItem, 65, 128)
                .tags(BingoTags.OVERWORLD)
                .infrequency(25));

            Item logItem = items.getOrThrow(ResourceKey.create(
                Registries.ITEM, ResourceLocations.minecraft(woodType + "_log")
            )).value();
            addGoal(obtainItemGoal(id(woodType + "_log"), items, logItem, 16, 32)
                .tags(BingoTags.OVERWORLD)
                .infrequency(25));

            Item woodItem = items.getOrThrow(ResourceKey.create(
                Registries.ITEM, ResourceLocations.minecraft(woodType + "_wood")
            )).value();
            addGoal(obtainItemGoal(id(woodType + "_wood"), items, woodItem, 11, 20)
                .infrequency(25)
                .tags(BingoTags.OVERWORLD));

            Item strippedWoodItem = items.getOrThrow(ResourceKey.create(
                Registries.ITEM, ResourceLocations.minecraft("stripped_" + woodType + "_wood")
            )).value();
            addGoal(obtainItemGoal(id("stripped_" + woodType + "_wood"), items, strippedWoodItem, 11, 20)
                .reactant("axe_use")
                .infrequency(25)
                .tags(BingoTags.OVERWORLD));

            Item strippedLogItem = items.getOrThrow(ResourceKey.create(
                Registries.ITEM, ResourceLocations.minecraft("stripped_" + woodType + "_log")
            )).value();
            addGoal(obtainItemGoal(id("stripped_" + woodType + "_log"), items, strippedLogItem, 16, 32)
                .reactant("axe_use")
                .infrequency(25)
                .tags(BingoTags.OVERWORLD));
        }

        addGoal(obtainItemGoal(id("tropical_fish"), items, Items.TROPICAL_FISH)
            .tags(BingoTags.OVERWORLD, BingoTags.OCEAN));
        addGoal(obtainItemGoal(id("pufferfish"), items, Items.PUFFERFISH)
            .tags(BingoTags.OVERWORLD, BingoTags.OCEAN));
        addGoal(obtainItemGoal(id("cod"), items, Items.COD, 6, 15)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD, BingoTags.OCEAN));
        addGoal(obtainItemGoal(id("salmon"), items, Items.SALMON, 4, 7)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD, BingoTags.OCEAN));
        addGoal(BingoGoal.builder(id("never_use_boat"))
            .criterion("use", CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.createCriterion(
                new PlayerInteractTrigger.TriggerInstance(
                    Optional.empty(), Optional.empty(),
                    Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity()
                        .entityType(EntityTypePredicate.of(entityTypes, ConventionalEntityTypeTags.BOATS))
                        .build()
                    ))
                )
            ))
            .tags(BingoTags.NEVER, BingoTags.OVERWORLD)
            .name("never_use_boat")
            .icon(Items.OAK_BOAT));
        addGoal(BingoGoal.builder(id("place_fish_in_nether"))
            .criterion("place", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(
                MatchTool.toolMatches(ItemPredicate.Builder.item().of(items, BingoItemTags.FISH_BUCKETS)),
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
        addGoal(obtainItemGoal(id("dried_kelp_block"), items, Items.DRIED_KELP_BLOCK, 11, 20)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("drown_zombie"))
            .criterion("drown", ZombieDrownedTrigger.builder().build())
            .name("drown_zombie")
            .tooltip("drown_zombie")
            .icon(CycleIcon.infer(EntityType.ZOMBIE, EntityType.DROWNED))
            .tags(BingoTags.ACTION, BingoTags.COMBAT, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("gunpowder"), items, Items.GUNPOWDER, 2, 5)
            .infrequency(2));
        addGoal(obtainItemGoal(id("spider_eye"), items, Items.SPIDER_EYE, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainSomeEdibleItems(id("edible_items"), 4, 5).tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("breed_mobs"))
            .sub("count", BingoSub.random(2, 4))
            .criterion(
                "breed",
                RelativeStatsTrigger.builder()
                    .stat(Stats.ANIMALS_BRED, MinMaxBounds.Ints.atLeast(0))
                    .build(),
                subber -> subber.sub("conditions.stats.0.value.min", "count")
            )
            .progress("breed")
            .name(
                Component.translatable("bingo.goal.breed_mobs", 0),
                subber -> subber.sub("with.0", "count")
            )
            .tooltip("breed_mobs")
            .icon(Items.WHEAT_SEEDS)
            .antisynergy("breed_animals")
            .infrequency(2)
            .tags(BingoTags.ACTION, BingoTags.STAT)
        );
        addGoal(crouchDistanceGoal(id("crouch_distance"), 100, 200));
        // TODO: never use debug
        addGoal(BingoGoal.builder(id("shoot_bell_from_10_blocks"))
            .criterion("shoot", ShootBellTrigger.builder()
                .distance(DistancePredicate.absolute(MinMaxBounds.Doubles.atLeast(10)))
                .build())
            .name("shoot_bell_from_10_blocks")
            .tooltip("shoot_bell_from_10_blocks")
            .icon(CycleIcon.infer(Items.ARROW, Items.BELL))
            .tags(BingoTags.ACTION, BingoTags.VILLAGE, BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("grindstone_repair"))
            .criterion("repair1", UseGrindstoneTrigger.builder().firstItem(ContextAwarePredicate.create(new ToolDamageCondition(MinMaxBounds.Ints.atLeast(1)))).build())
            .criterion("repair2", UseGrindstoneTrigger.builder().secondItem(ContextAwarePredicate.create(new ToolDamageCondition(MinMaxBounds.Ints.atLeast(1)))).build())
            .requirements(AdvancementRequirements.Strategy.OR)
            .name("grindstone_repair")
            .icon(CycleIcon.infer(Items.GRINDSTONE, ConventionalItemTags.TOOLS))
            .tags(BingoTags.ACTION));
        addGoal(obtainItemGoal(id("sweet_berries"), items, Items.SWEET_BERRIES, 2, 6)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME));
        addGoal(
            obtainItemGoal(
                id("banner_pattern"),
                items,
                new ItemTagCycleIcon(BingoItemTags.BANNER_PATTERNS),
                ItemPredicate.Builder.item().of(items, BingoItemTags.BANNER_PATTERNS)
            )
                .name("banner_pattern")
                .tags(BingoTags.OVERWORLD)
        );
        addGoal(BingoGoal.builder(id("drink_sus_stew"))
            .criterion("drink", ConsumeItemTrigger.TriggerInstance.usedItem(items, Items.SUSPICIOUS_STEW))
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD)
            .reactant("eat_non_meat")
            .name(Component.translatable("bingo.goal.drink_sus_stew", Items.SUSPICIOUS_STEW.getName()))
            .icon(Items.SUSPICIOUS_STEW));
        addGoal(BingoGoal.builder(id("give_fox_sword"))
            .criterion("pickup", CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.createCriterion(
                new PickedUpItemTrigger.TriggerInstance(
                    Optional.empty(),
                    Optional.of(ItemPredicate.Builder.item().of(items, ItemTags.SWORDS).build()),
                    Optional.of(ContextAwarePredicate.create(
                        LootItemEntityPropertyCondition.hasProperties(
                            LootContext.EntityTarget.THIS,
                            EntityPredicate.Builder.entity().of(entityTypes, EntityType.FOX)
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
        addGoal(obtainItemGoal(id("honey_bottle"), items, Items.HONEY_BOTTLE)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("honeycomb"), items, Items.HONEYCOMB, 3, 3)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("basalt"), items, Items.BASALT, 2, 6)
            .tags(BingoTags.NETHER));
        addGoal(obtainItemGoal(id("blackstone"), items, Items.BLACKSTONE, 2, 6)
            .tags(BingoTags.NETHER));
        {
            final List<CompoundTag> slots = IntStream.range(0, 4).mapToObj(slot -> BingoUtil.compound(Map.of(
                "id", StringTag.valueOf(Items.PORKCHOP.builtInRegistryHolder().key().location().toString()),
                "Slot", ByteTag.valueOf((byte) slot)
            ))).toList();
            addGoal(BingoGoal.builder(id("porkchops_in_soul_campfire"))
                .criterion("place", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                    LocationPredicate.Builder.location().setBlock(
                        BlockPredicate.Builder.block()
                            .of(blocks, Blocks.SOUL_CAMPFIRE)
                            .hasNbt(BingoUtil.compound(Map.of(
                                "Items", BingoUtil.list(slots)
                            )))
                    ),
                    ItemPredicate.Builder.item().of(items, Items.PORKCHOP)
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
        addGoal(obtainItemGoal(id("soul_lantern"), items, Items.SOUL_LANTERN)
            .tags(BingoTags.NETHER));
        addGoal(BingoGoal.builder(id("open_door_with_target_from_ten_blocks"))
            .criterion("open_door", DoorOpenedByTargetTrigger.builder()
                .projectile(EntityPredicate.Builder.entity().distance(DistancePredicate.horizontal(MinMaxBounds.Doubles.atLeast(10))))
                .door(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(blocks, BlockTags.DOORS)).build())
                .build()
            )
            .name("open_door_with_target_from_ten_blocks")
            .tooltip("open_door_with_target_from_ten_blocks")
            .icon(CycleIcon.infer(Items.ARROW, new ItemStack(Items.TARGET, 10), Items.OAK_DOOR))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
        );
        addGoal(obtainItemGoal(id("carrot_on_a_stick"), items, Items.CARROT_ON_A_STICK)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("barter_with_piglin"))
            .criterion("barter", CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.createCriterion(new PickedUpItemTrigger.TriggerInstance(
                Optional.empty(),
                Optional.of(ItemPredicate.Builder.item().of(items, PiglinAi.BARTERING_ITEM).build()),
                Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(entityTypes, EntityType.PIGLIN).flags(EntityFlagsPredicate.Builder.flags().setIsBaby(false)))))))
            .criterion("barter_directly", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
                ItemPredicate.Builder.item().of(items, PiglinAi.BARTERING_ITEM),
                Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(entityTypes, EntityType.PIGLIN).flags(EntityFlagsPredicate.Builder.flags().setIsBaby(false))))))
            .requirements(AdvancementRequirements.Strategy.OR)
            .name("barter_with_piglin")
            .icon(EntityIcon.ofSpawnEgg(EntityType.PIGLIN))
            .tags(BingoTags.ACTION, BingoTags.NETHER));
        addGoal(BingoGoal.builder(id("nausea"))
            .criterion("obtain", EffectsChangedTrigger.TriggerInstance.hasEffects(MobEffectsPredicate.Builder.effects().and(MobEffects.NAUSEA)))
            .name("nausea")
            .icon(EffectIcon.of(MobEffects.NAUSEA))
            .reactant("eat_meat")
            .tags(BingoTags.ITEM, BingoTags.OCEAN, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(
                id("obtain_enchanted_item"),
                items,
                Items.ENCHANTED_BOOK,
                ItemPredicate.Builder.item().withComponents(
                    DataComponentMatchers.Builder.components()
                        .partial(DataComponentPredicates.ENCHANTMENTS, createAnyEnchantmentsRequirement())
                        .build()
                ),
                ItemPredicate.Builder.item().withComponents(
                    DataComponentMatchers.Builder.components()
                        .partial(DataComponentPredicates.STORED_ENCHANTMENTS, createAnyStoredEnchantmentsRequirement())
                        .build()
                )
            )
            .name("obtain_enchanted_item")
            .tooltip("obtain_enchanted_item")
            .antisynergy("enchant")
        );
        addGoal(BingoGoal.builder(id("disenchant"))
            .criterion("disenchant1", UseGrindstoneTrigger.builder().firstItem(ContextAwarePredicate.create(new ToolIsEnchantedCondition(true))).build())
            .criterion("disenchant2", UseGrindstoneTrigger.builder().secondItem(ContextAwarePredicate.create(new ToolIsEnchantedCondition(true))).build())
            .requirements(AdvancementRequirements.Strategy.OR)
            .name("disenchant")
            .icon(CycleIcon.infer(Items.GRINDSTONE, Items.ENCHANTED_BOOK))
            .tags(BingoTags.ACTION));
        // TODO: never use sword
        addGoal(BingoGoal.builder(id("carnivore"))
            .criterion("not_meat", ConsumeItemTrigger.TriggerInstance.usedItem(
                ItemPredicate.Builder.item()
                    .of(items, BingoItemTags.NOT_MEAT)
            ))
            .tags(BingoTags.NEVER, BingoTags.ACTION)
            .antisynergy("food")
            .catalyst("eat_non_meat")
            .name("carnivore")
            .tooltip("carnivore")
            .icon(new ItemTagCycleIcon(ItemTags.MEAT))
        );
        // TODO: clean banner
        addGoal(obtainSomeItemsFromTag(id("gold_in_name"), BingoItemTags.GOLD_IN_NAME, "bingo.goal.gold_in_name", 5, 7)
            .tooltip("gold_in_name")
            .antisynergy("gold_items"));
        addGoal(obtainItemGoal(id("sand"), items, Items.SAND, 33, 64)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("sandstone"), items, Items.SANDSTONE, 11, 32)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("cut_sandstone"), items, Items.CUT_SANDSTONE, 11, 32)
            .setAntisynergy("sandstone")
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("paper"), items, Items.PAPER, 6, 15)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("coal_block"), items, Items.COAL_BLOCK, 3, 6)
            .reactant("never_coal"));
        addGoal(obtainItemGoal(id("apple"), items, Items.APPLE, 2, 5)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("tame_horse"))
            .criterion("obtain", TameAnimalTrigger.TriggerInstance.tamedAnimal(
                EntityPredicate.Builder.entity().of(entityTypes, EntityType.HORSE)
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
        addGoal(BingoGoal.builder(id("burn_village_house"))
            .criterion("burn", CriteriaTriggers.ITEM_USED_ON_BLOCK.createCriterion(
                new ItemUsedOnLocationTrigger.TriggerInstance(
                    Optional.empty(),
                    Optional.of(ContextAwarePredicate.create(
                        MatchTool.toolMatches(ItemPredicate.Builder.item().of(items, ItemTags.CREEPER_IGNITERS)).build(),
                        FlammableCondition.INSTANCE,
                        new InStructureCondition(StructureTags.VILLAGE)
                    ))
                )
            ))
            .name("burn_village_house")
            .tags(BingoTags.ACTION, BingoTags.VILLAGE, BingoTags.OVERWORLD)
            .icon(CycleIcon.infer(
                new BlockIcon(Blocks.FIRE.defaultBlockState(), new ItemStack(Items.FLINT_AND_STEEL)),
                EntityType.VILLAGER
            ))
        );
        addGoal(obtainItemGoal(id("emerald"), items, Items.EMERALD)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("milk_cure"))
            .criterion("cure", CriteriaTriggers.CONSUME_ITEM.createCriterion(
                new ConsumeItemTrigger.TriggerInstance(
                    Optional.of(ContextAwarePredicate.create(
                        new HasAnyEffectCondition(LootContext.EntityTarget.THIS)
                    )),
                    Optional.of(ItemPredicate.Builder.item()
                        .of(items, Items.MILK_BUCKET)
                        .build()
                    )
                )
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name("milk_cure")
            .icon(Items.MILK_BUCKET)
        );
        addGoal(
            obtainItemGoal(
                id("pottery_sherd"),
                items,
                new ItemTagCycleIcon(ItemTags.DECORATED_POT_SHERDS),
                ItemPredicate.Builder.item().of(items, ItemTags.DECORATED_POT_SHERDS)
            )
            .tags(BingoTags.OVERWORLD)
            .name("pottery_sherd"));
        final KeyMapping walkBackwards = Minecraft.getInstance().options.keyDown;
        addGoal(BingoGoal.builder(id("never_walk_backwards"))
            .criterion("walk", CriteriaTriggers.TICK.createCriterion(new PlayerTrigger.TriggerInstance(
                Optional.of(EntityPredicate.wrap(
                    EntityPredicate.Builder.entity()
                        .subPredicate(PlayerPredicate.Builder.player()
                            .hasInput(new InputPredicate(
                                Optional.empty(), // forward
                                Optional.of(true), // backward
                                Optional.empty(), // left
                                Optional.empty(), // right
                                Optional.empty(), // jump
                                Optional.empty(), // sneak
                                Optional.empty() // sprint
                            ))
                            .build()
                        )
                ))
            )))
            .tags(BingoTags.ACTION, BingoTags.NEVER)
            .name(Component.translatable(
                "bingo.goal.never_walk_backwards",
                Component.keybind(walkBackwards.getName())
            ))
            .infrequency(4)
            .icon(Items.DIRT_PATH)
        );

        addGoal(BingoGoal.builder(id("brush_armadillo"))
            .criterion("brush", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
                ItemPredicate.Builder.item().of(items, Items.BRUSH),
                Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(entityTypes, EntityType.ARMADILLO)))
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name("brush_armadillo")
            .icon(IndicatorIcon.infer(EntityType.ARMADILLO, Items.BRUSH)));

        addGoal(obtainItemGoal(id("crafter"), items, Items.CRAFTER)
            .tags(BingoTags.OVERWORLD));

        addGoal(BingoGoal.builder(id("fill_bundle"))
            .criterion("fill", FillBundleTrigger.builder().build())
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD)
            .name("fill_bundle")
            .icon(Items.BUNDLE));
    }

    private GoalBuilder eatEntireCake() {
        GoalBuilder builder = BingoGoal.builder(id("eat_entire_cake"));
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
            builder.criterion("level_" + level, CriteriaTriggers.DEFAULT_BLOCK_USE.createCriterion(
                new DefaultBlockInteractionTrigger.TriggerInstance(Optional.empty(), Optional.of(location))
            ));
        }
        return builder
            .progress(AchievedRequirementsProgressTracker.INSTANCE)
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name("eat_entire_cake")
            .icon(Items.CAKE)
            .reactant("eat_non_meat");
    }
}
