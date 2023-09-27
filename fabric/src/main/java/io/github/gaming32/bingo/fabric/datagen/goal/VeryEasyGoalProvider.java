package io.github.gaming32.bingo.fabric.datagen.goal;

import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoTags;
import io.github.gaming32.bingo.data.icons.CycleIcon;
import io.github.gaming32.bingo.data.icons.ItemIcon;
import io.github.gaming32.bingo.data.icons.ItemTagCycleIcon;
import io.github.gaming32.bingo.data.subs.BingoSub;
import io.github.gaming32.bingo.data.tags.BingoItemTags;
import io.github.gaming32.bingo.subpredicates.ItemEntityPredicate;
import io.github.gaming32.bingo.triggers.*;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class VeryEasyGoalProvider extends DifficultyGoalProvider {
    public VeryEasyGoalProvider(Consumer<BingoGoal> goalAdder) {
        super(0, "very_easy/", goalAdder);
    }

    @Override
    public void addGoals() {
        addGoal(obtainItemGoal(id("cobblestone"), Items.COBBLESTONE, 32, 64)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("dirt"), Items.DIRT, 32, 64)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("redstone"), Items.REDSTONE).tags(BingoTags.OVERWORLD)
            .infrequency(2));
        addGoal(obtainItemGoal(id("lava_bucket"), Items.LAVA_BUCKET)
            .reactant("use_buckets")
            .infrequency(4));
        addGoal(obtainItemGoal(id("milk_bucket"), Items.MILK_BUCKET)
            .reactant("use_buckets")
            .tags(BingoTags.OVERWORLD)
            .infrequency(4));
        addGoal(obtainItemGoal(id("water_bucket"), Items.WATER_BUCKET)
            .reactant("use_buckets")
            .tags(BingoTags.OVERWORLD)
            .infrequency(4));
        addGoal(
            obtainItemGoal(
                id("fish_bucket"),
                new ItemTagCycleIcon(BingoItemTags.FISH_BUCKETS),
                ItemPredicate.Builder.item().of(BingoItemTags.FISH_BUCKETS)
            )
            .name(Component.translatable("bingo.goal.fish_bucket"))
            .tooltip(Component.translatable("bingo.goal.fish_bucket.tooltip", Component.translatable("advancements.husbandry.tactical_fishing.title")))
            .antisynergy("fish_bucket")
            .reactant("use_buckets")
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD)
            .infrequency(4)
        );
        addGoal(obtainItemGoal(id("andesite"), Items.ANDESITE, 16, 32)
            .infrequency(3)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("granite"), Items.GRANITE, 16, 32)
            .infrequency(3)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("diorite"), Items.DIORITE, 16, 32)
            .infrequency(3)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("iron_block"), Items.IRON_BLOCK)
            .infrequency(2));
        addGoal(BingoGoal.builder(id("poppies_dandelions"))
            .sub("poppies_count", BingoSub.random(5, 25))
            .sub("dandelions_count", BingoSub.random(5, 25))
            .criterion("flowers", TotalCountInventoryChangeTrigger.builder()
                .items(
                    ItemPredicate.Builder.item().of(Items.POPPY).withCount(MinMaxBounds.Ints.atLeast(0)).build(),
                    ItemPredicate.Builder.item().of(Items.DANDELION).withCount(MinMaxBounds.Ints.atLeast(0)).build()
                )
                .build(),
                subber -> subber
                    .sub("conditions.items.0.count.min", "poppies_count")
                    .sub("conditions.items.1.count.min", "dandelions_count")
            )
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.and",
                    Component.translatable("bingo.count", 0, Items.POPPY.getDescription()),
                    Component.translatable("bingo.count", 0, Items.DANDELION.getDescription())),
                subber -> subber.sub("with.0.with.0", "poppies_count").sub("with.1.with.0", "dandelions_count"))
            .icon(new CycleIcon(
                ItemIcon.ofItem(Items.POPPY),
                ItemIcon.ofItem(Items.DANDELION)
            ), subber -> subber.sub("value.0.value.Count", "poppies_count").sub("value.1.value.Count", "dandelions_count")));
        addGoal(obtainLevelsGoal(id("levels"), 5, 7));
        addGoal(obtainItemGoal(id("note_block"), Items.NOTE_BLOCK, 5, 7)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("leaves"), Items.OAK_LEAVES, ItemPredicate.Builder.item().of(ItemTags.LEAVES), 32, 64)
            .tags(BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.leaves", 0), subber -> subber.sub("with.0", "count"))
            .icon(new ItemTagCycleIcon(ItemTags.LEAVES), subber -> subber.sub("count", "count"))
        );
        addGoal(blockCubeGoal(id("leaf_cube"), Blocks.OAK_LEAVES, BlockTags.LEAVES, Component.translatable("bingo.goal.cube.leaf")));
        // TODO: colors of wool
        addGoal(obtainItemGoal(id("snowball"), Items.SNOWBALL, 8, 16)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD));
        // TODO: slabs
        // TODO: stairs
        addGoal(obtainItemGoal(id("diamond"), Items.DIAMOND));
        addGoal(obtainItemGoal(id("rotten_flesh"), Items.ROTTEN_FLESH, 5, 15)
            .infrequency(2));
        addGoal(obtainItemGoal(id("stone"), Items.STONE, 10, 32)
            .tooltip(Component.translatable("bingo.goal.stone.tooltip"))
            .infrequency(2)
            .reactant("use_furnace")
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("bread"), Items.BREAD, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("hay_block"), Items.HAY_BLOCK)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("flower_pot"), Items.FLOWER_POT)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("feather"), Items.FEATHER, 2, 10)
            .infrequency(2));
        addGoal(BingoGoal.builder(id("sleep_in_bed"))
            .criterion("sleep", PlayerTrigger.TriggerInstance.sleptInBed())
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.sleep_in_bed"))
            .icon(Items.RED_BED)
            .reactant("sleep"));
        addGoal(obtainItemGoal(id("charcoal"), Items.CHARCOAL)
            .reactant("use_furnace")
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("coal"), Items.COAL));
        addGoal(obtainItemGoal(id("fishing_rod"), Items.FISHING_ROD));
        addGoal(obtainItemGoal(id("apple"), Items.APPLE)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("stick"), Items.STICK, 32, 64)
            .infrequency(2));
        addGoal(obtainItemGoal(id("kelp"), Items.KELP, 32, 64)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("cod"), Items.COD, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("salmon"), Items.SALMON, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        // TODO: different edible items
        addGoal(BingoGoal.builder(id("breed_mobs"))
            .criterion("breed", BredAnimalsTrigger.TriggerInstance.bredAnimals())
            .name(Component.translatable("bingo.goal.breed_mobs"))
            .tooltip(Component.translatable("bingo.goal.breed_mobs.tooltip"))
            .antisynergy("breed_animals")
            .infrequency(2)
            .tags(BingoTags.ACTION, BingoTags.STAT)
            .icon(Items.WHEAT_SEEDS)
        );
        addGoal(crouchDistanceGoal(id("crouch_distance"), 50, 100));
        // TODO: fill all slots of campfire
        addGoal(BingoGoal.builder(id("dye_sign"))
            .criterion("dye", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(BlockTags.ALL_SIGNS)),
                ItemPredicate.Builder.item().of(BingoItemTags.DYES)
            ))
            .name(Component.translatable("bingo.goal.dye_sign"))
            .tags(BingoTags.ACTION)
            .icon(Items.OAK_SIGN)
        );
        addGoal(BingoGoal.builder(id("extinguish_campfire"))
            .criterion("extinguish", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(Blocks.CAMPFIRE)),
                ItemPredicate.Builder.item().of(ItemTags.SHOVELS)
            ))
            .name(Component.translatable("bingo.goal.extinguish_campfire"))
            .tags(BingoTags.ACTION)
            .icon(Items.CAMPFIRE)
        );
        addGoal(BingoGoal.builder(id("never_pickup_crafting_tables"))
            .criterion("pickup", ItemPickedUpTrigger.TriggerInstance.pickedUp(
                EntityPredicate.Builder.entity().subPredicate(
                    ItemEntityPredicate.item(ItemPredicate.Builder.item().of(Items.CRAFTING_TABLE).build())
                ).build()
            ))
            .tags(BingoTags.NEVER)
            .name(Component.translatable("bingo.goal.never_pickup_crafting_tables"))
            .tooltip(Component.translatable("bingo.goal.never_pickup_crafting_tables.tooltip"))
            .icon(Items.CRAFTING_TABLE));
        // TODO: gold items
        addGoal(obtainItemGoal(id("sand"), Items.SAND, 10, 32)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("sandstone"), Items.SANDSTONE, 5, 10)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("cut_sandstone"), Items.CUT_SANDSTONE, 5, 10)
            .setAntisynergy("sandstone")
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("paper"), Items.PAPER, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("never_fish"))
            .criterion("use", TryUseItemTrigger.builder().item(ItemPredicate.Builder.item().of(Items.FISHING_ROD).build()).build())
            .tags(BingoTags.NEVER, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.never_fish"))
            .tooltip(Component.translatable("bingo.goal.never_fish.tooltip"))
            .icon(Items.FISHING_ROD)
            .catalyst("fishing"));
        addGoal(BingoGoal.builder(id("break_hoe"))
            .criterion("break", ItemBrokenTrigger.TriggerInstance.itemBroken(
                ItemPredicate.Builder.item().of(ItemTags.HOES).build()
            ))
            .tags(BingoTags.ACTION, BingoTags.STAT)
            .name(Component.translatable("bingo.goal.break_hoe"))
            .icon(Items.STONE_HOE));
        addGoal(BingoGoal.builder(id("bounce_on_bed"))
            .criterion("bounce", BingoTriggers.bounceOnBed())
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.bounce_on_bed"))
            .icon(Items.WHITE_BED)
        );
        addGoal(BingoGoal.builder(id("fill_composter"))
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
            .name(Component.translatable("bingo.goal.fill_composter"))
            .tooltip(Component.translatable("bingo.goal.fill_composter.tooltip"))
            .tags(BingoTags.ACTION)
            .icon(Blocks.COMPOSTER.defaultBlockState().setValue(ComposterBlock.LEVEL, 8))
        );

        for (String woodType : List.of("oak", "spruce", "birch", "dark_oak", "acacia", "cherry")) {
            Item planksItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_planks"));
            addGoal(obtainItemGoal(id(woodType + "_planks"), planksItem, 32, 64)
                .tags(BingoTags.OVERWORLD)
                .infrequency(25));

            Item logItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_log"));
            addGoal(obtainItemGoal(id(woodType + "_log"), logItem, 5, 15)
                .tags(BingoTags.OVERWORLD)
                .infrequency(25));

            Item woodItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_wood"));
            addGoal(obtainItemGoal(id(woodType + "_wood"), woodItem, 5, 10)
                .infrequency(25)
                .tags(BingoTags.OVERWORLD));

            Item strippedWoodItem = BuiltInRegistries.ITEM.get(new ResourceLocation("stripped_" + woodType + "_wood"));
            addGoal(obtainItemGoal(id("stripped_" + woodType + "_wood"), strippedWoodItem, 5, 10)
                .reactant("axe_use")
                .infrequency(25)
                .tags(BingoTags.OVERWORLD));

            Item strippedLogItem = BuiltInRegistries.ITEM.get(new ResourceLocation("stripped_" + woodType + "_log"));
            addGoal(obtainItemGoal(id("stripped_" + woodType + "_log"), strippedLogItem, 5, 15)
                .reactant("axe_use")
                .infrequency(25)
                .tags(BingoTags.OVERWORLD));
        }
    }
}
