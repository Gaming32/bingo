package io.github.gaming32.bingo.fabric.datagen.goal;

import com.demonwav.mcdev.annotations.Translatable;
import com.google.common.collect.ImmutableList;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.conditions.BlockPatternCondition;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.BingoTags;
import io.github.gaming32.bingo.data.ProgressTracker;
import io.github.gaming32.bingo.data.icons.CycleIcon;
import io.github.gaming32.bingo.data.icons.GoalIcon;
import io.github.gaming32.bingo.data.icons.ItemIcon;
import io.github.gaming32.bingo.data.icons.ItemTagCycleIcon;
import io.github.gaming32.bingo.data.subs.BingoSub;
import io.github.gaming32.bingo.data.subs.CompoundBingoSub;
import io.github.gaming32.bingo.data.subs.SubBingoSub;
import io.github.gaming32.bingo.triggers.*;
import io.github.gaming32.bingo.util.BingoUtil;
import io.github.gaming32.bingo.util.BlockPattern;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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

    protected static BingoGoal.Builder obtainItemGoal(ResourceLocation id, ItemLike icon, ItemPredicate.Builder... oneOfThese) {
        return obtainItemGoal(id, ItemIcon.ofItem(icon), oneOfThese);
    }

    protected static BingoGoal.Builder obtainItemGoal(ResourceLocation id, ItemStack icon, ItemPredicate.Builder... oneOfThese) {
        return obtainItemGoal(id, new ItemIcon(icon), oneOfThese);
    }

    protected static BingoGoal.Builder obtainItemGoal(ResourceLocation id, GoalIcon icon, ItemPredicate.Builder... oneOfThese) {
        BingoGoal.Builder builder = BingoGoal.builder(id);
        if (oneOfThese.length == 1) {
            builder.criterion("obtain", TotalCountInventoryChangeTrigger.builder().items(oneOfThese[0].build()).build())
                .progress("obtain");
        } else {
            for (int i = 0; i < oneOfThese.length; i++) {
                builder.criterion("obtain_" + i, TotalCountInventoryChangeTrigger.builder().items(oneOfThese[i].build()).build());
            }
            builder.requirements(AdvancementRequirements.Strategy.OR);
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
                .progress("obtain")
                .tags(BingoTags.ITEM)
                .icon(new ItemStack(icon, minCount));
        }
        return BingoGoal.builder(id)
            .sub("count", BingoSub.random(minCount, maxCount))
            .criterion("obtain",
                TotalCountInventoryChangeTrigger.builder().items(item.withCount(MinMaxBounds.Ints.atLeast(0)).build()).build(),
                subber -> subber.sub("conditions.items.0.count.min", "count"))
            .progress("obtain")
            .tags(BingoTags.ITEM)
            .icon(icon, subber -> subber.sub("value.Count", "count"));
    }

    protected static BingoGoal.Builder obtainSomeItemsFromTag(
        ResourceLocation id, TagKey<Item> tag, @Translatable String translationKey,
        int minCount, int maxCount
    ) {
        if (minCount == maxCount) {
            return BingoGoal.builder(id)
                .criterion("obtain", HasSomeItemsFromTagTrigger.builder().tag(tag).requiredCount(minCount).build())
                .progress("obtain")
                .tags(BingoTags.ITEM)
                .name(Component.translatable(translationKey, minCount))
                .icon(new ItemTagCycleIcon(tag, minCount));
        }
        return BingoGoal.builder(id)
            .sub("count", BingoSub.random(minCount, maxCount))
            .criterion(
                "obtain",
                HasSomeItemsFromTagTrigger.builder().tag(tag).requiredCount(0).build(),
                subber -> subber.sub("conditions.required_count", "count")
            )
            .progress("obtain")
            .tags(BingoTags.ITEM)
            .name(Component.translatable(translationKey, 0), subber -> subber.sub("with.0", "count"))
            .icon(new ItemTagCycleIcon(tag), subber -> subber.sub("count", "count"));
    }

    protected BingoGoal.Builder obtainAllItemsFromTag(TagKey<Item> tag, @Translatable(prefix = "bingo.goal.all_somethings.") String what) {
        return BingoGoal.builder(id("all_" + what))
            .criterion("obtain", HasSomeItemsFromTagTrigger.builder().tag(tag).requiresAll().build())
            .progress("obtain")
            .tags(BingoTags.ITEM)
            .name(Component.translatable("bingo.goal.all_somethings", Component.translatable("bingo.goal.all_somethings." + what)))
            .icon(new ItemTagCycleIcon(tag));
    }

    protected static BingoGoal.Builder obtainLevelsGoal(ResourceLocation id, int minLevels, int maxLevels) {
        return BingoGoal.builder(id)
            .sub("count", BingoSub.random(minLevels, maxLevels))
            .criterion("obtain", ExperienceChangeTrigger.builder().levels(MinMaxBounds.Ints.atLeast(0)).build(),
                subber -> subber.sub("conditions.levels.min", "count"))
            .tags(BingoTags.STAT)
            .name(Component.translatable("bingo.goal.levels", 0), subber -> subber.sub("with.0", "count"))
            .icon(Items.EXPERIENCE_BOTTLE, subber -> subber.sub("value.Count", "count"))
            .infrequency(2)
            .antisynergy("levels");
    }

    protected static BingoGoal.Builder crouchDistanceGoal(ResourceLocation id, int minDistance, int maxDistance) {
        RelativeStatsTrigger.Builder builder = RelativeStatsTrigger.builder();
        return BingoGoal.builder(id)
            .sub("distance", BingoSub.random(minDistance, maxDistance))
            .criterion("crouch",
                builder.stat(Stats.CROUCH_ONE_CM, MinMaxBounds.Ints.atLeast(0)).build(),
                subber -> subber.sub(
                    "conditions.stats.0.value.min",
                    new CompoundBingoSub(
                        CompoundBingoSub.ElementType.INT,
                        CompoundBingoSub.Operator.MUL,
                        new SubBingoSub("distance"),
                        BingoSub.literal(100)
                    )
                )
            )
            .progress(new ProgressTracker.Criterion("crouch", 0.01f))
            .name(Component.translatable("bingo.goal.crouch_distance", 0), subber -> subber.sub("with.0", "distance"))
            .antisynergy("crouch_distance")
            .infrequency(2)
            .tags(BingoTags.ACTION, BingoTags.STAT)
            .icon(Items.LEATHER_BOOTS, subber -> subber.sub("value.Count", "distance"));
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
            .icon(Items.MAGENTA_BED, subber -> subber.sub("value.Count", "count"))
            .tags(BingoTags.BUILD, BingoTags.COLOR, BingoTags.OVERWORLD);
    }

    protected static BingoGoal.Builder mineralPillarGoal(ResourceLocation id, TagKey<Block> tag) {
        return BingoGoal.builder(id)
            .criterion("pillar", MineralPillarTrigger.pillar(tag))
            .tags(BingoTags.BUILD);
    }

    protected static BingoGoal.Builder blockCubeGoal(ResourceLocation id, Object icon, TagKey<Block> blockTag, Component tagName) {
        return BingoGoal.builder(id)
            .sub("width", BingoSub.random(2, 4))
            .sub("height", BingoSub.random(2, 4))
            .sub("depth", BingoSub.random(2, 4))
            .criterion("cube",
                ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(
                    LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(blockTag))),
                    BlockPatternCondition.builder().aisle("#")
                        .where('#', BlockPredicate.Builder.block().of(blockTag))
                        .rotations(BlockPattern.Rotations.ALL)
                ),
                subber -> subber.sub("conditions.location.1.aisles", new CompoundBingoSub(
                    CompoundBingoSub.ElementType.ARRAY,
                    CompoundBingoSub.Operator.MUL,
                    BingoSub.wrapInArray(
                        new CompoundBingoSub(
                            CompoundBingoSub.ElementType.ARRAY,
                            CompoundBingoSub.Operator.MUL,
                            BingoSub.wrapInArray(
                                new CompoundBingoSub(
                                    CompoundBingoSub.ElementType.STRING,
                                    CompoundBingoSub.Operator.MUL,
                                    BingoSub.literal("#"),
                                    new SubBingoSub("width")
                                )
                            ),
                            new SubBingoSub("height")
                        )
                    ),
                    new SubBingoSub("depth")
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
        return makeItemWithGlint(new ItemStack(item));
    }

    protected static ItemStack makeItemWithGlint(ItemStack item) {
        ListTag enchantments = new ListTag();
        enchantments.add(new CompoundTag());
        item.getOrCreateTag().put("Enchantments", enchantments);
        return item;
    }

    protected static ItemStack makeBannerWithPattern(Item base, ResourceKey<BannerPattern> pattern, DyeColor color) {
        final ItemStack result = new ItemStack(base);
        final CompoundTag compound = new CompoundTag();
        compound.put("Patterns", new BannerPattern.Builder().addPattern(pattern, color).toListTag());
        BlockItem.setBlockEntityData(result, BlockEntityType.BANNER, compound);
        return result;
    }

    protected static BlockPredicate.Builder spawnerPredicate(EntityType<?> entityType) {
        return BlockPredicate.Builder.block()
            .of(Blocks.SPAWNER)
            .hasNbt(BingoUtil.compound(Map.of(
                "SpawnData", BingoUtil.compound(Map.of(
                    "entity", BingoUtil.compound(Map.of(
                        "id", StringTag.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString())
                    ))
                ))
            )));
    }

    protected static ItemStack simplifyBlockEntityStackData(ItemStack stack) {
        if (stack.hasTag()) {
            assert stack.getTag() != null;
            for (final String key : stack.getTag().getAllKeys().toArray(String[]::new)) {
                if (!key.equals(BlockItem.BLOCK_ENTITY_TAG) && !key.equals(BlockItem.BLOCK_STATE_TAG)) {
                    stack.getTag().remove(key);
                }
            }
            stack.getTag().getCompound(BlockItem.BLOCK_ENTITY_TAG).remove("id");
        }
        return stack;
    }

    protected static GoalIcon createPotionsIcon(Item baseItem) {
        final Set<String> encountered = new HashSet<>();
        return new CycleIcon(
            BuiltInRegistries.POTION.stream()
                .filter(p -> p != Potions.EMPTY)
                .filter(p -> encountered.add(p.getName("")))
                .map(p -> PotionUtils.setPotion(new ItemStack(baseItem), p))
                .map(ItemIcon::new)
                .collect(ImmutableList.toImmutableList())
        );
    }
}
