package io.github.gaming32.bingo.datagen.goal;

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
import io.github.gaming32.bingo.data.icons.GoalIcon;
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
import io.github.gaming32.bingo.util.Identifiers;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEntityTypeTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.advancements.criterion.ConsumeItemTrigger;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.DamagePredicate;
import net.minecraft.advancements.criterion.DamageSourcePredicate;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.DefaultBlockInteractionTrigger;
import net.minecraft.advancements.criterion.DistancePredicate;
import net.minecraft.advancements.criterion.EffectsChangedTrigger;
import net.minecraft.advancements.criterion.EntityEquipmentPredicate;
import net.minecraft.advancements.criterion.EntityFlagsPredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.EntityTypePredicate;
import net.minecraft.advancements.criterion.FishingRodHookedTrigger;
import net.minecraft.advancements.criterion.InputPredicate;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.MobEffectsPredicate;
import net.minecraft.advancements.criterion.PickedUpItemTrigger;
import net.minecraft.advancements.criterion.PlayerInteractTrigger;
import net.minecraft.advancements.criterion.PlayerPredicate;
import net.minecraft.advancements.criterion.PlayerTrigger;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.advancements.criterion.SummonedEntityTrigger;
import net.minecraft.advancements.criterion.TagPredicate;
import net.minecraft.advancements.criterion.TameAnimalTrigger;
import net.minecraft.advancements.criterion.TradeTrigger;
import net.minecraft.advancements.criterion.UsingItemTrigger;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.entity.animal.chicken.ChickenVariant;
import net.minecraft.world.entity.animal.chicken.ChickenVariants;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
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

import static io.github.gaming32.bingo.datagen.goal.GoalIds.Easy.*;

public class EasyGoalProvider extends DifficultyGoalProvider {
    public EasyGoalProvider(BiConsumer<Identifier, BingoGoal> goalAdder, HolderLookup.Provider registries) {
        super(BingoDifficulties.EASY, goalAdder, registries);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void addGoals() {
        final var entityTypes = registries.lookupOrThrow(Registries.ENTITY_TYPE);
        final var blocks = registries.lookupOrThrow(Registries.BLOCK);
        final var items = registries.lookupOrThrow(Registries.ITEM);

        addGoal(BingoGoal.builder(DIFFERENT_FISH)
            .sub("count", BingoSub.random(2, 4))
            .criterion("obtain",
                HasSomeFoodItemsTrigger.builder().requiredCount(1).tag(ItemTags.FISHES).build(),
                subber -> subber.sub("conditions.required_count", "count"))
            .progress("obtain")
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.different_fish", 0), subber -> subber.sub("with.0", "count"))
            .tooltip("different_fish")
            .icon(new ItemTagCycleIcon(ItemTags.FISHES), subber -> subber.sub("+count", "count")));
        addGoal(BingoGoal.builder(GROW_TREE_IN_NETHER)
            .criterion("grow", GrowFeatureTrigger.builder()
                .feature(BingoFeatureTags.TREES)
                .location(LocationPredicate.Builder.inDimension(Level.NETHER).build())
                .build()
            )
            .tags(BingoTags.ACTION, BingoTags.NETHER, BingoTags.OVERWORLD)
            .name("grow_tree_in_nether")
            .tooltip("grow_tree_in_nether")
            .icon(CycleIcon.infer(Items.BONE_MEAL, Items.OAK_SAPLING)));
        addGoal(obtainSomeItemsFromTag(TERRACOTTA, ItemTags.TERRACOTTA, "bingo.goal.colors_of_terracotta", 4, 7)
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD)
            .reactant("use_furnace")
            .antisynergy("terracotta_color")
            .infrequency(4)
        );
        addGoal(obtainItemGoal(MUSHROOM_STEW, items, Items.MUSHROOM_STEW, 2, 5));
        addGoal(BingoGoal.builder(SHOOT_BUTTON)
            .criterion("obtain", ArrowPressTrigger.builder()
                .arrow(EntityPredicate.Builder.entity().of(entityTypes, EntityTypeTags.ARROWS).build())
                .buttonOrPlate(BlockPredicate.Builder.block().of(blocks, BlockTags.BUTTONS).build())
                .build()
            )
            .tags(BingoTags.ACTION)
            .name("shoot_button")
            .icon(Items.OAK_BUTTON));
        addGoal(obtainItemGoal(WRITABLE_BOOK, items, Items.WRITABLE_BOOK)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(FLINT, items, Items.FLINT, 8, 16));
        addGoal(eatEntireCake());
        addGoal(obtainItemGoal(PUMPKIN_PIE, items, Items.PUMPKIN_PIE)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(FISH_TREASURE_JUNK)
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
        addGoal(obtainItemGoal(COARSE_DIRT, items, Items.COARSE_DIRT, 16, 64)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(CLOCK, items, Items.CLOCK, 2, 3)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(IRON_BLOCK, items, Items.IRON_BLOCK, 2, 4)
            .infrequency(2));
        addGoal(obtainItemGoal(GOLD_BLOCK, items, Items.GOLD_BLOCK)
            .infrequency(2));
        addGoal(obtainItemGoal(GOLDEN_APPLE, items, Items.GOLDEN_APPLE));
        addGoal(obtainItemGoal(BOOKSHELF, items, Items.BOOKSHELF, 2, 4)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(NEVER_WEAR_CHESTPLATES)
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
        addGoal(BingoGoal.builder(NEVER_USE_SHIELDS)
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
        addGoal(obtainItemGoal(JUKEBOX, items, Items.JUKEBOX));
        addGoal(BingoGoal.builder(_3X3X3_GLASS_CUBE)
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
                new ItemIcon(new ItemStackTemplate(Items.GLASS, 26)),
                ItemIcon.ofItem(Items.LAVA_BUCKET)
            ))
            .tags(BingoTags.BUILD, BingoTags.OVERWORLD)
        );
        addGoal(obtainItemGoal(MOSSY_COBBLESTONE, items, Items.MOSSY_COBBLESTONE, 16, 32)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(CACTUS, items, Items.CACTUS, 5, 15)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(TNT, items, Items.TNT, 2, 3)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainLevelsGoal(LEVELS, 8, 15));
        addGoal(BingoGoal.builder(CREATE_SNOW_GOLEM)
            .criterion("summon", SummonedEntityTrigger.TriggerInstance.summonedEntity(
                EntityPredicate.Builder.entity().of(entityTypes, EntityType.SNOW_GOLEM)
            ))
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.create_snow_golem", EntityType.SNOW_GOLEM.getDescription()))
            .icon(EntityIcon.ofSpawnEgg(EntityType.SNOW_GOLEM))
        );
        addGoal(obtainItemGoal(NOTE_BLOCK, items, Items.NOTE_BLOCK, 5, 15)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(INK_SAC, items, Items.INK_SAC, 5, 15)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(BREAD, items, Items.BREAD, 6, 10)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(HAY_BLOCK, items, Items.HAY_BLOCK, 2, 3)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainSomeItemsFromTag(WOOL_COLORS, ItemTags.WOOL, "bingo.goal.wool_colors", 5, 8)
            .antisynergy("wool_color")
            .infrequency(4)
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(PISTON, items, Items.PISTON)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(FULL_IRON_ARMOR)
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
        addGoal(BingoGoal.builder(FULL_LEATHER_ARMOR)
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
        addGoal(BingoGoal.builder(FILL_WATER_CAULDRON)
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
        addGoal(BingoGoal.builder(COMPLETE_MAP)
            .criterion("complete", CompleteMapTrigger.TriggerInstance.completeMap())
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name("complete_map")
            .icon(Items.FILLED_MAP)
            .antisynergy("complete_map"));
        addGoal(obtainItemGoal(SOUL_SAND, items, Items.SOUL_SAND, 5, 10)
            .tags(BingoTags.NETHER));
        addGoal(obtainItemGoal(SOUL_SOIL, items, Items.SOUL_SOIL, 5, 10)
            .tags(BingoTags.NETHER));
        addGoal(obtainItemGoal(PUMPKIN, items, Items.PUMPKIN, 5, 10)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(CARVED_PUMPKIN, items, Items.CARVED_PUMPKIN, 2, 5)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(VINE, items, Items.VINE, 10, 30)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME));
        addGoal(obtainSomeItemsFromTag(DIFFERENT_SLABS, ItemTags.SLABS, "bingo.goal.different_slabs", 5, 7)
            .antisynergy("slabs")
            .infrequency(2));
        addGoal(BingoGoal.builder(ALMOST_EVERY_SWORD)
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
        addGoal(BingoGoal.builder(ALMOST_EVERY_PICKAXE)
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
        addGoal(obtainItemGoal(BRICKS, items, Items.BRICKS, 16, 64)
            .reactant("use_furnace")
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(NETHER_BRICKS, items, Items.NETHER_BRICKS, 16, 32)
            .reactant("use_furnace")
            .tags(BingoTags.NETHER));
        addGoal(obtainItemGoal(ARROW, items, Items.ARROW, 8, 16));
        addGoal(BingoGoal.builder(SLEEP_IN_NETHER)
            .criterion("sleep", IntentionalGameDesignTrigger.TriggerInstance.clicked(
                LocationPredicate.Builder.inDimension(Level.NETHER).build()
            ))
            .tags(BingoTags.ACTION, BingoTags.NETHER, BingoTags.OVERWORLD)
            .name("sleep_in_nether")
            .icon(Items.PURPLE_BED)
        );
        addGoal(obtainItemGoal(FERMENTED_SPIDER_EYE, items, Items.FERMENTED_SPIDER_EYE)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainSomeItemsFromTag(DIFFERENT_STAIRS, ItemTags.STAIRS, "bingo.goal.different_stairs", 5, 7)
            .antisynergy("stairs")
            .infrequency(2));
        addGoal(obtainItemGoal(ENDER_PEARL, items, Items.ENDER_PEARL, 2, 3)
            .infrequency(2));
        addGoal(
            obtainItemGoal(
                EGG,
                items,
                new ItemTagCycleIcon(ItemTags.EGGS, 3),
                ItemPredicate.Builder.item().of(items, ItemTags.EGGS).withCount(MinMaxBounds.Ints.exactly(3))
            )
                .name(Component.translatable("bingo.count", 3, Component.translatable(Items.EGG.getDescriptionId())))
                .tooltip("egg")
                .tags(BingoTags.OVERWORLD)
        );
        addGoal(BingoGoal.builder(_4X4_PAINTINGS)
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
            .icon(new ItemStackTemplate(Items.PAINTING, 3))
            .antisynergy("painting")
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(BONE_BLOCK, items, Items.BONE_BLOCK, 5, 10));
        addGoal(BingoGoal.builder(DOUBLE_CREEPER_BOAT)
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
        addGoal(BingoGoal.builder(VILLAGER_TRADE)
            .criterion("obtain", TradeTrigger.TriggerInstance.tradedWithVillager())
            .name("villager_trade")
            .icon(EntityIcon.ofSpawnEgg(EntityType.VILLAGER))
            .tags(BingoTags.VILLAGE, BingoTags.ACTION, BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(DIFFERENT_COLORED_SHIELDS)
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
        addGoal(obtainItemGoal(DEAD_BUSH, items, Items.DEAD_BUSH)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME));
        addGoal(obtainItemGoal(GRASS, items, Items.SHORT_GRASS, 15, 32) // FIXME: Support TALL_GRASS too
            .tooltip("grass"));

        for (String dyeColor : List.of("cyan", "magenta", "red", "orange", "yellow", "green", "pink", "purple", "lime")) {
            addGoal(obtainItemGoal(id(dyeColor + "_dye"), items, itemResource(dyeColor + "_dye"))
                .infrequency(10)
                .tags(BingoTags.OVERWORLD));
        }

        addGoal(BingoGoal.builder(NEVER_SLEEP)
            .criterion("sleep", PlayerTrigger.TriggerInstance.sleptInBed())
            .tags(BingoTags.NEVER, BingoTags.OVERWORLD)
            .name("never_sleep")
            .icon(Items.RED_BED)
            .catalyst("sleep"));
        addGoal(BingoGoal.builder(GROW_HUGE_MUSHROOM)
            .criterion("grow", GrowFeatureTrigger.builder().feature(BingoFeatureTags.HUGE_MUSHROOMS).build())
            .tags(BingoTags.ACTION)
            .name("grow_huge_mushroom")
            .icon(Blocks.RED_MUSHROOM_BLOCK));
        addGoal(BingoGoal.builder(WATER_LAVA_MILK)
            .criterion("buckets", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.WATER_BUCKET, Items.LAVA_BUCKET, Items.MILK_BUCKET
            ))
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD)
            .name(Component.translatable(
                "bingo.three",
                Component.translatable(Items.WATER_BUCKET.getDescriptionId()),
                Component.translatable(Items.LAVA_BUCKET.getDescriptionId()),
                Component.translatable(Items.MILK_BUCKET.getDescriptionId())
            ))
            .icon(new CycleIcon(
                ItemIcon.ofItem(Items.WATER_BUCKET),
                ItemIcon.ofItem(Items.LAVA_BUCKET),
                ItemIcon.ofItem(Items.MILK_BUCKET)
            ))
            .antisynergy("bucket_types", "water_bucket", "lava_bucket", "milk_bucket")
            .reactant("use_buckets"));
        addGoal(obtainSomeItemsFromTag(DIFFERENT_FLOWERS, ConventionalItemTags.FLOWERS, "bingo.goal.different_flowers", 5, 7)
            .antisynergy("flowers")
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainSomeItemsFromTag(CONCRETE, ConventionalItemTags.CONCRETES, "bingo.goal.concrete", 3, 6)
            .antisynergy("concrete_color")
            .infrequency(4)
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD));
        addGoal(obtainSomeItemsFromTag(GLAZED_TERRACOTTA, ConventionalItemTags.GLAZED_TERRACOTTAS, "bingo.goal.glazed_terracotta", 3, 6)
            .reactant("use_furnace")
            .antisynergy("glazed_terracotta_color")
            .infrequency(4)
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD));
        addGoal(bedRowGoal(BED_ROW, 3, 6));
        addGoal(BingoGoal.builder(FINISH_AT_SPAWN)
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
            .name(Component.translatable("bingo.goal.finish_at_spawn", Component.translatable(Items.COMPASS.getDescriptionId())))
            .tooltip("finish_at_spawn")
            .icon(Items.COMPASS)
        );
        addGoal(obtainItemGoal(STONE, items, Items.STONE, 32, 64)
            .tooltip("stone")
            .infrequency(2)
            .reactant("use_furnace")
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(KILL_PASSIVE_MOBS_WITH_ONLY_FIRE)
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
                new EntityTypeTagCycleIcon(BingoEntityTypeTags.PASSIVE, 2),
                new BlockIcon(Blocks.FIRE.defaultBlockState(), new ItemStackTemplate(Items.FLINT_AND_STEEL))
            ), subber -> subber.sub("icons.0.count", "count").sub("icons.1.item.count", "count"))
            .reactant("pacifist")
            .tags(BingoTags.ACTION, BingoTags.COMBAT));
        addGoal(BingoGoal.builder(KILL_CREEPER_WITH_ONLY_FIRE)
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
        addGoal(obtainItemGoal(IRON_NUGGET, items, Items.IRON_NUGGET, 32, 64));
        addGoal(obtainItemGoal(GOLD_NUGGET, items, Items.GOLD_NUGGET, 32, 64));
        addGoal(obtainItemGoal(ROTTEN_FLESH, items, Items.ROTTEN_FLESH, 16, 32)
            .infrequency(2));
        addGoal(obtainItemGoal(REDSTONE, items, Items.REDSTONE, 16, 64)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(GOLDEN_CARROT, items, Items.GOLDEN_CARROT)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(COMMON_MOB_DROPS)
            .criterion("rotten_flesh", InventoryChangeTrigger.TriggerInstance.hasItems(Items.ROTTEN_FLESH))
            .criterion("spider_eye", InventoryChangeTrigger.TriggerInstance.hasItems(Items.SPIDER_EYE))
            .criterion("bone", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BONE))
            .criterion("gunpowder", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GUNPOWDER))
            .criterion("ender_pearl", InventoryChangeTrigger.TriggerInstance.hasItems(Items.ENDER_PEARL))
            .progress(AchievedRequirementsProgressTracker.INSTANCE)
            .name(Component.translatable("bingo.goal.common_mob_drops",
                Component.translatable(Items.ROTTEN_FLESH.getDescriptionId()),
                Component.translatable(Items.SPIDER_EYE.getDescriptionId()),
                Component.translatable(Items.BONE.getDescriptionId()),
                Component.translatable(Items.GUNPOWDER.getDescriptionId()),
                Component.translatable(Items.ENDER_PEARL.getDescriptionId())))
            .icon(CycleIcon.infer(Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.BONE, Items.GUNPOWDER, Items.ENDER_PEARL))
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(FEATHER, items, Items.FEATHER, 32, 64)
            .infrequency(2));
        addGoal(obtainItemGoal(LILY_PAD, items, Items.LILY_PAD, 2, 10)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME));
        addGoal(obtainItemGoal(STICK, items, Items.STICK, 65, 128)
            .infrequency(2));
        addGoal(BingoGoal.builder(WEAR_DIFFERENT_COLORED_ARMOR)
            .criterion("obtain", WearDifferentColoredArmorTrigger.builder(4).build())
            .progress("obtain")
            .name(Component.translatable("bingo.goal.wear_different_colored_armor", 4))
            .icon(CycleIcon.infer(
                Stream.concat(Stream.of((DyeColor) null), Arrays.stream(DyeColor.values()))
                    .flatMap(color -> Stream.of(Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS)
                        .map(item -> {
                            if (color == null) {
                                return new ItemStackTemplate(item, 4);
                            }

                            DyedItemColor itemColor = DyedItemColor.applyDyes((DyedItemColor) null, List.of(color));
                            return new ItemStackTemplate(item.builtInRegistryHolder(), 4, DataComponentPatch.builder().set(DataComponents.DYED_COLOR, itemColor).build());
                        })
                    )
            ))
            .reactant("wear_armor")
            .tags(BingoTags.ITEM, BingoTags.COLOR, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(SEAGRASS, items, Items.SEAGRASS, 15, 32)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));

        for (String woodType : List.of("oak", "spruce", "birch", "dark_oak", "acacia", "cherry")) {
            Item planksItem = items.getOrThrow(ResourceKey.create(
                Registries.ITEM, Identifiers.minecraft(woodType + "_planks")
            )).value();
            addGoal(obtainItemGoal(id(woodType + "_planks"), items, planksItem, 65, 128)
                .tags(BingoTags.OVERWORLD)
                .infrequency(25));

            Item logItem = items.getOrThrow(ResourceKey.create(
                Registries.ITEM, Identifiers.minecraft(woodType + "_log")
            )).value();
            addGoal(obtainItemGoal(id(woodType + "_log"), items, logItem, 16, 32)
                .tags(BingoTags.OVERWORLD)
                .infrequency(25));

            Item woodItem = items.getOrThrow(ResourceKey.create(
                Registries.ITEM, Identifiers.minecraft(woodType + "_wood")
            )).value();
            addGoal(obtainItemGoal(id(woodType + "_wood"), items, woodItem, 11, 20)
                .infrequency(25)
                .tags(BingoTags.OVERWORLD));

            Item strippedWoodItem = items.getOrThrow(ResourceKey.create(
                Registries.ITEM, Identifiers.minecraft("stripped_" + woodType + "_wood")
            )).value();
            addGoal(obtainItemGoal(id("stripped_" + woodType + "_wood"), items, strippedWoodItem, 11, 20)
                .reactant("axe_use")
                .infrequency(25)
                .tags(BingoTags.OVERWORLD));

            Item strippedLogItem = items.getOrThrow(ResourceKey.create(
                Registries.ITEM, Identifiers.minecraft("stripped_" + woodType + "_log")
            )).value();
            addGoal(obtainItemGoal(id("stripped_" + woodType + "_log"), items, strippedLogItem, 16, 32)
                .reactant("axe_use")
                .infrequency(25)
                .tags(BingoTags.OVERWORLD));
        }

        addGoal(obtainItemGoal(TROPICAL_FISH, items, Items.TROPICAL_FISH)
            .tags(BingoTags.OVERWORLD, BingoTags.OCEAN));
        addGoal(obtainItemGoal(PUFFERFISH, items, Items.PUFFERFISH)
            .tags(BingoTags.OVERWORLD, BingoTags.OCEAN));
        addGoal(obtainItemGoal(COD, items, Items.COD, 6, 15)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD, BingoTags.OCEAN));
        addGoal(obtainItemGoal(SALMON, items, Items.SALMON, 4, 7)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD, BingoTags.OCEAN));
        addGoal(BingoGoal.builder(NEVER_USE_BOAT)
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
        addGoal(BingoGoal.builder(PLACE_FISH_IN_NETHER)
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
        addGoal(obtainItemGoal(DRIED_KELP_BLOCK, items, Items.DRIED_KELP_BLOCK, 11, 20)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(DROWN_ZOMBIE)
            .criterion("drown", ZombieDrownedTrigger.builder().build())
            .name("drown_zombie")
            .tooltip("drown_zombie")
            .icon(CycleIcon.infer(EntityType.ZOMBIE, EntityType.DROWNED))
            .tags(BingoTags.ACTION, BingoTags.COMBAT, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(GUNPOWDER, items, Items.GUNPOWDER, 2, 5)
            .infrequency(2));
        addGoal(obtainItemGoal(SPIDER_EYE, items, Items.SPIDER_EYE, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainSomeEdibleItems(EDIBLE_ITEMS, 4, 5).tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(BREED_MOBS)
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
        addGoal(crouchDistanceGoal(CROUCH_DISTANCE, 100, 200));
        // TODO: never use debug
        addGoal(BingoGoal.builder(SHOOT_BELL_FROM_10_BLOCKS)
            .criterion("shoot", ShootBellTrigger.builder()
                .distance(DistancePredicate.absolute(MinMaxBounds.Doubles.atLeast(10)))
                .build())
            .name("shoot_bell_from_10_blocks")
            .tooltip("shoot_bell_from_10_blocks")
            .icon(CycleIcon.infer(Items.ARROW, Items.BELL))
            .tags(BingoTags.ACTION, BingoTags.VILLAGE, BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(GRINDSTONE_REPAIR)
            .criterion("repair1", UseGrindstoneTrigger.builder().firstItem(ContextAwarePredicate.create(new ToolDamageCondition(MinMaxBounds.Ints.atLeast(1)))).build())
            .criterion("repair2", UseGrindstoneTrigger.builder().secondItem(ContextAwarePredicate.create(new ToolDamageCondition(MinMaxBounds.Ints.atLeast(1)))).build())
            .requirements(AdvancementRequirements.Strategy.OR)
            .name("grindstone_repair")
            .icon(CycleIcon.infer(Items.GRINDSTONE, ConventionalItemTags.TOOLS))
            .tags(BingoTags.ACTION));
        addGoal(obtainItemGoal(SWEET_BERRIES, items, Items.SWEET_BERRIES, 2, 6)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME));
        addGoal(
            obtainItemGoal(
                BANNER_PATTERN,
                items,
                new ItemTagCycleIcon(BingoItemTags.BANNER_PATTERNS),
                ItemPredicate.Builder.item().of(items, BingoItemTags.BANNER_PATTERNS)
            )
                .name("banner_pattern")
                .tags(BingoTags.OVERWORLD)
        );
        addGoal(BingoGoal.builder(DRINK_SUS_STEW)
            .criterion("drink", ConsumeItemTrigger.TriggerInstance.usedItem(items, Items.SUSPICIOUS_STEW))
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD)
            .reactant("eat_non_meat")
            .name(Component.translatable("bingo.goal.drink_sus_stew", Component.translatable(Items.SUSPICIOUS_STEW.getDescriptionId())))
            .icon(Items.SUSPICIOUS_STEW));
        addGoal(BingoGoal.builder(GIVE_FOX_SWORD)
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
        addGoal(obtainItemGoal(HONEY_BOTTLE, items, Items.HONEY_BOTTLE)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(HONEYCOMB, items, Items.HONEYCOMB, 3, 3)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(BASALT, items, Items.BASALT, 2, 6)
            .tags(BingoTags.NETHER));
        addGoal(obtainItemGoal(BLACKSTONE, items, Items.BLACKSTONE, 2, 6)
            .tags(BingoTags.NETHER));
        {
            final List<CompoundTag> slots = IntStream.range(0, 4).mapToObj(slot -> BingoUtil.compound(Map.of(
                "id", StringTag.valueOf(Items.PORKCHOP.builtInRegistryHolder().key().identifier().toString()),
                "Slot", ByteTag.valueOf((byte) slot)
            ))).toList();
            addGoal(BingoGoal.builder(PORKCHOPS_IN_SOUL_CAMPFIRE)
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
                    new ItemIcon(new ItemStackTemplate(Items.PORKCHOP, 4))
                ))
                .reactant("pacifist")
                .tags(BingoTags.ACTION, BingoTags.NETHER, BingoTags.COMBAT)
            );
        }
        addGoal(obtainItemGoal(SOUL_LANTERN, items, Items.SOUL_LANTERN)
            .tags(BingoTags.NETHER)
            .setAntisynergy("lantern"));
        addGoal(obtainItemGoal(COPPER_LANTERN, items, Items.COPPER_LANTERN.unaffected())
            .tags(BingoTags.OVERWORLD)
            .setAntisynergy("lantern"));
        addGoal(BingoGoal.builder(OPEN_DOOR_WITH_TARGET_FROM_TEN_BLOCKS)
            .criterion("open_door", DoorOpenedByTargetTrigger.builder()
                .projectile(EntityPredicate.Builder.entity().distance(DistancePredicate.horizontal(MinMaxBounds.Doubles.atLeast(10))))
                .door(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(blocks, BlockTags.DOORS)).build())
                .build()
            )
            .name("open_door_with_target_from_ten_blocks")
            .tooltip("open_door_with_target_from_ten_blocks")
            .icon(CycleIcon.infer(Items.ARROW, new ItemStackTemplate(Items.TARGET, 10), Items.OAK_DOOR))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
        );
        addGoal(obtainItemGoal(CARROT_ON_A_STICK, items, Items.CARROT_ON_A_STICK)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(BARTER_WITH_PIGLIN)
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
        addGoal(BingoGoal.builder(NAUSEA)
            .criterion("obtain", EffectsChangedTrigger.TriggerInstance.hasEffects(MobEffectsPredicate.Builder.effects().and(MobEffects.NAUSEA)))
            .name("nausea")
            .icon(EffectIcon.of(MobEffects.NAUSEA))
            .reactant("eat_meat")
            .tags(BingoTags.ITEM, BingoTags.OCEAN, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(
                OBTAIN_ENCHANTED_ITEM,
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
        addGoal(BingoGoal.builder(DISENCHANT)
            .criterion("disenchant1", UseGrindstoneTrigger.builder().firstItem(ContextAwarePredicate.create(new ToolIsEnchantedCondition(true))).build())
            .criterion("disenchant2", UseGrindstoneTrigger.builder().secondItem(ContextAwarePredicate.create(new ToolIsEnchantedCondition(true))).build())
            .requirements(AdvancementRequirements.Strategy.OR)
            .name("disenchant")
            .icon(CycleIcon.infer(Items.GRINDSTONE, Items.ENCHANTED_BOOK))
            .tags(BingoTags.ACTION));
        // TODO: never use sword
        addGoal(BingoGoal.builder(CARNIVORE)
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
        addGoal(obtainSomeItemsFromTag(GOLD_IN_NAME, BingoItemTags.GOLD_IN_NAME, "bingo.goal.gold_in_name", 5, 7)
            .tooltip("gold_in_name")
            .antisynergy("gold_items"));
        addGoal(obtainSomeItemsFromTag(COPPER_IN_NAME, BingoItemTags.COPPER_IN_NAME, "bingo.goal.copper_in_name", 5, 7)
            .tooltip("copper_in_name")
            .antisynergy("copper_items"));
        addGoal(obtainItemGoal(SAND, items, Items.SAND, 33, 64)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(SANDSTONE, items, Items.SANDSTONE, 11, 32)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(CUT_SANDSTONE, items, Items.CUT_SANDSTONE, 11, 32)
            .setAntisynergy("sandstone")
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(PAPER, items, Items.PAPER, 6, 15)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(COAL_BLOCK, items, Items.COAL_BLOCK, 3, 6)
            .reactant("never_coal"));
        addGoal(obtainItemGoal(APPLE, items, Items.APPLE, 2, 5)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(TAME_HORSE)
            .criterion("obtain", TameAnimalTrigger.TriggerInstance.tamedAnimal(
                EntityPredicate.Builder.entity().of(entityTypes, EntityType.HORSE)
            ))
            .name("tame_horse")
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .icon(EntityIcon.ofSpawnEgg(EntityType.HORSE))
        );
        addGoal(BingoGoal.builder(HATCH_CHICKEN)
            .criterion("hatch", ChickenHatchTrigger.builder().build())
            .name("hatch_chicken")
            .icon(CycleIcon.infer(
                Items.EGG,
                createChickenIcon(ChickenVariants.TEMPERATE),
                Items.BLUE_EGG,
                createChickenIcon(ChickenVariants.COLD),
                Items.BROWN_EGG,
                createChickenIcon(ChickenVariants.WARM)
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD));
        // TODO: empty cauldron without buckets or bottles
        addGoal(BingoGoal.builder(SLEEP_IN_VILLAGER_BED)
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
        addGoal(BingoGoal.builder(BURN_VILLAGE_HOUSE)
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
                new BlockIcon(Blocks.FIRE.defaultBlockState(), new ItemStackTemplate(Items.FLINT_AND_STEEL)),
                EntityType.VILLAGER
            ))
        );
        addGoal(obtainItemGoal(EMERALD, items, Items.EMERALD)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(MILK_CURE)
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
                POTTERY_SHERD,
                items,
                new ItemTagCycleIcon(ItemTags.DECORATED_POT_SHERDS),
                ItemPredicate.Builder.item().of(items, ItemTags.DECORATED_POT_SHERDS)
            )
            .tags(BingoTags.OVERWORLD)
            .name("pottery_sherd"));
        final KeyMapping walkBackwards = Minecraft.getInstance().options.keyDown;
        addGoal(BingoGoal.builder(NEVER_WALK_BACKWARDS)
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

        addGoal(BingoGoal.builder(BRUSH_ARMADILLO)
            .criterion("brush", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
                ItemPredicate.Builder.item().of(items, Items.BRUSH),
                Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(entityTypes, EntityType.ARMADILLO)))
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name("brush_armadillo")
            .icon(IndicatorIcon.infer(EntityType.ARMADILLO, Items.BRUSH)));

        addGoal(obtainItemGoal(CRAFTER, items, Items.CRAFTER)
            .tags(BingoTags.OVERWORLD));

        addGoal(BingoGoal.builder(FILL_BUNDLE)
            .criterion("fill", FillBundleTrigger.builder().build())
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD)
            .name("fill_bundle")
            .icon(Items.BUNDLE));

        addGoal(obtainItemGoal(FIREFLY_BUSH, items, Items.FIREFLY_BUSH)
            .tags(BingoTags.OVERWORLD));

        addGoal(obtainItemGoal(DRIED_GHAST, items, Items.DRIED_GHAST)
            .tags(BingoTags.NETHER));

        addGoal(obtainItemGoal(COPPER_CHEST, items, Items.COPPER_CHEST)
            .tags(BingoTags.OVERWORLD));

        addGoal(obtainItemGoal(IRON_CHAIN, items, Items.IRON_CHAIN)
            .setAntisynergy("chain"));

        addGoal(obtainItemGoal(COPPER_CHAIN, items, Items.COPPER_CHAIN.unaffected())
            .setAntisynergy("chain"));

        addGoal(obtainItemGoal(SHELF, items, GoalIcon.infer(ItemTags.WOODEN_SHELVES), ItemPredicate.Builder.item().of(items, ItemTags.WOODEN_SHELVES))
            .name("shelf")
            .antisynergy("shelf"));

        addGoal(BingoGoal.builder(FEED_GOLDEN_DANDELION)
            .criterion(
                "feed",
                PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
                    ItemPredicate.Builder.item().of(items, Items.GOLDEN_DANDELION),
                    Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity()
                        .of(entityTypes, BingoEntityTypeTags.CAN_BE_AGE_LOCKED)
                        .flags(EntityFlagsPredicate.Builder.flags().setIsBaby(true))))
                )
            )
            .tags(BingoTags.OVERWORLD, BingoTags.ACTION)
            .name("feed_golden_dandelion")
            .tooltip("feed_golden_dandelion")
            .icon(new IndicatorIcon(new EntityTypeTagCycleIcon(BingoEntityTypeTags.CAN_BE_AGE_LOCKED), ItemIcon.ofItem(Items.GOLDEN_DANDELION))));
    }

    private GoalBuilder eatEntireCake() {
        GoalBuilder builder = BingoGoal.builder(EAT_ENTIRE_CAKE);
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

    private static GoalIcon createChickenIcon(ResourceKey<ChickenVariant> variant) {
        CompoundTag data = BingoUtil.compound(Map.of(
            "variant", StringTag.valueOf(variant.identifier().toString())
        ));
        return EntityIcon.ofSpawnEgg(EntityType.CHICKEN, data);
    }
}
