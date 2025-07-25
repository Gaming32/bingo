package io.github.gaming32.bingo.fabric.datagen.goal;

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
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.BredAnimalsTrigger;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemDurabilityTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

public class VeryEasyGoalProvider extends DifficultyGoalProvider {
    public VeryEasyGoalProvider(BiConsumer<ResourceLocation, BingoGoal> goalAdder, HolderLookup.Provider registries) {
        super(BingoDifficulties.VERY_EASY, goalAdder, registries);
    }

    @Override
    public void addGoals() {
        final var items = registries.lookupOrThrow(Registries.ITEM);
        final var blocks = registries.lookupOrThrow(Registries.BLOCK);

        addGoal(obtainItemGoal(id("cobblestone"), items, Items.COBBLESTONE, 32, 64)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("dirt"), items, Items.DIRT, 32, 64)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("redstone"), items, Items.REDSTONE).tags(BingoTags.OVERWORLD)
            .infrequency(2));
        addGoal(obtainItemGoal(id("lava_bucket"), items, Items.LAVA_BUCKET)
            .reactant("use_buckets")
            .infrequency(4));
        addGoal(obtainItemGoal(id("milk_bucket"), items, Items.MILK_BUCKET)
            .reactant("use_buckets")
            .tags(BingoTags.OVERWORLD)
            .infrequency(4));
        addGoal(obtainItemGoal(id("water_bucket"), items, Items.WATER_BUCKET)
            .reactant("use_buckets")
            .tags(BingoTags.OVERWORLD)
            .infrequency(4));
        addGoal(
            obtainItemGoal(
                id("fish_bucket"),
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
        addGoal(obtainItemGoal(id("andesite"), items, Items.ANDESITE, 16, 32)
            .infrequency(3)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("granite"), items, Items.GRANITE, 16, 32)
            .infrequency(3)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("diorite"), items, Items.DIORITE, 16, 32)
            .infrequency(3)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("iron_block"), items, Items.IRON_BLOCK)
            .infrequency(2));
        addGoal(BingoGoal.builder(id("poppies_dandelions"))
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
                    Component.translatable("bingo.count", 0, Items.POPPY.getName()),
                    Component.translatable("bingo.count", 0, Items.DANDELION.getName())),
                subber -> subber.sub("with.0.with.0", "poppies_count").sub("with.1.with.0", "dandelions_count"))
            .icon(new CycleIcon(
                ItemIcon.ofItem(Items.POPPY),
                ItemIcon.ofItem(Items.DANDELION)
            ), subber -> subber.sub("icons.0.item.count", "poppies_count").sub("icons.1.item.count", "dandelions_count")));
        addGoal(obtainLevelsGoal(id("levels"), 5, 7));
        addGoal(obtainItemGoal(id("note_block"), items, Items.NOTE_BLOCK, 5, 7)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("leaves"), Items.OAK_LEAVES, ItemPredicate.Builder.item().of(items, ItemTags.LEAVES), 32, 64)
            .tags(BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.leaves", 0), subber -> subber.sub("with.0", "count"))
            .icon(new ItemTagCycleIcon(ItemTags.LEAVES), subber -> subber.sub("+count", "count"))
        );
        addGoal(blockCubeGoal(
            id("leaf_cube"),
            makeItemWithGlint(Blocks.OAK_LEAVES),
            BlockTags.LEAVES,
            Component.translatable("bingo.goal.cube.leaf")
        ));
        addGoal(obtainSomeItemsFromTag(id("wool_colors"), ItemTags.WOOL, "bingo.goal.wool_colors", 2, 4)
            .antisynergy("wool_color")
            .infrequency(4)
            .tags(BingoTags.COLOR, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("snowball"), items, Items.SNOWBALL, 8, 16)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD));
        addGoal(obtainSomeItemsFromTag(id("different_slabs"), ItemTags.SLABS, "bingo.goal.different_slabs", 2, 4)
            .antisynergy("slabs")
            .infrequency(2));
        addGoal(obtainSomeItemsFromTag(id("different_stairs"), ItemTags.STAIRS, "bingo.goal.different_stairs", 2, 4)
            .antisynergy("stairs")
            .infrequency(2));
        addGoal(obtainItemGoal(id("diamond"), items, Items.DIAMOND));
        addGoal(obtainItemGoal(id("rotten_flesh"), items, Items.ROTTEN_FLESH, 5, 15)
            .infrequency(2));
        addGoal(obtainItemGoal(id("stone"), items, Items.STONE, 10, 32)
            .tooltip("stone")
            .infrequency(2)
            .reactant("use_furnace")
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("bread"), items, Items.BREAD, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("hay_block"), items, Items.HAY_BLOCK)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("flower_pot"), items, Items.FLOWER_POT)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("feather"), items, Items.FEATHER, 2, 10)
            .infrequency(2));
        addGoal(BingoGoal.builder(id("sleep_in_bed"))
            .criterion("sleep", PlayerTrigger.TriggerInstance.sleptInBed())
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name("sleep_in_bed")
            .icon(Items.RED_BED)
            .reactant("sleep"));
        addGoal(obtainItemGoal(id("charcoal"), items, Items.CHARCOAL)
            .reactant("use_furnace")
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("coal"), items, Items.COAL));
        addGoal(obtainItemGoal(id("fishing_rod"), items, Items.FISHING_ROD));
        addGoal(obtainItemGoal(id("apple"), items, Items.APPLE)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("stick"), items, Items.STICK, 32, 64)
            .infrequency(2));
        addGoal(obtainItemGoal(id("kelp"), items, Items.KELP, 32, 64)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("cod"), items, Items.COD, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("salmon"), items, Items.SALMON, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD));
        addGoal(obtainSomeEdibleItems(id("edible_items"), 2, 3));
        addGoal(BingoGoal.builder(id("breed_mob_pair"))
            .criterion("breed", BredAnimalsTrigger.TriggerInstance.bredAnimals())
            .name("breed_mob_pair")
            .tooltip("breed_mobs")
            .antisynergy("breed_animals")
            .infrequency(2)
            .tags(BingoTags.ACTION, BingoTags.STAT)
            .icon(Items.WHEAT_SEEDS)
        );
        addGoal(crouchDistanceGoal(id("crouch_distance"), 50, 100));
        {
            final List<CompoundTag> slots = IntStream.range(0, 4).mapToObj(slot -> BingoUtil.compound(Map.of("Slot", ByteTag.valueOf((byte) slot)))).toList();
            addGoal(BingoGoal.builder(id("fill_all_campfire_slots"))
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
        addGoal(BingoGoal.builder(id("dye_sign"))
            .criterion("dye", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(blocks, BlockTags.ALL_SIGNS)),
                ItemPredicate.Builder.item().of(items, ConventionalItemTags.DYES)
            ))
            .name("dye_sign")
            .tags(BingoTags.ACTION)
            .icon(Items.OAK_SIGN)
        );
        addGoal(BingoGoal.builder(id("extinguish_campfire"))
            .criterion("extinguish", ItemUsedOnLocationTrigger.TriggerInstance.itemUsedOnBlock(
                LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(blocks, Blocks.CAMPFIRE)),
                ItemPredicate.Builder.item().of(items, ItemTags.SHOVELS)
            ))
            .name("extinguish_campfire")
            .tags(BingoTags.ACTION)
            .icon(Items.CAMPFIRE)
        );
        addGoal(BingoGoal.builder(id("never_pickup_crafting_tables"))
            .criterion("pickup", ItemPickedUpTrigger.TriggerInstance.pickedUp(
                EntityPredicate.Builder.entity().subPredicate(
                    ItemEntityPredicate.item(ItemPredicate.Builder.item().of(items, Items.CRAFTING_TABLE).build())
                ).build()
            ))
            .tags(BingoTags.NEVER).name("never_pickup_crafting_tables")
            .tooltip("never_pickup_crafting_tables")
            .icon(Items.CRAFTING_TABLE));
        addGoal(obtainSomeItemsFromTag(id("gold_in_name"), BingoItemTags.GOLD_IN_NAME, "bingo.goal.gold_in_name", 2, 4)
            .tooltip("gold_in_name")
            .antisynergy("gold_items"));
        addGoal(obtainItemGoal(id("sand"), items, Items.SAND, 10, 32)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("sandstone"), items, Items.SANDSTONE, 5, 10)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("cut_sandstone"), items, Items.CUT_SANDSTONE, 5, 10)
            .setAntisynergy("sandstone")
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(obtainItemGoal(id("paper"), items, Items.PAPER, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD));
        addGoal(BingoGoal.builder(id("never_fish"))
            .criterion("use", TryUseItemTrigger.builder().item(ItemPredicate.Builder.item().of(items, Items.FISHING_ROD).build()).build())
            .tags(BingoTags.NEVER, BingoTags.OVERWORLD)
            .name("never_fish").tooltip("never_fish")
            .icon(Items.FISHING_ROD)
            .catalyst("fishing"));
        addGoal(BingoGoal.builder(id("break_hoe"))
            .criterion("break", ItemDurabilityTrigger.TriggerInstance.changedDurability(
                Optional.of(ItemPredicate.Builder.item().of(items, ItemTags.HOES).build()),
                MinMaxBounds.Ints.atMost(0)
            ))
            .tags(BingoTags.ACTION, BingoTags.STAT)
            .name("break_hoe")
            .icon(Items.STONE_HOE));
        addGoal(BingoGoal.builder(id("bounce_on_bed"))
            .criterion("bounce", BounceOnBlockTrigger.TriggerInstance.bounceOnBlock(
                BlockPredicate.Builder.block().of(blocks, BlockTags.BEDS)
            ))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD)
            .name("bounce_on_bed")
            .icon(Items.WHITE_BED)
        );
        addGoal(BingoGoal.builder(id("hang_painting"))
            .criterion("hang", AdjacentPaintingTrigger.builder().count(MinMaxBounds.Ints.atLeast(1)).build())
            .name("hang_painting")
            .icon(Items.PAINTING)
            .antisynergy("painting")
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD));
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
