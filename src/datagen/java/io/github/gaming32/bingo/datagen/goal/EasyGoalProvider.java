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
import io.github.gaming32.bingo.datagen.BingoDataGenUtil;
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
import io.github.gaming32.bingo.triggers.MobConvertedTrigger;
import io.github.gaming32.bingo.util.BingoUtil;
import io.github.gaming32.bingo.util.BlockPattern;
import io.github.gaming32.bingo.util.Identifiers;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEntityTypeTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.predicates.BlockPredicate;
import net.minecraft.advancements.predicates.DamagePredicate;
import net.minecraft.advancements.predicates.DamageSourcePredicate;
import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.advancements.predicates.DistancePredicate;
import net.minecraft.advancements.predicates.InputPredicate;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.LocationPredicate;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.advancements.predicates.MobEffectsPredicate;
import net.minecraft.advancements.predicates.StatePropertiesPredicate;
import net.minecraft.advancements.predicates.TagPredicate;
import net.minecraft.advancements.predicates.entity.EntityEquipmentPredicate;
import net.minecraft.advancements.predicates.entity.EntityFlagsPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.predicates.entity.EntityTypePredicate;
import net.minecraft.advancements.predicates.entity.PlayerPredicate;
import net.minecraft.advancements.triggers.ConsumeItemTrigger;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.DefaultBlockInteractionTrigger;
import net.minecraft.advancements.triggers.EffectsChangedTrigger;
import net.minecraft.advancements.triggers.FishingRodHookedTrigger;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.advancements.triggers.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.triggers.PickedUpItemTrigger;
import net.minecraft.advancements.triggers.PlayerInteractTrigger;
import net.minecraft.advancements.triggers.PlayerTrigger;
import net.minecraft.advancements.triggers.SummonedEntityTrigger;
import net.minecraft.advancements.triggers.TameAnimalTrigger;
import net.minecraft.advancements.triggers.TradeTrigger;
import net.minecraft.advancements.triggers.UsingItemTrigger;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchBlock;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.github.gaming32.bingo.datagen.goal.GoalIds.Easy.*;

public class EasyGoalProvider extends DifficultyGoalProvider {
    public EasyGoalProvider(BootstrapContext<BingoGoal> context) {
        super(BingoDifficulties.EASY, context);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void addGoals() {
        final var entityTypes = context.lookup(Registries.ENTITY_TYPE);
        final var blocks = context.lookup(Registries.BLOCK);
        final var items = context.lookup(Registries.ITEM);
        final var features = context.lookup(Registries.FEATURE);
        final var damageTypes = context.lookup(Registries.DAMAGE_TYPE);

        addGoal(GoalBuilder.of(DIFFERENT_FISH)
            .sub("count", BingoSub.random(2, 4))
            .criterion(
                context,
                "obtain",
                HasSomeFoodItemsTrigger.builder().requiredCount(1).tag(items, ItemTags.FISHES).build(),
                subber -> subber.sub("conditions.required_count", "count"))
            .progress("obtain")
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.different_fish", 0), subber -> subber.sub("with.0", "count"))
            .tooltip("different_fish")
            .icon(context, new ItemTagCycleIcon(ItemTags.FISHES), subber -> subber.sub("+count", "count")));
        addGoal(GoalBuilder.of(GROW_TREE_IN_NETHER)
            .criterion(context, "grow", GrowFeatureTrigger.builder()
                .feature(features, BingoFeatureTags.TREES)
                .location(LocationPredicate.Builder.inDimension(Level.NETHER).build())
                .build()
            )
            .tags(BingoTags.ACTION, BingoTags.NETHER, BingoTags.OVERWORLD)
            .name("grow_tree_in_nether")
            .tooltip("grow_tree_in_nether")
            .icon(context, CycleIcon.infer(Items.BONE_MEAL, Items.OAK_SAPLING)));
        addGoal(obtainSomeItemsFromTag(TERRACOTTA, ItemTags.TERRACOTTA, "bingo.goal.colors_of_terracotta", 4, 7)
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD)
            .reactant("use_furnace")
            .antisynergy("terracotta_color")
            .infrequency(4)
        );
        addGoal(obtainItemGoal(MUSHROOM_STEW, items, Items.MUSHROOM_STEW, 2, 5));
        addGoal(GoalBuilder.of(SHOOT_BUTTON)
            .criterion(context, "obtain", ArrowPressTrigger.builder()
                .arrow(EntityPredicate.Builder.entity().of(entityTypes, EntityTypeTags.ARROWS).build())
                .buttonOrPlate(BlockPredicate.Builder.block().of(blocks, BlockTags.BUTTONS).build())
                .build()
            )
            .tags(BingoTags.ACTION)
            .name("shoot_button")
            .icon(context, Items.OAK_BUTTON));
        addGoal(obtainItemGoal(WRITABLE_BOOK, items, Items.WRITABLE_BOOK)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(FLINT, items, Items.FLINT, 8, 16));
        addGoal(eatEntireCake(blocks));
        addGoal(obtainItemGoal(PUMPKIN_PIE, items, Items.PUMPKIN_PIE)
            .tags(BingoTags.OVERWORLD));
        addGoal(GoalBuilder.of(FISH_TREASURE_JUNK)
            .criterion(context, "treasure", FishingRodHookedTrigger.TriggerInstance.fishedItem(
                Optional.empty(), Optional.empty(),
                Optional.of(ItemPredicate.Builder.item().of(items, BingoItemTags.FISHING_TREASURE).build())
            ))
            .criterion(context, "junk", FishingRodHookedTrigger.TriggerInstance.fishedItem(
                Optional.empty(), Optional.empty(),
                Optional.of(ItemPredicate.Builder.item().of(items, BingoItemTags.FISHING_JUNK).build())
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name("fish_treasure_junk").tooltip("fish_treasure_junk")
            .icon(context, makeItemWithGlint(Items.FISHING_ROD))
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
        addGoal(GoalBuilder.of(NEVER_WEAR_CHESTPLATES)
            .criterion(context, "equip", EquipItemTrigger.builder()
                .newItem(ItemPredicate.Builder.item().of(items, ItemTags.CHEST_ARMOR).build())
                .slots(EquipmentSlot.CHEST)
                .build()
            )
            .tags(BingoTags.NEVER)
            .name("never_wear_chestplates")
            .icon(context, Items.IRON_CHESTPLATE)
            .antisynergy("never_wear_armor")
            .catalyst("wear_armor"));
        addGoal(GoalBuilder.of(NEVER_USE_SHIELDS)
            .criterion(context, "use", CriteriaTriggers.USING_ITEM.createCriterion(
                new UsingItemTrigger.TriggerInstance(
                    Optional.empty(),
                    Optional.of(ItemPredicate.Builder.item().of(items, ConventionalItemTags.SHIELD_TOOLS).build())
                )
            ))
            .tags(BingoTags.NEVER)
            .name("never_use_shields")
            .tooltip("never_use_shields")
            .icon(context, Items.SHIELD));
        addGoal(obtainItemGoal(JUKEBOX, items, Items.JUKEBOX));
        addGoal(GoalBuilder.of(_3X3X3_GLASS_CUBE)
            .criterion(context, "build", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(
                AllOfCondition.allOf(
                    MatchBlock.blockMatches(blocks, Blocks.GLASS)
                        .or(MatchBlock.blockMatches(blocks, Blocks.LAVA)),
                    BlockPatternCondition.builder()
                        .aisle("###", "###", "###")
                        .aisle("###", "#+#", "###")
                        .aisle("###", "###", "###")
                        .where('#', BlockPredicate.Builder.block().of(blocks, Blocks.GLASS))
                        .where('+', BlockPredicate.Builder.block().of(blocks, Blocks.LAVA))
                        .rotations(BlockPattern.Rotations.NONE)
                )
            ))
            .name("3x3x3_glass_cube")
            .icon(context, new CycleIcon(
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
        addGoal(GoalBuilder.of(CREATE_SNOW_GOLEM)
            .criterion(context, "summon", SummonedEntityTrigger.TriggerInstance.summonedEntity(
                EntityPredicate.Builder.entity().of(entityTypes, EntityTypes.SNOW_GOLEM)
            ))
            .tags(BingoTags.ACTION, BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.create_snow_golem", EntityTypes.SNOW_GOLEM.getDescription()))
            .icon(context, EntityIcon.ofSpawnEgg(EntityTypes.SNOW_GOLEM))
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
        addGoal(GoalBuilder.of(FULL_IRON_ARMOR)
            .criterion(context, "armor", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS
            ))
            .tags(BingoTags.ITEM)
            .infrequency(3)
            .reactant("wear_armor")
            .name("full_iron_armor")
            .icon(context, new CycleIcon(
                ItemIcon.ofItem(Items.IRON_HELMET),
                ItemIcon.ofItem(Items.IRON_CHESTPLATE),
                ItemIcon.ofItem(Items.IRON_LEGGINGS),
                ItemIcon.ofItem(Items.IRON_BOOTS)
            ))
        );
        addGoal(GoalBuilder.of(FULL_LEATHER_ARMOR)
            .criterion(context, "armor", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS
            ))
            .tags(BingoTags.ITEM)
            .infrequency(3)
            .name("full_leather_armor")
            .icon(context, new CycleIcon(
                ItemIcon.ofItem(Items.LEATHER_HELMET),
                ItemIcon.ofItem(Items.LEATHER_CHESTPLATE),
                ItemIcon.ofItem(Items.LEATHER_LEGGINGS),
                ItemIcon.ofItem(Items.LEATHER_BOOTS)
            ))
        );
        addGoal(GoalBuilder.of(FILL_WATER_CAULDRON)
            .criterion(context, "fill", CriteriaTriggers.ITEM_USED_ON_BLOCK.createCriterion(
                new ItemUsedOnLocationTrigger.TriggerInstance(
                    Optional.empty(),
                    Optional.of(Holder.direct(
                        MatchBlock.blockMatches(
                            blocks,
                            Blocks.WATER_CAULDRON,
                            StatePropertiesPredicate.Builder.properties().hasProperty(LayeredCauldronBlock.LEVEL, 3)
                        ).build()
                    ))
                )
            ))
            .name(Component.translatable(
                "bingo.goal.fill_water_cauldron",
                Blocks.CAULDRON.getName(), Blocks.WATER.getName()
            ))
            .icon(context, BlockIcon.ofBlock(Blocks.WATER_CAULDRON.defaultBlockState()
                .setValue(LayeredCauldronBlock.LEVEL, 3)
            ))
            .tags(BingoTags.OVERWORLD)
        );
        addGoal(GoalBuilder.of(COMPLETE_MAP)
            .criterion(context, "complete", CompleteMapTrigger.TriggerInstance.completeMap())
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name("complete_map")
            .icon(context, Items.FILLED_MAP)
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
        addGoal(obtainSomeItemsFromTag(DIFFERENT_SLABS, BingoItemTags.SLABS, "bingo.goal.different_slabs", 5, 7)
            .antisynergy("slabs")
            .infrequency(2));
        addGoal(GoalBuilder.of(ALMOST_EVERY_SWORD)
            .criterion(context, "wood", InventoryChangeTrigger.TriggerInstance.hasItems(Items.WOODEN_SWORD))
            .criterion(context, "stone", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STONE_SWORD))
            .criterion(context, "gold", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GOLDEN_SWORD))
            .criterion(context, "iron", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_SWORD))
            .criterion(context, "diamond", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND_SWORD))
            .progress(AchievedRequirementsProgressTracker.INSTANCE)
            .name("almost_every_sword")
            .icon(context, CycleIcon.infer(Items.WOODEN_SWORD, Items.STONE_SWORD, Items.GOLDEN_SWORD, Items.IRON_SWORD, Items.DIAMOND_SWORD))
            .antisynergy("all_swords")
            .infrequency(2)
            .tags(BingoTags.ITEM));
        addGoal(GoalBuilder.of(ALMOST_EVERY_PICKAXE)
            .criterion(context, "wood", InventoryChangeTrigger.TriggerInstance.hasItems(Items.WOODEN_PICKAXE))
            .criterion(context, "stone", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STONE_PICKAXE))
            .criterion(context, "gold", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GOLDEN_PICKAXE))
            .criterion(context, "iron", InventoryChangeTrigger.TriggerInstance.hasItems(Items.IRON_PICKAXE))
            .criterion(context, "diamond", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND_PICKAXE))
            .progress(AchievedRequirementsProgressTracker.INSTANCE)
            .name("almost_every_pickaxe")
            .icon(context, CycleIcon.infer(Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.GOLDEN_PICKAXE, Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE))
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
        addGoal(GoalBuilder.of(SLEEP_IN_NETHER)
            .criterion(context, "sleep", IntentionalGameDesignTrigger.TriggerInstance.clicked(
                LocationPredicate.Builder.inDimension(Level.NETHER).build()
            ))
            .tags(BingoTags.ACTION, BingoTags.NETHER, BingoTags.OVERWORLD)
            .name("sleep_in_nether")
            .icon(context, Items.BED.purple())
        );
        addGoal(obtainItemGoal(FERMENTED_SPIDER_EYE, items, Items.FERMENTED_SPIDER_EYE)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainSomeItemsFromTag(DIFFERENT_STAIRS, BingoItemTags.STAIRS, "bingo.goal.different_stairs", 5, 7)
            .antisynergy("stairs")
            .infrequency(2));
        addGoal(obtainItemGoal(ENDER_PEARL, items, Items.ENDER_PEARL, 2, 3)
            .infrequency(2));
        addGoal(
            obtainItemGoal(
                EGG,
                items,
                new ItemTagCycleIcon(ItemTags.EGGS, 3),
                ItemPredicate.Builder.item().of(items, ItemTags.EGGS).withCount(MinMaxBounds.Ints.atLeast(3))
            )
                .name(Component.translatable("bingo.count", 3, Component.translatable(Items.EGG.getDescriptionId())))
                .tooltip("egg")
                .tags(BingoTags.OVERWORLD)
        );
        addGoal(GoalBuilder.of(_4X4_PAINTINGS)
            .criterion(context, "paintings", AdjacentPaintingTrigger.builder()
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
            .icon(context, new ItemStackTemplate(Items.PAINTING, 3))
            .antisynergy("painting")
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(BONE_BLOCK, items, Items.BONE_BLOCK, 5, 10));
        addGoal(GoalBuilder.of(DOUBLE_CREEPER_BOAT)
            .criterion(context, "break", BingoTriggers.DESTROY_VEHICLE.get().createCriterion(new DestroyVehicleTrigger.TriggerInstance(
                Optional.empty(),
                Optional.of(BingoDataGenUtil.allOf(
                    LootItemEntityPropertyCondition.hasProperties(
                        LootContext.EntityTarget.THIS,
                        EntityPredicate.Builder.entity()
                            .of(entityTypes, EntityTypeTags.BOAT)
                            .build()
                    ).build(),
                    new PassengersCondition(List.of(
                        EntityPredicate.wrap(EntityPredicate.Builder.entity().of(entityTypes, EntityTypes.CREEPER)),
                        EntityPredicate.wrap(EntityPredicate.Builder.entity().of(entityTypes, EntityTypes.CREEPER))
                    ), false)
                )),
                Optional.empty()
            )))
            .name("double_creeper_boat")
            .icon(context, new CycleIcon(
                new EntityTypeTagCycleIcon(EntityTypeTags.BOAT, Items.OAK_BOAT.builtInRegistryHolder()),
                EntityIcon.ofSpawnEgg(EntityTypes.CREEPER, 2)
            ))
            .tags(BingoTags.ACTION, BingoTags.COMBAT, BingoTags.OVERWORLD)
        );
        addGoal(GoalBuilder.of(VILLAGER_TRADE)
            .criterion(context, "obtain", TradeTrigger.TriggerInstance.tradedWithVillager())
            .name("villager_trade")
            .icon(context, EntityIcon.ofSpawnEgg(EntityTypes.VILLAGER))
            .tags(BingoTags.VILLAGE, BingoTags.ACTION, BingoTags.OVERWORLD));
        addGoal(GoalBuilder.of(DIFFERENT_COLORED_SHIELDS)
            .sub("count", BingoSub.random(2, 3))
            .criterion(context, "obtain", DifferentColoredShieldsTrigger.builder(1).build(), subber -> subber.sub("conditions.min_count", "count"))
            .progress("obtain")
            .name(Component.translatable("bingo.goal.different_colored_shields", 0), subber -> subber.sub("with.0", "count"))
            .tooltip("different_colored_shields")
            .icon(context, CycleIcon.infer(Arrays.stream(DyeColor.values()).map(DifficultyGoalProvider::makeShieldWithColor)), subber -> {
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

        addGoal(GoalBuilder.of(NEVER_SLEEP)
            .criterion(context, "sleep", PlayerTrigger.TriggerInstance.sleptInBed())
            .tags(BingoTags.NEVER, BingoTags.OVERWORLD)
            .name("never_sleep")
            .icon(context, Items.BED.red())
            .catalyst("sleep"));
        addGoal(GoalBuilder.of(GROW_HUGE_MUSHROOM)
            .criterion(context, "grow", GrowFeatureTrigger.builder().feature(features, BingoFeatureTags.HUGE_MUSHROOMS).build())
            .tags(BingoTags.ACTION)
            .name("grow_huge_mushroom")
            .icon(context, Blocks.RED_MUSHROOM_BLOCK));
        addGoal(GoalBuilder.of(WATER_LAVA_MILK)
            .criterion(context, "buckets", InventoryChangeTrigger.TriggerInstance.hasItems(
                Items.WATER_BUCKET, Items.LAVA_BUCKET, Items.MILK_BUCKET
            ))
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD)
            .name(Component.translatable(
                "bingo.three",
                Component.translatable(Items.WATER_BUCKET.getDescriptionId()),
                Component.translatable(Items.LAVA_BUCKET.getDescriptionId()),
                Component.translatable(Items.MILK_BUCKET.getDescriptionId())
            ))
            .icon(context, new CycleIcon(
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
        addGoal(GoalBuilder.of(FINISH_AT_SPAWN)
            .criterion(context, "nearby", CriteriaTriggers.LOCATION.createCriterion(
                new PlayerTrigger.TriggerInstance(Optional.of(BingoDataGenUtil.allOf(
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
            .icon(context, Items.COMPASS)
        );
        addGoal(obtainItemGoal(STONE, items, Items.STONE, 32, 64)
            .tooltip("stone")
            .infrequency(2)
            .reactant("use_furnace")
            .tags(BingoTags.OVERWORLD));
        addGoal(GoalBuilder.of(KILL_PASSIVE_MOBS_WITH_ONLY_FIRE)
            .sub("count", BingoSub.random(4, 8))
            .criterion(context, "kill", EntityDieNearPlayerTrigger.builder()
                .entity(BingoDataGenUtil.allOf(
                    LootItemEntityPropertyCondition.hasProperties(
                        LootContext.EntityTarget.THIS,
                        EntityPredicate.Builder.entity().of(entityTypes, BingoEntityTypeTags.PASSIVE)
                    ),
                    HasOnlyBeenDamagedByCondition.builder().damageTypeTag(damageTypes, DamageTypeTags.IS_FIRE)
                ))
                .killingBlow(DamagePredicate.Builder.damageInstance().type(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(damageTypes, DamageTypeTags.IS_FIRE))).build())
                .build())
            .requiredCount(new SubBingoSub("count"))
            .progress(GoalAchievedCountProgressTracker.INSTANCE)
            .name(Component.translatable("bingo.goal.kill_passive_mobs_with_only_fire", 0), subber -> subber.sub("with.0", "count"))
            .tooltip("kill_passive_mobs_with_only_fire")
            .icon(context, new CycleIcon(
                new EntityTypeTagCycleIcon(BingoEntityTypeTags.PASSIVE, 2),
                new BlockIcon(Blocks.FIRE.defaultBlockState(), new ItemStackTemplate(Items.FLINT_AND_STEEL))
            ), subber -> subber.sub("icons.0.count", "count").sub("icons.1.item.count", "count"))
            .reactant("pacifist")
            .tags(BingoTags.ACTION, BingoTags.COMBAT));
        addGoal(GoalBuilder.of(KILL_CREEPER_WITH_ONLY_FIRE)
            .criterion(context, "kill", EntityDieNearPlayerTrigger.builder()
                .entity(BingoDataGenUtil.allOf(
                    LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().of(entityTypes, EntityTypes.CREEPER)),
                    HasOnlyBeenDamagedByCondition.builder().damageTypeTag(damageTypes, DamageTypeTags.IS_FIRE)
                ))
                .killingBlow(DamagePredicate.Builder.damageInstance().type(DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(damageTypes, DamageTypeTags.IS_FIRE))).build())
                .build()
            )
            .name("kill_creeper_with_only_fire")
            .icon(context, CycleIcon.infer(EntityTypes.CREEPER, Blocks.FIRE))
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
        addGoal(GoalBuilder.of(COMMON_MOB_DROPS)
            .criterion(context, "rotten_flesh", InventoryChangeTrigger.TriggerInstance.hasItems(Items.ROTTEN_FLESH))
            .criterion(context, "spider_eye", InventoryChangeTrigger.TriggerInstance.hasItems(Items.SPIDER_EYE))
            .criterion(context, "bone", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BONE))
            .criterion(context, "gunpowder", InventoryChangeTrigger.TriggerInstance.hasItems(Items.GUNPOWDER))
            .criterion(context, "ender_pearl", InventoryChangeTrigger.TriggerInstance.hasItems(Items.ENDER_PEARL))
            .progress(AchievedRequirementsProgressTracker.INSTANCE)
            .name(Component.translatable("bingo.goal.common_mob_drops",
                Component.translatable(Items.ROTTEN_FLESH.getDescriptionId()),
                Component.translatable(Items.SPIDER_EYE.getDescriptionId()),
                Component.translatable(Items.BONE.getDescriptionId()),
                Component.translatable(Items.GUNPOWDER.getDescriptionId()),
                Component.translatable(Items.ENDER_PEARL.getDescriptionId())))
            .icon(context, CycleIcon.infer(Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.BONE, Items.GUNPOWDER, Items.ENDER_PEARL))
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(FEATHER, items, Items.FEATHER, 32, 64)
            .infrequency(2));
        addGoal(obtainItemGoal(LILY_PAD, items, Items.LILY_PAD, 2, 10)
            .tags(BingoTags.OVERWORLD, BingoTags.RARE_BIOME));
        addGoal(obtainItemGoal(STICK, items, Items.STICK, 65, 128)
            .infrequency(2));
        addGoal(GoalBuilder.of(WEAR_DIFFERENT_COLORED_ARMOR)
            .criterion(context, "obtain", WearDifferentColoredArmorTrigger.builder(4).build())
            .progress("obtain")
            .name(Component.translatable("bingo.goal.wear_different_colored_armor", 4))
            .icon(context, CycleIcon.infer(
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
        addGoal(GoalBuilder.of(NEVER_USE_BOAT)
            .criterion(context, "use", CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.createCriterion(
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
            .icon(context, Items.OAK_BOAT));
        addGoal(GoalBuilder.of(PLACE_FISH_IN_NETHER)
            .criterion(context, "place", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(AllOfCondition.allOf(
                MatchTool.toolMatches(ItemPredicate.Builder.item().of(items, BingoItemTags.FISH_BUCKETS)),
                LocationCheck.checkLocation(LocationPredicate.Builder.location().setDimension(Level.NETHER))
            )))
            .name("place_fish_in_nether")
            .icon(context, new CycleIcon(
                ItemIcon.ofItem(Items.NETHERRACK),
                ItemIcon.ofItem(Items.TROPICAL_FISH_BUCKET)
            ))
            .antisynergy("pacifist")
            .tags(BingoTags.NETHER, BingoTags.ACTION, BingoTags.OVERWORLD)
        );
        addGoal(obtainItemGoal(DRIED_KELP_BLOCK, items, Items.DRIED_KELP_BLOCK, 11, 20)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        addGoal(GoalBuilder.of(DROWN_ZOMBIE)
            .criterion(context, "drown", MobConvertedTrigger.builder()
                .convertedFrom(entityTypes, EntityTypes.ZOMBIE)
                .convertedTo(entityTypes, EntityTypes.DROWNED)
                .build())
            .name("drown_zombie")
            .tooltip("drown_zombie")
            .icon(context, CycleIcon.infer(EntityTypes.ZOMBIE, EntityTypes.DROWNED))
            .tags(BingoTags.ACTION, BingoTags.COMBAT, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(GUNPOWDER, items, Items.GUNPOWDER, 2, 5)
            .infrequency(2));
        addGoal(obtainItemGoal(SPIDER_EYE, items, Items.SPIDER_EYE, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainSomeEdibleItems(EDIBLE_ITEMS, 4, 5).tags(BingoTags.OVERWORLD));
        addGoal(GoalBuilder.of(BREED_MOBS)
            .sub("count", BingoSub.random(2, 4))
            .criterion(
                context,
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
            .icon(context, Items.WHEAT_SEEDS)
            .antisynergy("breed_animals")
            .infrequency(2)
            .tags(BingoTags.ACTION, BingoTags.STAT)
        );
        addGoal(crouchDistanceGoal(CROUCH_DISTANCE, 100, 200));
        // TODO: never use debug
        addGoal(GoalBuilder.of(SHOOT_BELL_FROM_10_BLOCKS)
            .criterion(context, "shoot", ShootBellTrigger.builder()
                .distance(DistancePredicate.absolute(MinMaxBounds.Doubles.atLeast(10)))
                .build())
            .name("shoot_bell_from_10_blocks")
            .tooltip("shoot_bell_from_10_blocks")
            .icon(context, CycleIcon.infer(Items.ARROW, Items.BELL))
            .tags(BingoTags.ACTION, BingoTags.VILLAGE, BingoTags.OVERWORLD));
        addGoal(GoalBuilder.of(GRINDSTONE_REPAIR)
            .criterion(context, "repair1", UseGrindstoneTrigger.builder().firstItem(Holder.direct(new ToolDamageCondition(MinMaxBounds.Ints.atLeast(1)))).build())
            .criterion(context, "repair2", UseGrindstoneTrigger.builder().secondItem(Holder.direct(new ToolDamageCondition(MinMaxBounds.Ints.atLeast(1)))).build())
            .requirements(AdvancementRequirements.Strategy.OR)
            .name("grindstone_repair")
            .icon(context, CycleIcon.infer(Items.GRINDSTONE, ConventionalItemTags.TOOLS))
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
        addGoal(GoalBuilder.of(DRINK_SUS_STEW)
            .criterion(context, "drink", ConsumeItemTrigger.TriggerInstance.usedItem(items, Items.SUSPICIOUS_STEW))
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD)
            .reactant("eat_non_meat")
            .name(Component.translatable("bingo.goal.drink_sus_stew", Component.translatable(Items.SUSPICIOUS_STEW.getDescriptionId())))
            .icon(context, Items.SUSPICIOUS_STEW));
        addGoal(GoalBuilder.of(GIVE_FOX_SWORD)
            .criterion(context, "pickup", CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.createCriterion(
                new PickedUpItemTrigger.TriggerInstance(
                    Optional.empty(),
                    Optional.of(ItemPredicate.Builder.item().of(items, ItemTags.SWORDS).build()),
                    Optional.of(Holder.direct(
                        LootItemEntityPropertyCondition.hasProperties(
                            LootContext.EntityTarget.THIS,
                            EntityPredicate.Builder.entity().of(entityTypes, EntityTypes.FOX)
                        ).build()
                    ))
                )
            ))
            .name(Component.translatable("bingo.goal.give_fox_sword", EntityTypes.FOX.getDescription()))
            .icon(context, new CycleIcon(
                EntityIcon.ofSpawnEgg(EntityTypes.FOX),
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
            addGoal(GoalBuilder.of(PORKCHOPS_IN_SOUL_CAMPFIRE)
                .criterion(context, "place", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
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
                .icon(context, new CycleIcon(
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
        addGoal(obtainItemGoal(COPPER_LANTERN, items, Items.COPPER_LANTERN.weathering().unaffected())
            .tags(BingoTags.OVERWORLD)
            .setAntisynergy("lantern"));
        addGoal(GoalBuilder.of(OPEN_DOOR_WITH_TARGET_FROM_TEN_BLOCKS)
            .criterion(context, "open_door", DoorOpenedByTargetTrigger.builder()
                .projectile(EntityPredicate.Builder.entity().distance(DistancePredicate.horizontal(MinMaxBounds.Doubles.atLeast(10))))
                .door(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(blocks, BlockTags.DOORS)).build())
                .build()
            )
            .name("open_door_with_target_from_ten_blocks")
            .tooltip("open_door_with_target_from_ten_blocks")
            .icon(context, CycleIcon.infer(Items.ARROW, new ItemStackTemplate(Items.TARGET, 10), Items.OAK_DOOR))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
        );
        addGoal(obtainItemGoal(CARROT_ON_A_STICK, items, Items.CARROT_ON_A_STICK)
            .tags(BingoTags.OVERWORLD));
        addGoal(GoalBuilder.of(BARTER_WITH_PIGLIN)
            .criterion(context, "barter", CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.createCriterion(new PickedUpItemTrigger.TriggerInstance(
                Optional.empty(),
                Optional.of(ItemPredicate.Builder.item().of(items, PiglinAi.BARTERING_ITEM).build()),
                Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(entityTypes, EntityTypes.PIGLIN).flags(EntityFlagsPredicate.Builder.flags().setIsBaby(false)))))))
            .criterion(context, "barter_directly", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
                ItemPredicate.Builder.item().of(items, PiglinAi.BARTERING_ITEM),
                Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(entityTypes, EntityTypes.PIGLIN).flags(EntityFlagsPredicate.Builder.flags().setIsBaby(false))))))
            .requirements(AdvancementRequirements.Strategy.OR)
            .name("barter_with_piglin")
            .icon(context, EntityIcon.ofSpawnEgg(EntityTypes.PIGLIN))
            .tags(BingoTags.ACTION, BingoTags.NETHER));
        addGoal(GoalBuilder.of(NAUSEA)
            .criterion(context, "obtain", EffectsChangedTrigger.TriggerInstance.hasEffects(MobEffectsPredicate.Builder.effects().and(MobEffects.NAUSEA)))
            .name("nausea")
            .icon(context, EffectIcon.of(MobEffects.NAUSEA))
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
        addGoal(GoalBuilder.of(DISENCHANT)
            .criterion(context, "disenchant1", UseGrindstoneTrigger.builder().firstItem(Holder.direct(new ToolIsEnchantedCondition(true))).build())
            .criterion(context, "disenchant2", UseGrindstoneTrigger.builder().secondItem(Holder.direct(new ToolIsEnchantedCondition(true))).build())
            .requirements(AdvancementRequirements.Strategy.OR)
            .name("disenchant")
            .icon(context, CycleIcon.infer(Items.GRINDSTONE, Items.ENCHANTED_BOOK))
            .tags(BingoTags.ACTION));
        // TODO: never use sword
        addGoal(GoalBuilder.of(CARNIVORE)
            .criterion(context, "not_meat", ConsumeItemTrigger.TriggerInstance.usedItem(
                ItemPredicate.Builder.item()
                    .of(items, BingoItemTags.NOT_MEAT)
            ))
            .tags(BingoTags.NEVER, BingoTags.ACTION)
            .antisynergy("food")
            .catalyst("eat_non_meat")
            .name("carnivore")
            .tooltip("carnivore")
            .icon(context, new ItemTagCycleIcon(ItemTags.MEAT))
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
        addGoal(GoalBuilder.of(TAME_HORSE)
            .criterion(context, "obtain", TameAnimalTrigger.TriggerInstance.tamedAnimal(
                EntityPredicate.Builder.entity().of(entityTypes, EntityTypes.HORSE)
            ))
            .name("tame_horse")
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .icon(context, EntityIcon.ofSpawnEgg(EntityTypes.HORSE))
        );
        addGoal(GoalBuilder.of(HATCH_CHICKEN)
            .criterion(context, "hatch", ChickenHatchTrigger.builder().build())
            .name("hatch_chicken")
            .icon(context, CycleIcon.infer(
                Items.EGG,
                createChickenIcon(ChickenVariants.TEMPERATE),
                Items.BLUE_EGG,
                createChickenIcon(ChickenVariants.COLD),
                Items.BROWN_EGG,
                createChickenIcon(ChickenVariants.WARM)
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD));
        // TODO: empty cauldron without buckets or bottles
        addGoal(GoalBuilder.of(SLEEP_IN_VILLAGER_BED)
            .criterion(context, "sleep", BingoTriggers.slept(
                Holder.direct(new VillagerOwnershipCondition(PoiManager.Occupancy.IS_OCCUPIED, Optional.empty()))
            ))
            .name(Component.translatable(
                "bingo.goal.sleep_in_villager_bed",
                EntityTypes.VILLAGER.getDescription()
            ))
            .icon(context, new CycleIcon(
                ItemIcon.ofItem(Items.BED.red()),
                EntityIcon.ofSpawnEgg(EntityTypes.VILLAGER)
            ))
            .reactant("sleep")
            .tags(BingoTags.ACTION, BingoTags.VILLAGE, BingoTags.OVERWORLD)
        );
        addGoal(GoalBuilder.of(BURN_VILLAGE_HOUSE)
            .criterion(context, "burn", CriteriaTriggers.ITEM_USED_ON_BLOCK.createCriterion(
                new ItemUsedOnLocationTrigger.TriggerInstance(
                    Optional.empty(),
                    Optional.of(BingoDataGenUtil.allOf(
                        MatchTool.toolMatches(ItemPredicate.Builder.item().of(items, ItemTags.CREEPER_IGNITERS)).build(),
                        FlammableCondition.INSTANCE,
                        new InStructureCondition(StructureTags.VILLAGE)
                    ))
                )
            ))
            .name("burn_village_house")
            .tags(BingoTags.ACTION, BingoTags.VILLAGE, BingoTags.OVERWORLD)
            .icon(context, CycleIcon.infer(
                new BlockIcon(Blocks.FIRE.defaultBlockState(), new ItemStackTemplate(Items.FLINT_AND_STEEL)),
                EntityTypes.VILLAGER
            ))
        );
        addGoal(obtainItemGoal(EMERALD, items, Items.EMERALD)
            .tags(BingoTags.OVERWORLD));
        addGoal(GoalBuilder.of(MILK_CURE)
            .criterion(context, "cure", CriteriaTriggers.CONSUME_ITEM.createCriterion(
                new ConsumeItemTrigger.TriggerInstance(
                    Optional.of(Holder.direct(
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
            .icon(context, Items.MILK_BUCKET)
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
        addGoal(GoalBuilder.of(NEVER_WALK_BACKWARDS)
            .criterion(context, "walk", CriteriaTriggers.TICK.createCriterion(new PlayerTrigger.TriggerInstance(
                Optional.of(EntityPredicate.wrap(
                    EntityPredicate.Builder.entity()
                        .player(PlayerPredicate.Builder.player()
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
            .icon(context, Items.DIRT_PATH)
        );

        addGoal(GoalBuilder.of(BRUSH_ARMADILLO)
            .criterion(context, "brush", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
                ItemPredicate.Builder.item().of(items, Items.BRUSH),
                Optional.of(EntityPredicate.wrap(EntityPredicate.Builder.entity().of(entityTypes, EntityTypes.ARMADILLO)))
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name("brush_armadillo")
            .icon(context, IndicatorIcon.infer(EntityTypes.ARMADILLO, Items.BRUSH)));

        addGoal(obtainItemGoal(CRAFTER, items, Items.CRAFTER)
            .tags(BingoTags.OVERWORLD));

        addGoal(GoalBuilder.of(FILL_BUNDLE)
            .criterion(context, "fill", FillBundleTrigger.builder().build())
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD)
            .name("fill_bundle")
            .icon(context, Items.BUNDLE));

        addGoal(obtainItemGoal(FIREFLY_BUSH, items, Items.FIREFLY_BUSH)
            .tags(BingoTags.OVERWORLD));

        addGoal(obtainItemGoal(DRIED_GHAST, items, Items.DRIED_GHAST)
            .tags(BingoTags.NETHER));

        addGoal(obtainItemGoal(COPPER_CHEST, items, Items.COPPER_CHEST.weathering().unaffected())
            .tags(BingoTags.OVERWORLD));

        addGoal(obtainItemGoal(IRON_CHAIN, items, Items.IRON_CHAIN)
            .setAntisynergy("chain"));

        addGoal(obtainItemGoal(COPPER_CHAIN, items, Items.COPPER_CHAIN.weathering().unaffected())
            .setAntisynergy("chain"));

        addGoal(obtainItemGoal(SHELF, items, GoalIcon.infer(ItemTags.WOODEN_SHELVES), ItemPredicate.Builder.item().of(items, ItemTags.WOODEN_SHELVES))
            .name("shelf")
            .antisynergy("shelf"));

        addGoal(GoalBuilder.of(FEED_GOLDEN_DANDELION)
            .criterion(
                context,
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
            .icon(context, new IndicatorIcon(new EntityTypeTagCycleIcon(BingoEntityTypeTags.CAN_BE_AGE_LOCKED), ItemIcon.ofItem(Items.GOLDEN_DANDELION))));
    }

    private GoalBuilder eatEntireCake(HolderGetter<Block> blocks) {
        GoalBuilder builder = GoalBuilder.of(EAT_ENTIRE_CAKE);
        for (int level = 0; level < 7; level++) {
            LootItemCondition blockCondition;
            if (level == 6) {
                blockCondition = MatchBlock.blockMatches(blocks, Blocks.AIR).build();
            } else {
                blockCondition = MatchBlock.blockMatches(
                    blocks,
                    Blocks.CAKE,
                    StatePropertiesPredicate.Builder.properties().hasProperty(BlockStateProperties.BITES, level + 1)
                ).build();
            }
            builder.criterion(context, "level_" + level, CriteriaTriggers.DEFAULT_BLOCK_USE.createCriterion(
                new DefaultBlockInteractionTrigger.TriggerInstance(Optional.empty(), Optional.of(Holder.direct(blockCondition)))
            ));
        }
        return builder
            .progress(AchievedRequirementsProgressTracker.INSTANCE)
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name("eat_entire_cake")
            .icon(context, Items.CAKE)
            .reactant("eat_non_meat");
    }

    private static GoalIcon createChickenIcon(ResourceKey<ChickenVariant> variant) {
        CompoundTag data = BingoUtil.compound(Map.of(
            "variant", StringTag.valueOf(variant.identifier().toString())
        ));
        return EntityIcon.ofSpawnEgg(EntityTypes.CHICKEN, data);
    }
}
