package io.github.gaming32.bingo.datagen.goal;

import io.github.gaming32.bingo.data.BingoDifficulties;
import io.github.gaming32.bingo.data.BingoTags;
import io.github.gaming32.bingo.data.goal.BingoGoal;
import io.github.gaming32.bingo.data.icons.CycleIcon;
import io.github.gaming32.bingo.data.icons.ItemIcon;
import io.github.gaming32.bingo.data.icons.ItemTagCycleIcon;
import io.github.gaming32.bingo.data.subs.BingoSub;
import io.github.gaming32.bingo.data.tags.bingo.BingoItemTags;
import io.github.gaming32.bingo.subpredicates.ItemEntityPredicate;
import io.github.gaming32.bingo.triggers.AdjacentPaintingTrigger;
import io.github.gaming32.bingo.triggers.BounceOnBlockTrigger;
import io.github.gaming32.bingo.triggers.ItemPickedUpTrigger;
import io.github.gaming32.bingo.triggers.TotalCountInventoryChangeTrigger;
import io.github.gaming32.bingo.triggers.TryUseItemTrigger;
import io.github.gaming32.bingo.util.BingoUtil;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.advancements.criterion.BredAnimalsTrigger;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemDurabilityTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.PlayerTrigger;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import static io.github.gaming32.bingo.datagen.goal.GoalIds.VeryEasy.*;

public class VeryEasyGoalProvider extends DifficultyGoalProvider {
    public VeryEasyGoalProvider(BiConsumer<Identifier, BingoGoal> goalAdder, HolderLookup.Provider registries) {
        super(BingoDifficulties.VERY_EASY, goalAdder, registries);
    }

    @Override
    public void addGoals() {
        final var items = registries.lookupOrThrow(Registries.ITEM);
        final var blocks = registries.lookupOrThrow(Registries.BLOCK);

        addGoal(obtainItemGoal(COBBLESTONE, items, Items.COBBLESTONE, 32, 64)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(DIRT, items, Items.DIRT, 32, 64)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(REDSTONE, items, Items.REDSTONE).tags(BingoTags.OVERWORLD)
            .infrequency(2));
        addGoal(obtainItemGoal(LAVA_BUCKET, items, Items.LAVA_BUCKET)
            .reactant("use_buckets")
            .infrequency(4));
        addGoal(obtainItemGoal(MILK_BUCKET, items, Items.MILK_BUCKET)
            .reactant("use_buckets")
            .tags(BingoTags.OVERWORLD)
            .infrequency(4));
        addGoal(obtainItemGoal(WATER_BUCKET, items, Items.WATER_BUCKET)
            .reactant("use_buckets")
            .tags(BingoTags.OVERWORLD)
            .infrequency(4));
        addGoal(
            obtainItemGoal(
                FISH_BUCKET,
                items,
                new ItemTagCycleIcon(BingoItemTags.FISH_BUCKETS),
                ItemPredicate.Builder.item().of(items, BingoItemTags.FISH_BUCKETS)
            )
            .name("fish_bucket")
            .tooltip(Component.translatable(
                "bingo.goal.fish_bucket.tooltip",
                Component.translatable("advancements.husbandry.tactical_fishing.title")
            ))
            .antisynergy("fish_bucket")
            .reactant("use_buckets")
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD)
            .infrequency(4)
        );
        addGoal(obtainItemGoal(ANDESITE, items, Items.ANDESITE, 16, 32)
            .infrequency(3)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(GRANITE, items, Items.GRANITE, 16, 32)
            .infrequency(3)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(DIORITE, items, Items.DIORITE, 16, 32)
            .infrequency(3)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(IRON_BLOCK, items, Items.IRON_BLOCK)
            .infrequency(2));
        addGoal(BingoGoal.builder(POPPIES_DANDELIONS)
            .sub("poppies_count", BingoSub.random(5, 25))
            .sub("dandelions_count", BingoSub.random(5, 25))
            .criterion("flowers", TotalCountInventoryChangeTrigger.builder()
                .items(
                    ItemPredicate.Builder.item().of(items, Items.POPPY).withCount(MinMaxBounds.Ints.atLeast(0)).build(),
                    ItemPredicate.Builder.item().of(items, Items.DANDELION).withCount(MinMaxBounds.Ints.atLeast(0)).build()
                )
                .build(),
                subber -> subber
                    .sub("conditions.items.0.count.min", "poppies_count")
                    .sub("conditions.items.1.count.min", "dandelions_count")
            )
            .progress("flowers")
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.and",
                    Component.translatable("bingo.count", 0, Component.translatable(Items.POPPY.getDescriptionId())),
                    Component.translatable("bingo.count", 0, Component.translatable(Items.DANDELION.getDescriptionId()))),
                subber -> subber.sub("with.0.with.0", "poppies_count").sub("with.1.with.0", "dandelions_count"))
            .icon(new CycleIcon(
                ItemIcon.ofItem(Items.POPPY),
                ItemIcon.ofItem(Items.DANDELION)
            ), subber -> subber.sub("icons.0.item.count", "poppies_count").sub("icons.1.item.count", "dandelions_count")));
        addGoal(obtainLevelsGoal(LEVELS, 5, 7));
        addGoal(obtainItemGoal(NOTE_BLOCK, items, Items.NOTE_BLOCK, 5, 7)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(LEAVES, Items.OAK_LEAVES, ItemPredicate.Builder.item().of(items, ItemTags.LEAVES), 32, 64)
            .tags(BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.leaves", 0), subber -> subber.sub("with.0", "count"))
            .icon(new ItemTagCycleIcon(ItemTags.LEAVES), subber -> subber.sub("+count", "count"))
        );
        addGoal(blockCubeGoal(
            LEAF_CUBE,
            makeItemWithGlint(Blocks.OAK_LEAVES),
            BlockTags.LEAVES,
            Component.translatable("bingo.goal.cube.leaf")
        ));
        addGoal(obtainSomeItemsFromTag(WOOL_COLORS, ItemTags.WOOL, "bingo.goal.wool_colors", 2, 4)
            .antisynergy("wool_color")
            .infrequency(4)
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(SNOWBALL, items, Items.SNOWBALL, 8, 16)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD));
        addGoal(obtainSomeItemsFromTag(DIFFERENT_SLABS, ItemTags.SLABS, "bingo.goal.different_slabs", 2, 4)
            .antisynergy("slabs")
            .infrequency(2));
        addGoal(obtainSomeItemsFromTag(DIFFERENT_STAIRS, ItemTags.STAIRS, "bingo.goal.different_stairs", 2, 4)
            .antisynergy("stairs")
            .infrequency(2));
        addGoal(obtainItemGoal(DIAMOND, items, Items.DIAMOND));
        addGoal(obtainItemGoal(ROTTEN_FLESH, items, Items.ROTTEN_FLESH, 5, 15)
            .infrequency(2));
        addGoal(obtainItemGoal(STONE, items, Items.STONE, 10, 32)
            .tooltip("stone")
            .infrequency(2)
            .reactant("use_furnace")
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(BREAD, items, Items.BREAD, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(HAY_BLOCK, items, Items.HAY_BLOCK)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(FLOWER_POT, items, Items.FLOWER_POT)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(FEATHER, items, Items.FEATHER, 2, 10)
            .infrequency(2));
        addGoal(BingoGoal.builder(SLEEP_IN_BED)
            .criterion("sleep", PlayerTrigger.TriggerInstance.sleptInBed())
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name("sleep_in_bed")
            .icon(Items.RED_BED)
            .reactant("sleep"));
        addGoal(obtainItemGoal(CHARCOAL, items, Items.CHARCOAL)
            .reactant("use_furnace")
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(COAL, items, Items.COAL));
        addGoal(obtainItemGoal(FISHING_ROD, items, Items.FISHING_ROD));
        addGoal(obtainItemGoal(APPLE, items, Items.APPLE)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(STICK, items, Items.STICK, 32, 64)
            .infrequency(2));
        addGoal(obtainItemGoal(KELP, items, Items.KELP, 32, 64)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(COD, items, Items.COD, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(SALMON, items, Items.SALMON, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        addGoal(obtainSomeEdibleItems(EDIBLE_ITEMS, 2, 3));
        addGoal(BingoGoal.builder(BREED_MOB_PAIR)
            .criterion("breed", BredAnimalsTrigger.TriggerInstance.bredAnimals())
            .name("breed_mob_pair")
            .tooltip("breed_mobs")
            .antisynergy("breed_animals")
            .infrequency(2)
            .tags(BingoTags.ACTION, BingoTags.STAT)
            .icon(Items.WHEAT_SEEDS)
        );
        addGoal(crouchDistanceGoal(CROUCH_DISTANCE, 50, 100));
        {
            final List<CompoundTag> slots = IntStream.range(0, 4).mapToObj(slot -> BingoUtil.compound(Map.of("Slot", ByteTag.valueOf((byte) slot)))).toList();
            addGoal(BingoGoal.builder(FILL_ALL_CAMPFIRE_SLOTS)
                .criterion("place", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                    LocationPredicate.Builder.location().setBlock(
                        BlockPredicate.Builder.block()
                            .of(blocks, Blocks.CAMPFIRE)
                            .hasNbt(BingoUtil.compound(Map.of(
                                "Items",
                                BingoUtil.list(slots)
                            )))
                    ),
                    ItemPredicate.Builder.item()
                ))
                .name(Component.translatable("bingo.goal.fill_all_campfire_slots", Blocks.CAMPFIRE.getName()))
                .icon(makeItemWithGlint(Items.CAMPFIRE))
                .tags(BingoTags.ACTION));
        }
        addGoal(BingoGoal.builder(DYE_SIGN)
            .criterion("dye", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(blocks, BlockTags.ALL_SIGNS)),
                ItemPredicate.Builder.item().of(items, ConventionalItemTags.DYES)
            ))
            .name("dye_sign")
            .tags(BingoTags.ACTION)
            .icon(Items.OAK_SIGN)
        );
        addGoal(BingoGoal.builder(EXTINGUISH_CAMPFIRE)
            .criterion("extinguish", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(blocks, Blocks.CAMPFIRE)),
                ItemPredicate.Builder.item().of(items, ItemTags.SHOVELS)
            ))
            .name("extinguish_campfire")
            .tags(BingoTags.ACTION)
            .icon(Items.CAMPFIRE)
        );
        addGoal(BingoGoal.builder(NEVER_PICKUP_CRAFTING_TABLES)
            .criterion("pickup", ItemPickedUpTrigger.TriggerInstance.pickedUp(
                EntityPredicate.Builder.entity().subPredicate(
                    ItemEntityPredicate.item(ItemPredicate.Builder.item().of(items, Items.CRAFTING_TABLE).build())
                ).build()
            ))
            .tags(BingoTags.NEVER).name("never_pickup_crafting_tables")
            .tooltip("never_pickup_crafting_tables")
            .icon(Items.CRAFTING_TABLE));
        addGoal(obtainSomeItemsFromTag(GOLD_IN_NAME, BingoItemTags.GOLD_IN_NAME, "bingo.goal.gold_in_name", 2, 4)
            .tooltip("gold_in_name")
            .antisynergy("gold_items"));
        addGoal(obtainSomeItemsFromTag(COPPER_IN_NAME, BingoItemTags.COPPER_IN_NAME, "bingo.goal.copper_in_name", 2, 4)
            .tooltip("copper_in_name")
            .antisynergy("copper_items"));
        addGoal(obtainItemGoal(SAND, items, Items.SAND, 10, 32)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(SANDSTONE, items, Items.SANDSTONE, 5, 10)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(CUT_SANDSTONE, items, Items.CUT_SANDSTONE, 5, 10)
            .setAntisynergy("sandstone")
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(PAPER, items, Items.PAPER, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(NEVER_FISH)
            .criterion("use", TryUseItemTrigger.builder().item(ItemPredicate.Builder.item().of(items, Items.FISHING_ROD).build()).build())
            .tags(BingoTags.NEVER, BingoTags.OVERWORLD)
            .name("never_fish").tooltip("never_fish")
            .icon(Items.FISHING_ROD)
            .catalyst("fishing"));
        addGoal(BingoGoal.builder(BREAK_HOE)
            .criterion("break", ItemDurabilityTrigger.TriggerInstance.changedDurability(
                Optional.of(ItemPredicate.Builder.item().of(items, ItemTags.HOES).build()),
                MinMaxBounds.Ints.atMost(0)
            ))
            .tags(BingoTags.ACTION, BingoTags.STAT)
            .name("break_hoe")
            .icon(Items.STONE_HOE));
        addGoal(BingoGoal.builder(BOUNCE_ON_BED)
            .criterion("bounce", BounceOnBlockTrigger.TriggerInstance.bounceOnBlock(
                BlockPredicate.Builder.block().of(blocks, BlockTags.BEDS)
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name("bounce_on_bed")
            .icon(Items.WHITE_BED)
        );
        addGoal(BingoGoal.builder(HANG_PAINTING)
            .criterion("hang", AdjacentPaintingTrigger.builder().count(MinMaxBounds.Ints.atLeast(1)).build())
            .name("hang_painting")
            .icon(Items.PAINTING)
            .antisynergy("painting")
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(FILL_COMPOSTER)
            .criterion("fill", CriteriaTriggers.ITEM_USED_ON_BLOCK.createCriterion(
                new ItemUsedOnLocationTrigger.TriggerInstance(
                    Optional.empty(),
                    Optional.of(
                        ContextAwarePredicate.create(LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.COMPOSTER)
                            .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(ComposterBlock.LEVEL, 7))
                            .build()
                        )
                    )
                )
            ))
            .name("fill_composter")
            .tooltip("fill_composter")
            .tags(BingoTags.ACTION)
            .icon(Blocks.COMPOSTER.defaultBlockState().setValue(ComposterBlock.LEVEL, 8))
        );

        for (String woodType : List.of("oak", "spruce", "birch", "dark_oak", "acacia", "cherry")) {
            addGoal(obtainItemGoal(id(woodType + "_planks"), items, itemResource(woodType + "_planks"), 32, 64)
                .tags(BingoTags.OVERWORLD)
                .infrequency(25));

            addGoal(obtainItemGoal(id(woodType + "_log"), items, itemResource(woodType + "_log"), 5, 15)
                .tags(BingoTags.OVERWORLD)
                .infrequency(25));

            addGoal(obtainItemGoal(id(woodType + "_wood"), items, itemResource(woodType + "_wood"), 5, 10)
                .infrequency(25)
                .tags(BingoTags.OVERWORLD));

            addGoal(obtainItemGoal(id("stripped_" + woodType + "_wood"), items, itemResource("stripped_" + woodType + "_wood"), 5, 10)
                .reactant("axe_use")
                .infrequency(25)
                .tags(BingoTags.OVERWORLD));

            addGoal(obtainItemGoal(id("stripped_" + woodType + "_log"), items, itemResource("stripped_" + woodType + "_log"), 5, 15)
                .reactant("axe_use")
                .infrequency(25)
                .tags(BingoTags.OVERWORLD));
        }
    }
}
