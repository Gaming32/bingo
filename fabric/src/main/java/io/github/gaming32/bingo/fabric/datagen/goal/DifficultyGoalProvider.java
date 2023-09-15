package io.github.gaming32.bingo.fabric.datagen.goal;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.conditions.BlockPatternCondition;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoSub;
import io.github.gaming32.bingo.data.BingoTags;
import io.github.gaming32.bingo.triggers.BedRowTrigger;
import io.github.gaming32.bingo.triggers.BingoTriggers;
import io.github.gaming32.bingo.triggers.ExperienceChangeTrigger;
import io.github.gaming32.bingo.triggers.HasSomeItemsFromTagTrigger;
import io.github.gaming32.bingo.triggers.MineralPillarTrigger;
import io.github.gaming32.bingo.triggers.TotalCountInventoryChangeTrigger;
import io.github.gaming32.bingo.util.BlockPattern;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class DifficultyGoalProvider {
    private final int difficulty;
    private final String prefix;
    private final Consumer<BingoGoal> goalAdder;

    protected DifficultyGoalProvider(int difficulty, String prefix, Consumer<BingoGoal> goalAdder) {
        this.difficulty = difficulty;
        this.prefix = prefix;
        this.goalAdder = goalAdder;
    }

    protected final void addGoal(BingoGoal.Builder goal) {
        BingoGoal builtGoal = goal.difficulty(difficulty).build();
        if (!builtGoal.getId().getPath().startsWith(prefix)) {
            throw new IllegalArgumentException("Goal ID does not start with " + prefix);
        }
        goalAdder.accept(builtGoal);
    }

    public abstract void addGoals();

    protected final ResourceLocation id(String path) {
        return new ResourceLocation(Bingo.MOD_ID, prefix + path);
    }

    protected static BingoGoal.Builder obtainItemGoal(ResourceLocation id, ItemLike item) {
        return obtainItemGoal(id, item, ItemPredicate.Builder.item().of(item))
            .antisynergy(BuiltInRegistries.ITEM.getKey(item.asItem()).getPath())
            .name(item.asItem().getDescription());
    }

    protected static BingoGoal.Builder obtainItemGoal(ResourceLocation id, ItemLike icon, ItemPredicate.Builder item) {
        return obtainItemGoal(id, new ItemStack(icon), item);
    }

    protected static BingoGoal.Builder obtainItemGoal(ResourceLocation id, ItemStack icon, ItemPredicate.Builder... items) {
        BingoGoal.Builder builder = BingoGoal.builder(id);
        if (items.length == 1) {
            builder.criterion("obtain", TotalCountInventoryChangeTrigger.builder().items(items[0].build()).build());
        } else {
            List<String> requirements = new ArrayList<>(items.length);
            for (int i = 0; i < items.length; i++) {
                builder.criterion("obtain_" + i, TotalCountInventoryChangeTrigger.builder().items(items[i].build()).build());
                requirements.add("obtain_" + i);
            }
            builder.requirements(requirements);
        }
        return builder
            .tags(BingoTags.ITEM)
            .icon(icon);
    }

    protected static BingoGoal.Builder obtainItemGoal(ResourceLocation id, ItemLike item, int minCount, int maxCount) {
        if (minCount == maxCount) {
            return obtainItemGoal(id, item, ItemPredicate.Builder.item().of(item), minCount, maxCount)
                .antisynergy(BuiltInRegistries.ITEM.getKey(item.asItem()).getPath())
                .name(Component.translatable("bingo.count", minCount, item.asItem().getDescription()));
        }
        return obtainItemGoal(id, item, ItemPredicate.Builder.item().of(item), minCount, maxCount)
            .antisynergy(BuiltInRegistries.ITEM.getKey(item.asItem()).getPath())
            .name(Component.translatable("bingo.count", 0, item.asItem().getDescription()),
                subber -> subber.sub("with.0", "count"));
    }

    protected static BingoGoal.Builder obtainItemGoal(ResourceLocation id, ItemLike icon, ItemPredicate.Builder item, int minCount, int maxCount) {
        if (minCount == maxCount) {
            return BingoGoal.builder(id)
                .criterion("obtain", TotalCountInventoryChangeTrigger.builder().items(item.withCount(MinMaxBounds.Ints.exactly(minCount)).build()).build())
                .tags(BingoTags.ITEM)
                .icon(new ItemStack(icon, minCount));
        }
        return BingoGoal.builder(id)
            .sub("count", BingoSub.random(minCount, maxCount))
            .criterion("obtain",
                TotalCountInventoryChangeTrigger.builder().items(item.withCount(MinMaxBounds.Ints.atLeast(0)).build()).build(),
                subber -> subber.sub("conditions.items.0.count.min", "count"))
            .tags(BingoTags.ITEM)
            .icon(icon, subber -> subber.sub("count", "count"));
    }

    protected static BingoGoal.Builder obtainSomeItemsFromTag(ResourceLocation id, ItemLike icon, TagKey<Item> tag, String translationKey, int minCount, int maxCount) {
        return obtainSomeItemsFromTag(id, new ItemStack(icon), tag, translationKey, minCount, maxCount);
    }

    protected static BingoGoal.Builder obtainSomeItemsFromTag(ResourceLocation id, ItemStack icon, TagKey<Item> tag, String translationKey, int minCount, int maxCount) {
        return BingoGoal.builder(id)
            .sub("count", BingoSub.random(minCount, maxCount))
            .criterion(
                "obtain",
                HasSomeItemsFromTagTrigger.builder().tag(tag).requiredCount(0).build(),
                subber -> subber.sub("conditions.required_count", "count")
            )
            .tags(BingoTags.ITEM)
            .name(Component.translatable(translationKey, 0), subber -> subber.sub("with.0", "count"))
            .icon(icon, subber -> subber.sub("count", "count"));
    }

    protected static BingoGoal.Builder obtainLevelsGoal(ResourceLocation id, int minLevels, int maxLevels) {
        return BingoGoal.builder(id)
            .sub("count", BingoSub.random(minLevels, maxLevels))
            .criterion("obtain", ExperienceChangeTrigger.builder().levels(MinMaxBounds.Ints.atLeast(0)).build(),
                subber -> subber.sub("conditions.levels.min", "count"))
            .tags(BingoTags.STAT)
            .name(Component.translatable("bingo.goal.levels", 0), subber -> subber.sub("with.0", "count"))
            .icon(Items.EXPERIENCE_BOTTLE, subber -> subber.sub("count", "count"))
            .infrequency(2)
            .antisynergy("levels");
    }

    protected static BingoGoal.Builder crouchDistanceGoal(ResourceLocation id, int minDistance, int maxDistance) {
        return BingoGoal.builder(id)
            .sub("distance", BingoSub.random(minDistance, maxDistance))
            .criterion("crouch",
                BingoTriggers.statChanged(Stats.CUSTOM.get(Stats.CROUCH_ONE_CM), MinMaxBounds.Ints.atLeast(0)),
                subber -> subber.sub(
                    "conditions.player.0.predicate.type_specific.bingo:relative_stats.0.value.min",
                    new BingoSub.CompoundBingoSub(
                        BingoSub.CompoundBingoSub.ElementType.INT,
                        BingoSub.CompoundBingoSub.Operator.MUL,
                        new BingoSub.SubBingoSub("distance"),
                        BingoSub.literal(100)
                    )
                )
            )
            .name(Component.translatable("bingo.goal.crouch_distance", 0), subber -> subber.sub("with.0", "distance"))
            .antisynergy("crouch_distance")
            .infrequency(2)
            .tags(BingoTags.ACTION, BingoTags.STAT)
            .icon(Items.LEATHER_BOOTS, subber -> subber.sub("count", "distance"));
    }

    protected static BingoGoal.Builder bedRowGoal(ResourceLocation id, int minCount, int maxCount) {
        if (minCount == maxCount) {
            return BingoGoal.builder(id)
                .criterion("obtain", BedRowTrigger.create(minCount))
                .name(Component.translatable("bingo.goal.bed_row", minCount))
                .antisynergy("bed_color")
                .infrequency(4)
                .icon(new ItemStack(Items.MAGENTA_BED, minCount))
                .tags(BingoTags.BUILD, BingoTags.COLOR, BingoTags.OVERWORLD);
        }
        return BingoGoal.builder(id)
            .sub("count", BingoSub.random(minCount, maxCount))
            .criterion("obtain", BedRowTrigger.create(0), subber -> subber.sub("conditions.count", "count"))
            .name(Component.translatable("bingo.goal.bed_row", 0), subber -> subber.sub("with.0", "count"))
            .antisynergy("bed_color")
            .infrequency(4)
            .icon(Items.MAGENTA_BED, subber -> subber.sub("count", "count"))
            .tags(BingoTags.BUILD, BingoTags.COLOR, BingoTags.OVERWORLD);
    }

    protected static BingoGoal.Builder mineralPillarGoal(ResourceLocation id, TagKey<Block> tag) {
        return BingoGoal.builder(id)
            .criterion("pillar", MineralPillarTrigger.TriggerInstance.pillar(tag))
            .tags(BingoTags.BUILD);
    }

    protected static BingoGoal.Builder blockCubeGoal(ResourceLocation id, Block icon, TagKey<Block> blockTag, Component tagName) {
        return BingoGoal.builder(id)
            .sub("width", BingoSub.random(2, 4))
            .sub("height", BingoSub.random(2, 4))
            .sub("depth", BingoSub.random(2, 4))
            .criterion("cube",
                ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(
                    LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(blockTag).build())),
                    BlockPatternCondition.builder().aisle("#")
                        .where('#', BlockPredicate.Builder.block().of(blockTag).build())
                        .rotations(BlockPattern.Rotations.ALL)
                ),
                subber -> subber.sub("conditions.location.1.aisles", new BingoSub.CompoundBingoSub(
                    BingoSub.CompoundBingoSub.ElementType.ARRAY,
                    BingoSub.CompoundBingoSub.Operator.MUL,
                    BingoSub.wrapInArray(
                        new BingoSub.CompoundBingoSub(
                            BingoSub.CompoundBingoSub.ElementType.ARRAY,
                            BingoSub.CompoundBingoSub.Operator.MUL,
                            BingoSub.wrapInArray(
                                new BingoSub.CompoundBingoSub(
                                    BingoSub.CompoundBingoSub.ElementType.STRING,
                                    BingoSub.CompoundBingoSub.Operator.MUL,
                                    BingoSub.literal("#"),
                                    new BingoSub.SubBingoSub("width")
                                )
                            ),
                            new BingoSub.SubBingoSub("height")
                        )
                    ),
                    new BingoSub.SubBingoSub("depth")
                ))
            )
            .tags(BingoTags.BUILD, BingoTags.OVERWORLD)
            .name(
                Component.translatable("bingo.goal.cube", 0, 0, 0, tagName),
                subber -> subber.sub("with.0", "width").sub("with.1", "height").sub("with.2", "depth")
            )
            .tooltip(Component.translatable("bingo.goal.cube.tooltip"))
            .icon(icon);
    }

    protected static ItemStack makeItemWithGlint(ItemLike item) {
        ItemStack result = new ItemStack(item);
        ListTag enchantments = new ListTag();
        enchantments.add(new CompoundTag());
        result.getOrCreateTag().put("Enchantments", enchantments);
        return result;
    }

    protected static ItemStack makeBannerWithPattern(Item base, ResourceKey<BannerPattern> pattern, DyeColor color) {
        final ItemStack result = new ItemStack(base);
        final CompoundTag compound = new CompoundTag();
        compound.put("Patterns", new BannerPattern.Builder().addPattern(pattern, color).toListTag());
        BlockItem.setBlockEntityData(result, BlockEntityType.BANNER, compound);
        return result;
    }
}
