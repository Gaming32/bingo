package io.github.gaming32.bingo.datagen.goal;

import com.demonwav.mcdev.annotations.Translatable;
import com.google.common.collect.ImmutableList;
import io.github.gaming32.bingo.conditions.BlockPatternCondition;
import io.github.gaming32.bingo.data.BingoDifficulty;
import io.github.gaming32.bingo.data.BingoTags;
import io.github.gaming32.bingo.data.JsonSubber;
import io.github.gaming32.bingo.data.goal.BingoGoal;
import io.github.gaming32.bingo.data.goal.GoalBuilder;
import io.github.gaming32.bingo.data.goal.GoalHolder;
import io.github.gaming32.bingo.data.icons.CycleIcon;
import io.github.gaming32.bingo.data.icons.GoalIcon;
import io.github.gaming32.bingo.data.icons.ItemIcon;
import io.github.gaming32.bingo.data.icons.ItemTagCycleIcon;
import io.github.gaming32.bingo.data.progresstrackers.CriterionProgressTracker;
import io.github.gaming32.bingo.data.subs.BingoSub;
import io.github.gaming32.bingo.data.subs.CompoundBingoSub;
import io.github.gaming32.bingo.data.subs.SubBingoSub;
import io.github.gaming32.bingo.triggers.BedRowTrigger;
import io.github.gaming32.bingo.triggers.ExperienceChangeTrigger;
import io.github.gaming32.bingo.triggers.HasSomeFoodItemsTrigger;
import io.github.gaming32.bingo.triggers.HasSomeItemsFromTagTrigger;
import io.github.gaming32.bingo.triggers.MineralPillarTrigger;
import io.github.gaming32.bingo.triggers.RelativeStatsTrigger;
import io.github.gaming32.bingo.triggers.TotalCountInventoryChangeTrigger;
import io.github.gaming32.bingo.util.BingoUtil;
import io.github.gaming32.bingo.util.BlockPattern;
import io.github.gaming32.bingo.util.Identifiers;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class DifficultyGoalProvider {
    private final ResourceKey<BingoDifficulty> difficulty;
    private final String prefix;
    private final BiConsumer<Identifier, BingoGoal> goalAdder;
    protected final HolderLookup.Provider registries;

    protected DifficultyGoalProvider(
        ResourceKey<BingoDifficulty> difficulty,
        BiConsumer<Identifier, BingoGoal> goalAdder,
        HolderLookup.Provider registries
    ) {
        this.difficulty = difficulty;
        this.prefix = difficulty.identifier().getPath() + '/';
        this.goalAdder = goalAdder;
        this.registries = registries;
    }

    protected final void addGoal(GoalBuilder goal) {
        GoalHolder builtGoal = goal.difficulty(difficulty).build(registries);
        if (!builtGoal.id().getPath().startsWith(prefix)) {
            throw new IllegalArgumentException("Goal ID does not start with " + prefix);
        }
        goalAdder.accept(builtGoal.id(), builtGoal.goal());
    }

    public abstract void addGoals();

    protected final Identifier id(String path) {
        return Identifiers.bingo(prefix + path);
    }

    protected static GoalBuilder obtainItemGoal(Identifier id, HolderLookup<Item> items, ResourceKey<Item> item) {
        return obtainItemGoal(id, items, items.getOrThrow(item).value());
    }

    @SuppressWarnings("deprecation")
    protected static GoalBuilder obtainItemGoal(Identifier id, HolderLookup<Item> items, ItemLike item) {
        return obtainItemGoal(id, items, item, ItemPredicate.Builder.item().of(items, item))
            .antisynergy(item.asItem().builtInRegistryHolder().key().identifier().getPath())
            .name(Component.translatable(item.asItem().getDescriptionId()));
    }

    protected static GoalBuilder obtainItemGoal(Identifier id, HolderLookup<Item> items, ItemLike icon, ItemPredicate.Builder... oneOfThese) {
        return obtainItemGoal(id, items, ItemIcon.ofItem(icon), oneOfThese);
    }

    protected static GoalBuilder obtainItemGoal(Identifier id, HolderLookup<Item> items, ItemStackTemplate icon, ItemPredicate.Builder... oneOfThese) {
        return obtainItemGoal(id, items, new ItemIcon(icon), oneOfThese);
    }

    protected static GoalBuilder obtainItemGoal(
        Identifier id,
        @SuppressWarnings("unused") HolderLookup<Item> items, // Unused, but present for consistency
        GoalIcon icon,
        ItemPredicate.Builder... oneOfThese
    ) {
        GoalBuilder builder = BingoGoal.builder(id);
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

    protected static GoalBuilder obtainItemGoal(Identifier id, HolderLookup<Item> items, ResourceKey<Item> item, int minCount, int maxCount) {
        return obtainItemGoal(id, items, items.getOrThrow(item).value(), minCount, maxCount);
    }

    @SuppressWarnings("deprecation")
    protected static GoalBuilder obtainItemGoal(Identifier id, HolderLookup<Item> items, ItemLike item, int minCount, int maxCount) {
        final var realItem = item.asItem();
        final Consumer<JsonSubber> subFunction = minCount == maxCount
            ? subber -> {}
            : subber -> subber.sub("with.0", "count");
        return obtainItemGoal(id, item, ItemPredicate.Builder.item().of(items, item), minCount, maxCount)
            .antisynergy(realItem.builtInRegistryHolder().key().identifier().getPath())
            .name(Component.translatable("bingo.count", minCount, Component.translatable(realItem.asItem().getDescriptionId())), subFunction);
    }

    protected static GoalBuilder obtainItemGoal(Identifier id, ItemLike icon, ItemPredicate.Builder item, int minCount, int maxCount) {
        if (minCount == maxCount) {
            return BingoGoal.builder(id)
                .criterion("obtain", TotalCountInventoryChangeTrigger.builder().items(item.withCount(MinMaxBounds.Ints.exactly(minCount)).build()).build())
                .progress("obtain")
                .tags(BingoTags.ITEM)
                .icon(new ItemStackTemplate(icon.asItem(), minCount));
        }
        return BingoGoal.builder(id)
            .sub("count", BingoSub.random(minCount, maxCount))
            .criterion("obtain",
                TotalCountInventoryChangeTrigger.builder().items(item.withCount(MinMaxBounds.Ints.atLeast(0)).build()).build(),
                subber -> subber.sub("conditions.items.0.count.min", "count"))
            .progress("obtain")
            .tags(BingoTags.ITEM)
            .icon(icon, subber -> subber.sub("item.count", "count"));
    }

    protected static GoalBuilder obtainSomeItemsFromTag(
        Identifier id, TagKey<Item> tag, @Translatable String translationKey,
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
                HasSomeItemsFromTagTrigger.builder().tag(tag).requiredCount(1).build(),
                subber -> subber.sub("conditions.required_count", "count")
            )
            .progress("obtain")
            .tags(BingoTags.ITEM)
            .name(Component.translatable(translationKey, 0), subber -> subber.sub("with.0", "count"))
            .icon(new ItemTagCycleIcon(tag), subber -> subber.sub("+count", "count"));
    }

    protected GoalBuilder obtainAllItemsFromTag(TagKey<Item> tag, @Translatable(prefix = "bingo.goal.all_somethings.") String what) {
        return BingoGoal.builder(id("all_" + what))
            .criterion("obtain", HasSomeItemsFromTagTrigger.builder().tag(tag).requiresAll().build())
            .progress("obtain")
            .tags(BingoTags.ITEM)
            .name(Component.translatable("bingo.goal.all_somethings", Component.translatable("bingo.goal.all_somethings." + what)))
            .antisynergy("all_" + tag.location().getPath())
            .icon(new ItemTagCycleIcon(tag));
    }

    protected static GoalBuilder obtainSomeEdibleItems(Identifier id, int minCount, int maxCount) {
        if (minCount == maxCount) {
            return BingoGoal.builder(id)
                .criterion("obtain", HasSomeFoodItemsTrigger.builder().requiredCount(minCount).build())
                .progress("obtain")
                .tags(BingoTags.ITEM)
                .name(Component.translatable("bingo.goal.edible_items", minCount))
                .tooltip("edible_items")
                .tooltipIcon(Identifiers.bingo("textures/gui/tooltips/raw_and_cooked.png"))
                .antisynergy("edible_items")
                .infrequency(2)
                .icon(new ItemTagCycleIcon(ConventionalItemTags.FOODS, minCount));
        }
        return BingoGoal.builder(id)
            .sub("count", BingoSub.random(minCount, maxCount))
            .criterion("obtain", HasSomeFoodItemsTrigger.builder().requiredCount(1).build(), subber -> subber.sub("conditions.required_count", "count"))
            .progress("obtain")
            .tags(BingoTags.ITEM)
            .name(Component.translatable("bingo.goal.edible_items", 0), subber -> subber.sub("with.0", "count"))
            .tooltip("edible_items")
            .tooltipIcon(Identifiers.bingo("textures/gui/tooltips/raw_and_cooked.png"))
            .antisynergy("edible_items")
            .infrequency(2)
            .icon(new ItemTagCycleIcon(ConventionalItemTags.FOODS), subber -> subber.sub("+count", "count"));
    }

    protected static GoalBuilder obtainLevelsGoal(Identifier id, int minLevels, int maxLevels) {
        return BingoGoal.builder(id)
            .sub("count", BingoSub.random(minLevels, maxLevels))
            .criterion("obtain", ExperienceChangeTrigger.builder().levels(MinMaxBounds.Ints.atLeast(0)).build(),
                subber -> subber.sub("conditions.levels.min", "count"))
            .tags(BingoTags.STAT)
            .name(Component.translatable("bingo.goal.levels", 0), subber -> subber.sub("with.0", "count"))
            .icon(Items.EXPERIENCE_BOTTLE, subber -> subber.sub("item.count", "count"))
            .infrequency(2)
            .antisynergy("levels");
    }

    protected static GoalBuilder crouchDistanceGoal(Identifier id, int minDistance, int maxDistance) {
        return BingoGoal.builder(id)
            .sub("distance", BingoSub.random(minDistance, maxDistance))
            .criterion(
                "crouch",
                RelativeStatsTrigger.builder()
                    .stat(Stats.CROUCH_ONE_CM, MinMaxBounds.Ints.atLeast(0))
                    .build(),
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
            .progress(new CriterionProgressTracker("crouch", 0.01f))
            .name(Component.translatable("bingo.goal.crouch_distance", 0), subber -> subber.sub("with.0", "distance"))
            .antisynergy("crouch_distance")
            .infrequency(2)
            .tags(BingoTags.ACTION, BingoTags.STAT)
            .icon(Items.LEATHER_BOOTS, subber -> subber.sub("item.count", "distance"));
    }

    protected static GoalBuilder bedRowGoal(Identifier id, int minCount, int maxCount) {
        if (minCount == maxCount) {
            return BingoGoal.builder(id)
                .criterion("obtain", BedRowTrigger.create(minCount))
                .name(Component.translatable("bingo.goal.bed_row", minCount))
                .antisynergy("bed_color")
                .infrequency(4)
                .icon(new ItemStackTemplate(Items.MAGENTA_BED, minCount))
                .tags(BingoTags.BUILD, BingoTags.COLOR, BingoTags.OVERWORLD);
        }
        return BingoGoal.builder(id)
            .sub("count", BingoSub.random(minCount, maxCount))
            .criterion("obtain", BedRowTrigger.create(1), subber -> subber.sub("conditions.count", "count"))
            .name(Component.translatable("bingo.goal.bed_row", 0), subber -> subber.sub("with.0", "count"))
            .antisynergy("bed_color")
            .infrequency(4)
            .icon(Items.MAGENTA_BED, subber -> subber.sub("item.count", "count"))
            .tags(BingoTags.BUILD, BingoTags.COLOR, BingoTags.OVERWORLD);
    }

    protected static GoalBuilder mineralPillarGoal(Identifier id, TagKey<Block> tag) {
        return BingoGoal.builder(id)
            .criterion("pillar", MineralPillarTrigger.pillar(tag))
            .tags(BingoTags.BUILD);
    }

    protected GoalBuilder blockCubeGoal(Identifier id, Object icon, TagKey<Block> blockTag, Component tagName) {
        final var blocks = registries.lookupOrThrow(Registries.BLOCK);
        return BingoGoal.builder(id)
            .sub("width", BingoSub.random(2, 4))
            .sub("height", BingoSub.random(2, 4))
            .sub("depth", BingoSub.random(2, 4))
            .criterion("cube",
                ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(
                    LocationCheck.checkLocation(LocationPredicate.Builder.location()
                        .setBlock(BlockPredicate.Builder.block().of(blocks, blockTag))
                    ),
                    BlockPatternCondition.builder().aisle("#")
                        .where('#', BlockPredicate.Builder.block().of(blocks, blockTag))
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
            .tooltip("cube")
            .icon(icon);
    }

    protected static ItemStackTemplate makeItemWithGlint(ItemLike item) {
        return makeItemWithGlint(new ItemStackTemplate(item.asItem()));
    }

    protected static ItemStackTemplate makeItemWithGlint(ItemStackTemplate item) {
        return new ItemStackTemplate(
            item.item(),
            item.count(),
            BingoUtil.builderFrom(item.components()).set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true).build()
        );
    }

    protected static ItemStackTemplate makeBannerWithPattern(Item base, Holder<BannerPattern> pattern, DyeColor color) {
        return new ItemStackTemplate(
            base,
            DataComponentPatch.builder()
                .set(DataComponents.BANNER_PATTERNS, new BannerPatternLayers.Builder().add(pattern, color).build())
                .build()
        );
    }

    protected static ItemStackTemplate makeShieldWithColor(@Nullable DyeColor color) {
        return new ItemStackTemplate(Items.SHIELD, DataComponentPatch.builder().set(DataComponents.BASE_COLOR, color).build());
    }

    @SuppressWarnings("deprecation")
    protected static BlockPredicate.Builder spawnerPredicate(
        HolderLookup<Block> blocks,
        @SuppressWarnings("unused") HolderLookup<EntityType<?>> entityTypes,
        EntityType<?> entityType
    ) {
        return BlockPredicate.Builder.block()
            .of(blocks, Blocks.SPAWNER)
            .hasNbt(BingoUtil.compound(Map.of(
                "SpawnData", BingoUtil.compound(Map.of(
                    "entity", BingoUtil.compound(Map.of(
                        "id", StringTag.valueOf(entityType.builtInRegistryHolder().key().identifier().toString())
                    ))
                ))
            )));
    }

    protected static GoalIcon createPotionsIcon(HolderLookup<Potion> potions, Item baseItem) {
        final Set<String> encountered = new HashSet<>();
        return new CycleIcon(
            potions.listElements()
                .filter(p -> encountered.add(p.value().name()))
                .map(p -> new ItemStackTemplate(
                    baseItem,
                    DataComponentPatch.builder().set(DataComponents.POTION_CONTENTS, new PotionContents(p)).build()
                ))
                .map(ItemIcon::new)
                .collect(ImmutableList.toImmutableList())
        );
    }

    protected static EnchantmentsPredicate.Enchantments createAnyEnchantmentsRequirement() {
        return EnchantmentsPredicate.enchantments(List.of(
            new EnchantmentPredicate(Optional.empty(), MinMaxBounds.Ints.atLeast(1))
        ));
    }

    protected static EnchantmentsPredicate.StoredEnchantments createAnyStoredEnchantmentsRequirement() {
        return EnchantmentsPredicate.storedEnchantments(List.of(
            new EnchantmentPredicate(Optional.empty(), MinMaxBounds.Ints.atLeast(1))
        ));
    }

    protected static ResourceKey<Item> itemResource(String path) {
        return ResourceKey.create(Registries.ITEM, Identifiers.minecraft(path));
    }
}
