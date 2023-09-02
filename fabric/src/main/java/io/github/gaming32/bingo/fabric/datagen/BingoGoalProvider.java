package io.github.gaming32.bingo.fabric.datagen;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoItemTags;
import io.github.gaming32.bingo.data.BingoSub;
import io.github.gaming32.bingo.data.BingoTags;
import io.github.gaming32.bingo.triggers.ExperienceChangeTrigger;
import io.github.gaming32.bingo.triggers.TryUseItemTrigger;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PickedUpItemTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class BingoGoalProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public BingoGoalProvider(FabricDataOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "bingo/goals");
    }

    @Override
    @NotNull
    public CompletableFuture<?> run(CachedOutput output) {
        Set<ResourceLocation> existingGoals = new HashSet<>();
        List<CompletableFuture<?>> generators = new ArrayList<>();

        Consumer<BingoGoal> goalAdder = goal -> {
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

    private static void addGoals(Consumer<BingoGoal> goalAdder) {
        addVeryEasyGoals(goalAdder);
    }

    private static void addVeryEasyGoals(Consumer<BingoGoal> goalAdder) {
        goalAdder.accept(obtainItemGoal(veryEasyId("cobblestone"), Items.COBBLESTONE, 32, 64)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("dirt"), Items.DIRT, 32, 64)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("redstone"), Items.REDSTONE).tags(BingoTags.OVERWORLD)
            .infrequency(2)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("lava_bucket"), Items.LAVA_BUCKET)
            .reactant("use_buckets")
            .infrequency(4)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("milk_bucket"), Items.MILK_BUCKET)
            .reactant("use_buckets")
            .tags(BingoTags.OVERWORLD)
            .infrequency(4)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("water_bucket"), Items.WATER_BUCKET)
            .reactant("use_buckets")
            .tags(BingoTags.OVERWORLD)
            .infrequency(4)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("fish_bucket"), Items.TROPICAL_FISH_BUCKET, ItemPredicate.Builder.item().of(BingoItemTags.FISH_BUCKETS))
            .name(Component.translatable("bingo.goal.fish_bucket"))
            .tooltip(Component.translatable("bingo.goal.fish_bucket.tooltip", Component.translatable("advancements.husbandry.tactical_fishing.title")))
            .antisynergy("fish_bucket")
            .reactant("use_buckets")
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD)
            .infrequency(4)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("andesite"), Items.ANDESITE, 16, 32)
            .infrequency(3)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("granite"), Items.GRANITE, 16, 32)
            .infrequency(3)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("diorite"), Items.DIORITE, 16, 32)
            .infrequency(3)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("iron_block"), Items.IRON_BLOCK)
            .infrequency(2)
            .difficulty(0)
            .build());
        goalAdder.accept(BingoGoal.builder(veryEasyId("poppies_dandelions"))
            .sub("poppies_count", new BingoSub.RandomBingoSub(MinMaxBounds.Ints.between(5, 25)))
            .sub("dandelions_count", new BingoSub.RandomBingoSub(MinMaxBounds.Ints.between(5, 25)))
            .criterion("poppy", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(Items.POPPY).withCount(MinMaxBounds.Ints.exactly(0)).build()),
                subber -> subber.sub("conditions.items.0.count", "poppies_count"))
            .criterion("dandelion", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(Items.DANDELION).withCount(MinMaxBounds.Ints.exactly(0)).build()),
                subber -> subber.sub("conditions.items.0.count", "dandelions_count"))
            .tags(BingoTags.ITEM, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.and",
                    Component.translatable("bingo.count", 0, Items.POPPY.getDescription()),
                    Component.translatable("bingo.count", 0, Items.DANDELION.getDescription())),
                subber -> subber.sub("with.0.with.0", "poppies_count").sub("with.1.with.0", "dandelions_count"))
            .icon(Items.POPPY)
            .difficulty(0)
            .build());
        goalAdder.accept(BingoGoal.builder(veryEasyId("levels"))
            .sub("count", new BingoSub.RandomBingoSub(MinMaxBounds.Ints.between(5, 7)))
            .criterion("obtain", ExperienceChangeTrigger.builder().levels(MinMaxBounds.Ints.atLeast(0)).build(),
                subber -> subber.sub("conditions.levels.min", "count"))
            .tags(BingoTags.STAT)
            .name(Component.translatable("bingo.goal.levels", 0), subber -> subber.sub("with.0", "count"))
            .icon(Items.EXPERIENCE_BOTTLE, subber -> subber.sub("count", "count"))
            .infrequency(2)
            .antisynergy("levels")
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("note_block"), Items.NOTE_BLOCK, 5, 7)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("leaves"), Items.OAK_LEAVES, ItemPredicate.Builder.item().of(ItemTags.LEAVES), 32, 64)
            .name(Component.translatable("bingo.goal.leaves"))
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        // TODO: leaf cube
        // TODO: colors of wool
        goalAdder.accept(obtainItemGoal(veryEasyId("snowball"), Items.SNOWBALL, 8, 16)
            .tags(BingoTags.RARE_BIOME, BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        // TODO: slabs
        // TODO: stairs
        goalAdder.accept(obtainItemGoal(veryEasyId("diamond"), Items.DIAMOND)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("rotten_flesh"), Items.ROTTEN_FLESH, 5, 15)
            .infrequency(2)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("stone"), Items.STONE, 10, 32)
            .tooltip(Component.translatable("bingo.goal.stone.tooltip"))
            .infrequency(2)
            .reactant("use_furnace")
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("bread"), Items.BREAD, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("hay_block"), Items.HAY_BLOCK)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("flower_pot"), Items.FLOWER_POT)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("feather"), Items.FEATHER, 2, 10)
            .infrequency(2)
            .difficulty(0)
            .build());
        // TODO: sleep in a bed
        goalAdder.accept(obtainItemGoal(veryEasyId("charcoal"), Items.CHARCOAL)
            .reactant("use_furnace")
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("coal"), Items.COAL)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("fishing_rod"), Items.FISHING_ROD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("apple"), Items.APPLE)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("stick"), Items.STICK, 32, 64)
            .infrequency(2)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("kelp"), Items.KELP, 32, 64)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("cod"), Items.COD, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("salmon"), Items.SALMON, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OCEAN, BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        // TODO: different edible items
        // TODO: breed a set of mobs
        // TODO: crouch distance
        // TODO: fill all slots of campfire
        // TODO: change sign color
        // TODO: extinguish campfire
        goalAdder.accept(BingoGoal.builder(veryEasyId("never_pickup_crafting_tables"))
            .criterion("pickup", PickedUpItemTrigger.TriggerInstance.thrownItemPickedUpByPlayer(
                ContextAwarePredicate.ANY, ItemPredicate.Builder.item().of(Items.CRAFTING_TABLE).build(), ContextAwarePredicate.ANY))
            .tags(BingoTags.NEVER)
            .name(Component.translatable("bingo.goal.never_pickup_crafting_tables"))
            .tooltip(Component.translatable("bingo.goal.never_pickup_crafting_tables.tooltip"))
            .icon(Items.CRAFTING_TABLE)
            .difficulty(0)
            .build());
        // TODO: gold items
        goalAdder.accept(obtainItemGoal(veryEasyId("sand"), Items.SAND, 10, 32)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("sandstone"), Items.SANDSTONE, 5, 10)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("cut_sandstone"), Items.CUT_SANDSTONE, 5, 10)
            .setAntisynergy("sandstone")
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(obtainItemGoal(veryEasyId("paper"), Items.PAPER, 2, 5)
            .infrequency(2)
            .tags(BingoTags.OVERWORLD)
            .difficulty(0)
            .build());
        goalAdder.accept(BingoGoal.builder(veryEasyId("never_fish"))
            .criterion("use", TryUseItemTrigger.builder().item(ItemPredicate.Builder.item().of(Items.FISHING_ROD).build()).build())
            .tags(BingoTags.NEVER, BingoTags.OVERWORLD)
            .name(Component.translatable("bingo.goal.never_fish"))
            .tooltip(Component.translatable("bingo.goal.never_fish.tooltip"))
            .icon(Items.FISHING_ROD)
            .catalyst("fishing")
            .difficulty(0)
            .build());
        // TODO: deplete hoe
        // TODO: bounce on bed
        // TODO: hang painting
        // TODO: fill composter

        for (String woodType : List.of("oak", "spruce", "birch", "dark_oak", "acacia")) {
            Item planksItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_planks"));
            goalAdder.accept(obtainItemGoal(veryEasyId(woodType + "_planks"), planksItem, 32, 64)
                .tags(BingoTags.OVERWORLD)
                .infrequency(25)
                .difficulty(0)
                .build());

            Item logItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_log"));
            goalAdder.accept(obtainItemGoal(veryEasyId(woodType + "_log"), logItem, 5, 15)
                .tags(BingoTags.OVERWORLD)
                .infrequency(25)
                .difficulty(0)
                .build());

            Item woodItem = BuiltInRegistries.ITEM.get(new ResourceLocation(woodType + "_wood"));
            goalAdder.accept(obtainItemGoal(veryEasyId(woodType + "_wood"), woodItem, 5, 10)
                .infrequency(25)
                .tags(BingoTags.OVERWORLD)
                .difficulty(0)
                .build());

            Item strippedWoodItem = BuiltInRegistries.ITEM.get(new ResourceLocation("stripped_" + woodType + "_wood"));
            goalAdder.accept(obtainItemGoal(veryEasyId("stripped_" + woodType + "_wood"), strippedWoodItem, 5, 10)
                .reactant("axe_use")
                .infrequency(25)
                .tags(BingoTags.OVERWORLD)
                .difficulty(0)
                .build());

            Item strippedLogItem = BuiltInRegistries.ITEM.get(new ResourceLocation("stripped_" + woodType + "_log"));
            goalAdder.accept(obtainItemGoal(veryEasyId("stripped_" + woodType + "_log"), strippedLogItem, 5, 10)
                .reactant("axe_use")
                .infrequency(25)
                .tags(BingoTags.OVERWORLD)
                .difficulty(0)
                .build());
        }
    }

    private static ResourceLocation veryEasyId(String id) {
        return new ResourceLocation(Bingo.MOD_ID, "very_easy/" + id);
    }

    private static ResourceLocation easyId(String id) {
        return new ResourceLocation(Bingo.MOD_ID, "easy/" + id);
    }

    private static ResourceLocation mediumId(String id) {
        return new ResourceLocation(Bingo.MOD_ID, "medium/" + id);
    }

    private static ResourceLocation hardId(String id) {
        return new ResourceLocation(Bingo.MOD_ID, "hard/" + id);
    }

    private static ResourceLocation veryHardId(String id) {
        return new ResourceLocation(Bingo.MOD_ID, "very_hard/" + id);
    }

    public static BingoGoal.Builder obtainItemGoal(ResourceLocation id, ItemLike item) {
        return obtainItemGoal(id, item, ItemPredicate.Builder.item().of(item))
            .antisynergy(BuiltInRegistries.ITEM.getKey(item.asItem()).getPath())
            .name(item.asItem().getDescription());
    }

    private static BingoGoal.Builder obtainItemGoal(ResourceLocation id, ItemLike icon, ItemPredicate.Builder item) {
        return BingoGoal.builder(id)
            .criterion("obtain", InventoryChangeTrigger.TriggerInstance.hasItems(item.build()))
            .tags(BingoTags.ITEM)
            .icon(icon);
    }

    public static BingoGoal.Builder obtainItemGoal(ResourceLocation id, ItemLike item, int minCount, int maxCount) {
        return obtainItemGoal(id, item, ItemPredicate.Builder.item().of(item), minCount, maxCount)
            .antisynergy(BuiltInRegistries.ITEM.getKey(item.asItem()).getPath())
            .name(Component.translatable("bingo.count", 0, item.asItem().getDescription()),
                subber -> subber.sub("with.0", "count"));
    }

    private static BingoGoal.Builder obtainItemGoal(ResourceLocation id, ItemLike icon, ItemPredicate.Builder item, int minCount, int maxCount) {
        return BingoGoal.builder(id)
            .sub("count", new BingoSub.RandomBingoSub(MinMaxBounds.Ints.between(minCount, maxCount)))
            .criterion("obtain", InventoryChangeTrigger.TriggerInstance.hasItems(
                item.withCount(MinMaxBounds.Ints.exactly(0)).build()),
                subber -> subber.sub("conditions.items.0.count", "count"))
            .tags(BingoTags.ITEM)
            .icon(icon, subber -> subber.sub("count", "count"));
    }
}
