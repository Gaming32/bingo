package io.github.gaming32.bingo.datagen.goal;

import com.demonwav.mcdev.annotations.Translatable;
import com.google.common.collect.ImmutableList;
import io.github.gaming32.bingo.conditions.BlockPatternCondition;
import io.github.gaming32.bingo.data.BingoDifficulty;
import io.github.gaming32.bingo.data.BingoRegistries;
import io.github.gaming32.bingo.data.BingoTags;
import io.github.gaming32.bingo.data.JsonSubber;
import io.github.gaming32.bingo.data.goal.BingoGoal;
import io.github.gaming32.bingo.data.goal.GoalHolder;
import io.github.gaming32.bingo.data.icons.CycleIcon;
import io.github.gaming32.bingo.data.icons.EffectIcon;
import io.github.gaming32.bingo.data.icons.EntityIcon;
import io.github.gaming32.bingo.data.icons.GoalIcon;
import io.github.gaming32.bingo.data.icons.ItemIcon;
import io.github.gaming32.bingo.data.icons.ItemTagCycleIcon;
import io.github.gaming32.bingo.data.progresstrackers.AchievedCriteriaProgressTracker;
import io.github.gaming32.bingo.data.progresstrackers.CriterionProgressTracker;
import io.github.gaming32.bingo.data.subs.BingoSub;
import io.github.gaming32.bingo.data.subs.CompoundBingoSub;
import io.github.gaming32.bingo.data.subs.SubBingoSub;
import io.github.gaming32.bingo.datagen.BingoDataGenUtil;
import io.github.gaming32.bingo.triggers.BedRowTrigger;
import io.github.gaming32.bingo.triggers.ExperienceChangeTrigger;
import io.github.gaming32.bingo.triggers.HasSomeFoodItemsTrigger;
import io.github.gaming32.bingo.triggers.HasSomeItemsFromTagTrigger;
import io.github.gaming32.bingo.triggers.MineralPillarTrigger;
import io.github.gaming32.bingo.triggers.RelativeStatsTrigger;
import io.github.gaming32.bingo.triggers.StatusEffectsTrigger;
import io.github.gaming32.bingo.triggers.TotalCountInventoryChangeTrigger;
import io.github.gaming32.bingo.util.BingoUtil;
import io.github.gaming32.bingo.util.BlockPattern;
import io.github.gaming32.bingo.util.Identifiers;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.predicates.BlockPredicate;
import net.minecraft.advancements.predicates.EnchantmentPredicate;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.LocationPredicate;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.triggers.TameAnimalTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.BootstrapContextAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.world.entity.animal.wolf.WolfVariants;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.storage.loot.predicates.AllOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public abstract class DifficultyGoalProvider {
    private final ResourceKey<BingoDifficulty> difficulty;
    private final String prefix;
    protected final BootstrapContext<BingoGoal> context;

    protected DifficultyGoalProvider(
        ResourceKey<BingoDifficulty> difficulty,
        BootstrapContext<BingoGoal> context
    ) {
        this.difficulty = difficulty;
        this.prefix = difficulty.identifier().getPath() + '/';
        this.context = context;
    }

    protected final void addGoal(GoalBuilder goal) {
        GoalHolder builtGoal = goal.difficulty(difficulty).build(context);
        if (!builtGoal.id().getPath().startsWith(prefix)) {
            throw new IllegalArgumentException("Goal ID does not start with " + prefix);
        }
        context.register(ResourceKey.create(BingoRegistries.GOAL, builtGoal.id()), builtGoal.goal());
    }

    public abstract void addGoals();

    protected final Identifier id(String path) {
        return Identifiers.bingo(prefix + path);
    }

    protected GoalBuilder obtainItemGoal(Identifier id, HolderGetter<Item> items, ResourceKey<Item> item) {
        return obtainItemGoal(id, items, items.getOrThrow(item).value());
    }

    @SuppressWarnings("deprecation")
    protected GoalBuilder obtainItemGoal(Identifier id, HolderGetter<Item> items, ItemLike item) {
        return obtainItemGoal(id, items, item, ItemPredicate.Builder.item().of(items, item))
            .antisynergy(item.asItem().builtInRegistryHolder().key().identifier().getPath())
            .name(Component.translatable(item.asItem().getDescriptionId()));
    }

    protected GoalBuilder obtainItemGoal(Identifier id, HolderGetter<Item> items, ItemLike icon, ItemPredicate.Builder... oneOfThese) {
        return obtainItemGoal(id, items, ItemIcon.ofItem(icon), oneOfThese);
    }

    protected GoalBuilder obtainItemGoal(Identifier id, HolderGetter<Item> items, ItemStackTemplate icon, ItemPredicate.Builder... oneOfThese) {
        return obtainItemGoal(id, items, new ItemIcon(icon), oneOfThese);
    }

    protected GoalBuilder obtainItemGoal(
        Identifier id,
        @SuppressWarnings("unused") HolderGetter<Item> items, // Unused, but present for consistency
        GoalIcon icon,
        ItemPredicate.Builder... oneOfThese
    ) {
        GoalBuilder builder = GoalBuilder.of(id);
        if (oneOfThese.length == 1) {
            builder.criterion(context, "obtain", TotalCountInventoryChangeTrigger.builder().items(oneOfThese[0].build()).build())
                .progress("obtain");
        } else {
            for (int i = 0; i < oneOfThese.length; i++) {
                builder.criterion(context, "obtain_" + i, TotalCountInventoryChangeTrigger.builder().items(oneOfThese[i].build()).build());
            }
            builder.requirements(AdvancementRequirements.Strategy.OR);
        }
        return builder
            .tags(BingoTags.ITEM)
            .icon(context, icon);
    }

    protected GoalBuilder obtainItemGoal(Identifier id, HolderGetter<Item> items, ResourceKey<Item> item, int minCount, int maxCount) {
        return obtainItemGoal(id, items, items.getOrThrow(item).value(), minCount, maxCount);
    }

    @SuppressWarnings("deprecation")
    protected GoalBuilder obtainItemGoal(Identifier id, HolderGetter<Item> items, ItemLike item, int minCount, int maxCount) {
        final var realItem = item.asItem();
        final Consumer<JsonSubber> subFunction = minCount == maxCount
            ? _ -> {}
            : subber -> subber.sub("with.0", "count");
        return obtainItemGoal(id, item, ItemPredicate.Builder.item().of(items, item), minCount, maxCount)
            .antisynergy(realItem.builtInRegistryHolder().key().identifier().getPath())
            .name(Component.translatable("bingo.count", minCount, Component.translatable(realItem.asItem().getDescriptionId())), subFunction);
    }

    protected GoalBuilder obtainItemGoal(Identifier id, ItemLike icon, ItemPredicate.Builder item, int minCount, int maxCount) {
        if (minCount == maxCount) {
            return GoalBuilder.of(id)
                .criterion(context, "obtain", TotalCountInventoryChangeTrigger.builder().items(item.withCount(MinMaxBounds.Ints.atLeast(minCount)).build()).build())
                .progress("obtain")
                .tags(BingoTags.ITEM)
                .icon(context, new ItemStackTemplate(icon.asItem(), minCount));
        }
        return GoalBuilder.of(id)
            .sub("count", BingoSub.random(minCount, maxCount))
            .criterion(
                context,
                "obtain",
                TotalCountInventoryChangeTrigger.builder().items(item.withCount(MinMaxBounds.Ints.atLeast(0)).build()).build(),
                subber -> subber.sub("conditions.items.0.count.min", "count"))
            .progress("obtain")
            .tags(BingoTags.ITEM)
            .icon(context, icon, subber -> subber.sub("item.count", "count"));
    }

    @SafeVarargs
    protected final GoalBuilder obtainSomeItemsFromTag(
        Identifier id, TagKey<Item> tag, @Translatable String translationKey,
        int minCount, int maxCount, TagKey<Item>... groups
    ) {
        final var trigger = HasSomeItemsFromTagTrigger.builder().tag(tag);
        for (final TagKey<Item> group : groups) {
            trigger.group(group);
        }
        if (minCount == maxCount) {
            return GoalBuilder.of(id)
                .criterion(context, "obtain", trigger.requiredCount(minCount).build())
                .progress("obtain")
                .tags(BingoTags.ITEM)
                .name(Component.translatable(translationKey, minCount))
                .icon(context, new ItemTagCycleIcon(tag, minCount));
        }
        return GoalBuilder.of(id)
            .sub("count", BingoSub.random(minCount, maxCount))
            .criterion(
                context,
                "obtain",
                trigger.requiredCount(1).build(),
                subber -> subber.sub("conditions.required_count", "count")
            )
            .progress("obtain")
            .tags(BingoTags.ITEM)
            .name(Component.translatable(translationKey, 0), subber -> subber.sub("with.0", "count"))
            .icon(context, new ItemTagCycleIcon(tag), subber -> subber.sub("+count", "count"));
    }

    protected GoalBuilder obtainAllItemsFromTag(TagKey<Item> tag, @Translatable(prefix = "bingo.goal.all_somethings.") String what) {
        return GoalBuilder.of(id("all_" + what))
            .criterion(context, "obtain", HasSomeItemsFromTagTrigger.builder().tag(tag).requiresAll().build())
            .progress("obtain")
            .tags(BingoTags.ITEM)
            .name(Component.translatable("bingo.goal.all_somethings", Component.translatable("bingo.goal.all_somethings." + what)))
            .antisynergy("all_" + tag.location().getPath())
            .icon(context, new ItemTagCycleIcon(tag));
    }

    protected GoalBuilder obtainSomeEdibleItems(Identifier id, int minCount, int maxCount) {
        if (minCount == maxCount) {
            return GoalBuilder.of(id)
                .criterion(context, "obtain", HasSomeFoodItemsTrigger.builder().requiredCount(minCount).build())
                .progress("obtain")
                .tags(BingoTags.ITEM)
                .name(Component.translatable("bingo.goal.edible_items", minCount))
                .tooltip("edible_items")
                .tooltipIcon(Identifiers.bingo("textures/gui/tooltips/raw_and_cooked.png"))
                .antisynergy("edible_items")
                .infrequency(2)
                .icon(context, new ItemTagCycleIcon(ConventionalItemTags.FOODS, minCount));
        }
        return GoalBuilder.of(id)
            .sub("count", BingoSub.random(minCount, maxCount))
            .criterion(context, "obtain", HasSomeFoodItemsTrigger.builder().requiredCount(1).build(), subber -> subber.sub("conditions.required_count", "count"))
            .progress("obtain")
            .tags(BingoTags.ITEM)
            .name(Component.translatable("bingo.goal.edible_items", 0), subber -> subber.sub("with.0", "count"))
            .tooltip("edible_items")
            .tooltipIcon(Identifiers.bingo("textures/gui/tooltips/raw_and_cooked.png"))
            .antisynergy("edible_items")
            .infrequency(2)
            .icon(context, new ItemTagCycleIcon(ConventionalItemTags.FOODS), subber -> subber.sub("+count", "count"));
    }

    protected GoalBuilder obtainLevelsGoal(Identifier id, int minLevels, int maxLevels) {
        return GoalBuilder.of(id)
            .sub("count", BingoSub.random(minLevels, maxLevels))
            .criterion(context, "obtain", ExperienceChangeTrigger.builder().levels(MinMaxBounds.Ints.atLeast(0)).build(),
                subber -> subber.sub("conditions.levels.min", "count"))
            .tags(BingoTags.STAT)
            .name(Component.translatable("bingo.goal.levels", 0), subber -> subber.sub("with.0", "count"))
            .icon(context, Items.EXPERIENCE_BOTTLE, subber -> subber.sub("item.count", "count"))
            .infrequency(2)
            .antisynergy("levels");
    }

    protected GoalBuilder crouchDistanceGoal(Identifier id, int minDistance, int maxDistance) {
        return GoalBuilder.of(id)
            .sub("distance", BingoSub.random(minDistance, maxDistance))
            .criterion(
                context,
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
            .icon(context, Items.LEATHER_BOOTS, subber -> subber.sub("item.count", "distance"));
    }

    protected GoalBuilder bedRowGoal(Identifier id, int minCount, int maxCount) {
        if (minCount == maxCount) {
            return GoalBuilder.of(id)
                .criterion(context, "obtain", BedRowTrigger.create(minCount))
                .name(Component.translatable("bingo.goal.bed_row", minCount))
                .antisynergy("bed_color")
                .infrequency(4)
                .icon(context, new ItemStackTemplate(Items.BED.magenta(), minCount))
                .tags(BingoTags.BUILD, BingoTags.COLOR, BingoTags.OVERWORLD);
        }
        return GoalBuilder.of(id)
            .sub("count", BingoSub.random(minCount, maxCount))
            .criterion(context, "obtain", BedRowTrigger.create(1), subber -> subber.sub("conditions.count", "count"))
            .name(Component.translatable("bingo.goal.bed_row", 0), subber -> subber.sub("with.0", "count"))
            .antisynergy("bed_color")
            .infrequency(4)
            .icon(context, Items.BED.magenta(), subber -> subber.sub("item.count", "count"))
            .tags(BingoTags.BUILD, BingoTags.COLOR, BingoTags.OVERWORLD);
    }

    protected GoalBuilder mineralPillarGoal(Identifier id, TagKey<Block> tag) {
        return GoalBuilder.of(id)
            .criterion(context, "pillar", MineralPillarTrigger.pillar(tag))
            .tags(BingoTags.BUILD);
    }

    protected GoalBuilder blockCubeGoal(Identifier id, Object icon, TagKey<Block> blockTag, Component tagName) {
        final var blocks = context.lookup(Registries.BLOCK);
        return GoalBuilder.of(id)
            .sub("width", BingoSub.random(2, 4))
            .sub("height", BingoSub.random(2, 4))
            .sub("depth", BingoSub.random(2, 4))
            .criterion(
                context,
                "cube",
                ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(AllOfCondition.allOf(
                    LocationCheck.checkLocation(LocationPredicate.Builder.location()
                        .setBlock(BlockPredicate.Builder.block().of(blocks, blockTag))
                    ),
                    BlockPatternCondition.builder().aisle("#")
                        .where('#', BlockPredicate.Builder.block().of(blocks, blockTag))
                        .rotations(BlockPattern.Rotations.ALL)
                )),
                subber -> subber.sub("conditions.location.terms.1.aisles", new CompoundBingoSub(
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
            .icon(context, icon);
    }

    protected GoalBuilder statusEffectsGoal(Identifier id, int minCount, int maxCount) {
        return GoalBuilder.of(id)
            .sub("count", BingoSub.random(minCount, maxCount))
            .criterion(context, "effects", StatusEffectsTrigger.TriggerInstance.statusEffects(1), subber -> subber.sub("conditions.min_count", "count"))
            .progress("effects")
            .name(Component.translatable("bingo.goal.status_effects", 0), subber -> subber.sub("with.0", "count"))
            .icon(context, new CycleIcon(
                EffectIcon.of(MobEffects.SPEED),
                EffectIcon.of(MobEffects.STRENGTH),
                EffectIcon.of(MobEffects.FIRE_RESISTANCE),
                EffectIcon.of(MobEffects.NIGHT_VISION),
                EffectIcon.of(MobEffects.REGENERATION),
                EffectIcon.of(MobEffects.POISON)
            ))
            .tags(BingoTags.ITEM, BingoTags.NETHER, BingoTags.OVERWORLD)
            .antisynergy("status_effects");
    }

    protected GoalBuilder tameWolfVariantsGoal(Identifier id, int count) {
        final var entityTypes = context.lookup(Registries.ENTITY_TYPE);
        final var wolfVariants = context.lookup(Registries.WOLF_VARIANT);

        final List<ResourceKey<WolfVariant>> variants = BingoDataGenUtil.<WolfVariant>getResourceKeysFromClass(WolfVariants.class, Registries.WOLF_VARIANT)
            .stream()
            .distinct() // WolfVariants.DEFAULT is a duplicate key
            .sorted(Comparator.comparing(ResourceKey::identifier))
            .toList();

        final GoalBuilder builder = GoalBuilder.of(id);
        final List<String> criteria = new ArrayList<>();
        final List<GoalIcon> icons = new ArrayList<>();
        for (final ResourceKey<WolfVariant> variant : variants) {
            final Identifier location = variant.identifier();
            final String criterion = "tame_" + location.getNamespace() + "_" + location.getPath();
            builder.criterion(context, criterion, TameAnimalTrigger.TriggerInstance.tamedAnimal(
                EntityPredicate.Builder.entity()
                    .of(entityTypes, EntityTypes.WOLF)
                    .components(DataComponentExactPredicate.builder()
                        .expect(DataComponents.WOLF_VARIANT, wolfVariants.getOrThrow(variant))
                        .build()
                    )
            ));
            criteria.add(criterion);
            final CompoundTag iconData = new CompoundTag();
            iconData.putString("variant", location.toString());
            icons.add(EntityIcon.ofSpawnEgg(EntityTypes.WOLF, iconData, count));
        }

        return builder
            .requirements(atLeastCriteria(criteria, count))
            .progress(new AchievedCriteriaProgressTracker(count))
            .name(Component.translatable("bingo.goal.tame_wolf_variants", count))
            .tooltip("tame_wolf_variants")
            .icon(context, new CycleIcon(icons))
            .tags(BingoTags.ACTION, BingoTags.OVERWORLD, BingoTags.RARE_BIOME)
            .antisynergy("wolf_variants");
    }

    // Requirements that are met when any `count` of the criteria are met. In conjunctive normal form, that's every
    // group of `criteria.size() - count + 1` criteria, since a group only fails if it has none of the met criteria.
    // TODO: a better way, this can create a horribly large number of combinations
    protected static AdvancementRequirements atLeastCriteria(List<String> criteria, int count) {
        final int groupSize = criteria.size() - count + 1;
        final List<List<String>> groups = new ArrayList<>();
        addCriteriaGroups(criteria, groupSize, 0, new ArrayList<>(), groups);
        return new AdvancementRequirements(groups);
    }

    private static void addCriteriaGroups(List<String> criteria, int groupSize, int start, List<String> current, List<List<String>> groups) {
        if (current.size() == groupSize) {
            groups.add(List.copyOf(current));
            return;
        }
        for (int i = start; i <= criteria.size() - (groupSize - current.size()); i++) {
            current.add(criteria.get(i));
            addCriteriaGroups(criteria, groupSize, i + 1, current, groups);
            current.removeLast();
        }
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
        HolderGetter<Block> blocks,
        @SuppressWarnings("unused") HolderGetter<EntityType<?>> entityTypes,
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

    @SuppressWarnings("deprecation")
    protected static GoalIcon createPotionsIcon(BootstrapContextAccess context, Item baseItem) {
        final Set<String> encountered = new HashSet<>();
        return new CycleIcon(
            context.listContextElements(Registries.POTION)
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
