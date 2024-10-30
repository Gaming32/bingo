package io.github.gaming32.bingo.data;

import com.demonwav.mcdev.annotations.Translatable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.data.icons.BlockIcon;
import io.github.gaming32.bingo.data.icons.EmptyIcon;
import io.github.gaming32.bingo.data.icons.GoalIcon;
import io.github.gaming32.bingo.data.progresstrackers.CriterionProgressTracker;
import io.github.gaming32.bingo.data.progresstrackers.EmptyProgressTracker;
import io.github.gaming32.bingo.data.progresstrackers.ProgressTracker;
import io.github.gaming32.bingo.data.subs.BingoSub;
import io.github.gaming32.bingo.game.ActiveGoal;
import io.github.gaming32.bingo.util.BingoCodecs;
import io.github.gaming32.bingo.util.BingoUtil;
import io.github.gaming32.bingo.util.ResourceLocations;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Consumer;

public class BingoGoal {
    public static final Codec<BingoGoal> CODEC = RecordCodecBuilder.<BingoGoal>create(
        instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, BingoSub.CODEC).optionalFieldOf("bingo_subs", Map.of()).forGetter(BingoGoal::getSubs),
            Codec.unboundedMap(Codec.STRING, Codec.PASSTHROUGH).fieldOf("criteria").forGetter(BingoGoal::getCriteria),
            AdvancementRequirements.CODEC.optionalFieldOf("requirements").forGetter(g -> Optional.of(g.requirements)),
            ProgressTracker.CODEC.optionalFieldOf("progress", EmptyProgressTracker.INSTANCE).forGetter(BingoGoal::getProgress),
            BingoCodecs.optionalDynamicField("required_count", BingoCodecs.EMPTY_DYNAMIC.createInt(1)).forGetter(BingoGoal::getRequiredCount),
            RegistryCodecs.homogeneousList(BingoRegistries.TAG).optionalFieldOf("tags", HolderSet.empty()).forGetter(BingoGoal::getTags),
            Codec.PASSTHROUGH.fieldOf("name").forGetter(BingoGoal::getName),
            BingoCodecs.optionalDynamicField("tooltip").forGetter(BingoGoal::getTooltip),
            ResourceLocation.CODEC.optionalFieldOf("tooltip_icon").forGetter(BingoGoal::getTooltipIcon),
            BingoCodecs.optionalDynamicField("icon").forGetter(BingoGoal::getIcon),
            BingoCodecs.optionalPositiveInt("infrequency").forGetter(BingoGoal::getInfrequency),
            BingoCodecs.minifiedSetField(Codec.STRING, "antisynergy").forGetter(BingoGoal::getAntisynergy),
            BingoCodecs.minifiedSetField(Codec.STRING, "catalyst").forGetter(BingoGoal::getCatalyst),
            BingoCodecs.minifiedSetField(Codec.STRING, "reactant").forGetter(BingoGoal::getReactant),
            RegistryFixedCodec.create(BingoRegistries.DIFFICULTY).fieldOf("difficulty").forGetter(BingoGoal::getDifficulty),
            RegistryOps.retrieveGetter(Registries.TRIGGER_TYPE)
        ).apply(instance, BingoGoal::new)
    ).validate(BingoGoal::validate);

    private static Map<ResourceLocation, GoalHolder> goals = Map.of();
    private static Map<Integer, List<GoalHolder>> goalsByDifficulty = Map.of();

    private final Map<String, BingoSub> subs;
    private final Map<String, Dynamic<?>> criteria;
    private final AdvancementRequirements requirements;
    private final ProgressTracker progress;
    private final Dynamic<?> requiredCount;
    private final HolderSet<BingoTag> tags;
    private final Dynamic<?> name;
    private final Dynamic<?> tooltip;
    private final Optional<ResourceLocation> tooltipIcon;
    private final Dynamic<?> icon;
    private final OptionalInt infrequency;
    private final Set<String> antisynergy;
    private final Set<String> catalyst;
    private final Set<String> reactant;
    private final Holder<BingoDifficulty> difficulty;

    private final BingoTag.SpecialType specialType;
    private final boolean requiredOnClient;

    public BingoGoal(
        Map<String, BingoSub> subs,
        Map<String, Dynamic<?>> criteria,
        Optional<AdvancementRequirements> requirements,
        ProgressTracker progress,
        Dynamic<?> requiredCount,
        HolderSet<BingoTag> tags,
        Dynamic<?> name,
        Dynamic<?> tooltip,
        Optional<ResourceLocation> tooltipIcon,
        Dynamic<?> icon,
        OptionalInt infrequency,
        Collection<String> antisynergy,
        Collection<String> catalyst,
        Collection<String> reactant,
        Holder<BingoDifficulty> difficulty,
        HolderGetter<CriterionTrigger<?>> triggerTypes
    ) {
        this.subs = ImmutableMap.copyOf(subs);
        this.criteria = ImmutableMap.copyOf(criteria);
        this.requirements = requirements.orElseGet(() -> AdvancementRequirements.allOf(criteria.keySet()));
        this.progress = progress;
        this.requiredCount = requiredCount;
        this.tags = tags;
        this.name = name;
        this.tooltip = tooltip;
        this.tooltipIcon = tooltipIcon;
        this.icon = icon;
        this.infrequency = infrequency;
        this.antisynergy = ImmutableSet.copyOf(antisynergy);
        this.catalyst = ImmutableSet.copyOf(catalyst);
        this.reactant = ImmutableSet.copyOf(reactant);
        this.difficulty = difficulty;

        BingoTag.SpecialType specialType = BingoTag.SpecialType.NONE;
        for (final var tag : tags) {
            if (tag.value().specialType() != BingoTag.SpecialType.NONE) {
                specialType = tag.value().specialType();
                break;
            }
        }
        this.specialType = specialType;

        boolean requiresClient = false;
        final var triggerCodec = ResourceKey.codec(Registries.TRIGGER_TYPE);
        for (final Dynamic<?> criterion : criteria.values()) {
            final var triggerKey = criterion.get("trigger")
                .flatMap(triggerCodec::parse)
                .result()
                .orElse(null);
            if (triggerKey == null) continue;
            final var trigger = triggerTypes.get(triggerKey);
            if (trigger.isPresent() && trigger.get().value().bingo$requiresClientCode()) {
                requiresClient = true;
                break;
            }
        }
        this.requiredOnClient = requiresClient;
    }

    public DataResult<BingoGoal> validate() {
        final DataResult<AdvancementRequirements> requirementsResult = requirements.validate(criteria.keySet());
        if (requirementsResult.error().isPresent()) {
            return DataResult.error(requirementsResult.error().get()::message);
        }

        final var triggerCodec = ResourceKey.codec(Registries.TRIGGER_TYPE);
        for (final Dynamic<?> criterion : criteria.values()) {
            final var triggerKey = criterion.get("trigger").flatMap(triggerCodec::parse);
            if (triggerKey.isError()) {
                //noinspection OptionalGetWithoutIsPresent
                return DataResult.error(triggerKey.error().get().messageSupplier());
            }
        }

        for (final var tag : tags) {
            final BingoTag.SpecialType type = tag.value().specialType();
            if (type != BingoTag.SpecialType.NONE && type != specialType) {
                return DataResult.error(() -> "Inconsistent specialTypes: " + type + " does not match " + specialType);
            }
        }
        if (specialType == BingoTag.SpecialType.FINISH && requirements.size() != 1) {
            return DataResult.error(() -> "\"finish\" goals must have only ORed requirements");
        }

        final DataResult<ProgressTracker> progressResult = progress.validate(this);
        if (progressResult.error().isPresent()) {
            return DataResult.error(progressResult.error().get()::message);
        }

        return DataResult.success(this);
    }

    public static Set<ResourceLocation> getGoalIds() {
        return goals.keySet();
    }

    @Nullable
    public static BingoGoal.GoalHolder getGoal(ResourceLocation id) {
        return goals.get(id);
    }

    public static List<GoalHolder> getGoalsByDifficulty(int difficulty) {
        if (difficulty < 0) {
            throw new IllegalArgumentException("Difficulties < 0 aren't allowed");
        }
        return goalsByDifficulty.getOrDefault(difficulty, List.of());
    }

    public Map<String, BingoSub> getSubs() {
        return subs;
    }

    public Map<String, Dynamic<?>> getCriteria() {
        return criteria;
    }

    public AdvancementRequirements getRequirements() {
        return requirements;
    }

    public ProgressTracker getProgress() {
        return progress;
    }

    public Dynamic<?> getRequiredCount() {
        return requiredCount;
    }

    public HolderSet<BingoTag> getTags() {
        return tags;
    }

    public Dynamic<?> getName() {
        return name;
    }

    public Dynamic<?> getTooltip() {
        return tooltip;
    }

    public Optional<ResourceLocation> getTooltipIcon() {
        return tooltipIcon;
    }

    public Dynamic<?> getIcon() {
        return icon;
    }

    public OptionalInt getInfrequency() {
        return infrequency;
    }

    public Set<String> getAntisynergy() {
        return antisynergy;
    }

    public Set<String> getCatalyst() {
        return catalyst;
    }

    public Set<String> getReactant() {
        return reactant;
    }

    public Holder<BingoDifficulty> getDifficulty() {
        return difficulty;
    }

    public BingoTag.SpecialType getSpecialType() {
        return specialType;
    }

    public boolean isRequiredOnClient() {
        return requiredOnClient;
    }

    public Map<String, Dynamic<?>> buildSubs(RandomSource rand) {
        final Map<String, Dynamic<?>> result = new LinkedHashMap<>();
        for (final var entry : subs.entrySet()) {
            result.put(entry.getKey(), entry.getValue().substitute(result, rand));
        }
        return ImmutableMap.copyOf(result);
    }

    public MutableComponent buildName(Map<String, Dynamic<?>> referable, RandomSource rand) {
        return BingoUtil.ensureHasFallback(BingoUtil.fromDynamic(
            ComponentSerialization.CODEC,
            performSubstitutions(name, referable, rand)
        ).copy());
    }

    public Optional<Component> buildTooltip(Map<String, Dynamic<?>> referable, RandomSource rand) {
        if (tooltip.getValue() == tooltip.getOps().empty()) {
            return Optional.empty();
        }
        return Optional.of(
            BingoUtil.ensureHasFallback(BingoUtil.fromDynamic(
                ComponentSerialization.CODEC,
                performSubstitutions(tooltip, referable, rand)
            ).copy())
        );
    }

    public GoalIcon buildIcon(Map<String, Dynamic<?>> referable, RandomSource rand) {
        if (icon.getValue() == icon.getOps().empty()) {
            return EmptyIcon.INSTANCE;
        }
        return BingoUtil.fromDynamic(GoalIcon.CODEC, performSubstitutions(icon, referable, rand));
    }

    public Map<String, Criterion<?>> buildCriteria(Map<String, Dynamic<?>> referable, RandomSource rand) {
        final ImmutableMap.Builder<String, Criterion<?>> result = ImmutableMap.builderWithExpectedSize(criteria.size());
        for (final var entry : criteria.entrySet()) {
            result.put(entry.getKey(), BingoUtil.fromDynamic(Criterion.CODEC, performSubstitutions(entry.getValue(), referable, rand)));
        }
        return result.build();
    }

    public int buildRequiredCount(Map<String, Dynamic<?>> referable, RandomSource rand) {
        return BingoUtil.fromDynamic(ExtraCodecs.POSITIVE_INT, performSubstitutions(requiredCount, referable, rand));
    }

    public static Dynamic<?> performSubstitutions(
        Dynamic<?> value,
        Map<String, Dynamic<?>> referable,
        RandomSource rand
    ) {
        final var asList = value.asStreamOpt();
        if (asList.result().isPresent()) {
            return value.createList(
                asList.result().get().map(d -> performSubstitutions(d, referable, rand).convert(d.getOps()))
            );
        }

        if (value.get("bingo_type").result().isPresent()) {
            return BingoUtil.fromDynamic(BingoSub.INNER_CODEC, value).substitute(referable, rand);
        }

        final var asMap = value.asMapOpt();
        if (asMap.result().isPresent()) {
            return value.createMap(asMap.result().get()
                .collect(ImmutableMap.toImmutableMap(
                    Pair::getFirst,
                    e -> performSubstitutions(e.getSecond(), referable, rand).convert(e.getSecond().getOps())
                ))
            );
        }

        return value;
    }

    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public record GoalHolder(ResourceLocation id, BingoGoal goal) {
        public ActiveGoal build(RandomSource rand) {
            final Map<String, Dynamic<?>> subs = goal.buildSubs(rand);
            final Optional<Component> tooltip = goal.buildTooltip(subs, rand);
            final MutableComponent name = goal.buildName(subs, rand);
            tooltip.ifPresent(t -> name.withStyle(s -> s
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, t))
            ));
            return new ActiveGoal(
                id, name, tooltip,
                goal.buildIcon(subs, rand),
                goal.buildCriteria(subs, rand),
                goal.buildRequiredCount(subs, rand),
                Optional.of(goal.difficulty),
                goal.requirements,
                goal.specialType,
                goal.progress,
                goal.tooltipIcon
            );
        }

        @Override
        public String toString() {
            return id.toString();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof GoalHolder h && id.equals(h.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }

    public static final class Builder {
        public static final ThreadLocal<DynamicOps<JsonElement>> JSON_OPS = ThreadLocal.withInitial(() -> JsonOps.INSTANCE);

        private final ResourceLocation id;
        private final ImmutableMap.Builder<String, BingoSub> subs = ImmutableMap.builder();
        private final ImmutableMap.Builder<String, Dynamic<?>> criteria = ImmutableMap.builder();
        private Optional<AdvancementRequirements> requirements = Optional.empty();
        private ProgressTracker progress = EmptyProgressTracker.INSTANCE;
        private Dynamic<?> requiredCount = BingoCodecs.EMPTY_DYNAMIC.createInt(1);
        private AdvancementRequirements.Strategy requirementsStrategy = AdvancementRequirements.Strategy.AND;
        private final ImmutableSet.Builder<ResourceKey<BingoTag>> tags = ImmutableSet.builder();
        private Optional<Dynamic<?>> name = Optional.empty();
        private Dynamic<?> tooltip = BingoCodecs.EMPTY_DYNAMIC;
        private Optional<ResourceLocation> tooltipIcon = Optional.empty();
        private Dynamic<?> icon = BingoCodecs.EMPTY_DYNAMIC;
        private OptionalInt infrequency = OptionalInt.empty();
        private ImmutableSet.Builder<String> antisynergy = ImmutableSet.builder();
        private final ImmutableSet.Builder<String> catalyst = ImmutableSet.builder();
        private final ImmutableSet.Builder<String> reactant = ImmutableSet.builder();
        private Optional<ResourceKey<BingoDifficulty>> difficulty;

        private Builder(ResourceLocation id) {
            this.id = id;
        }

        public Builder sub(String key, BingoSub sub) {
            this.subs.put(key, sub);
            return this;
        }

        public Builder criterion(String key, Criterion<?> criterion) {
            return criterion(key, criterion, subber -> {});
        }

        public Builder criterion(String key, Criterion<?> criterion, Consumer<JsonSubber> subber) {
            JsonSubber json = new JsonSubber(Criterion.CODEC.encodeStart(JSON_OPS.get(), criterion).getOrThrow());
            subber.accept(json);
            this.criteria.put(key, new Dynamic<>(JsonOps.INSTANCE, json.json()));
            return this;
        }

        public Builder requirements(AdvancementRequirements requirements) {
            this.requirements = Optional.of(requirements);
            return this;
        }

        public Builder requirements(AdvancementRequirements.Strategy strategy) {
            this.requirementsStrategy = strategy;
            return this;
        }

        public Builder progress(ProgressTracker progress) {
            this.progress = progress;
            return this;
        }

        public Builder progress(String criterion) {
            return progress(CriterionProgressTracker.unscaled(criterion));
        }

        public Builder requiredCount(int requiredCount) {
            this.requiredCount = BingoCodecs.EMPTY_DYNAMIC.createInt(requiredCount);
            return this;
        }

        public Builder requiredCount(BingoSub requiredCountSub) {
            this.requiredCount = BingoUtil.toDynamic(BingoSub.INNER_CODEC, requiredCountSub);
            return this;
        }

        @SafeVarargs
        public final Builder tags(ResourceKey<BingoTag>... tags) {
            this.tags.add(tags);
            return this;
        }

        public Builder name(@Translatable(prefix = "bingo.goal.") String name) {
            return this.name(Component.translatable("bingo.goal." + name));
        }

        public Builder name(Component name) {
            return this.name(name, subber -> {});
        }

        public Builder name(Component name, Consumer<JsonSubber> subber) {
            JsonSubber json = new JsonSubber(ComponentSerialization.CODEC.encodeStart(JSON_OPS.get(), name).getOrThrow());
            subber.accept(json);
            this.name = Optional.of(new Dynamic<>(JsonOps.INSTANCE, json.json()));
            return this;
        }

        public Builder tooltip(@Translatable(prefix = "bingo.goal.", suffix = ".tooltip") String tooltip) {
            return this.tooltip(Component.translatable("bingo.goal." + tooltip + ".tooltip"));
        }

        public Builder tooltip(Component tooltip) {
            return this.tooltip(tooltip, subber -> {});
        }

        public Builder tooltip(Component tooltip, Consumer<JsonSubber> subber) {
            JsonSubber json = new JsonSubber(ComponentSerialization.CODEC.encodeStart(JSON_OPS.get(), tooltip).getOrThrow());
            subber.accept(json);
            this.tooltip = new Dynamic<>(JsonOps.INSTANCE, json.json());
            return this;
        }

        public Builder tooltipIcon(ResourceLocation tooltipIcon) {
            this.tooltipIcon = Optional.of(tooltipIcon);
            return this;
        }

        public Builder icon(Object icon) {
            return icon(icon, subber -> {});
        }

        public Builder icon(Object icon, Consumer<JsonSubber> subber) {
            return icon(GoalIcon.infer(icon), subber);
        }

        public Builder icon(Block icon, ItemLike fallback) {
            return icon(icon, fallback, subber -> {});
        }

        public Builder icon(Block icon, ItemLike fallback, Consumer<JsonSubber> subber) {
            return icon(BlockIcon.ofBlockAndItem(icon, fallback), subber);
        }

        public Builder icon(GoalIcon icon) {
            return icon(icon, subber -> {});
        }

        public Builder icon(GoalIcon icon, Consumer<JsonSubber> subber) {
            JsonSubber jsonSubber = new JsonSubber(GoalIcon.CODEC.encodeStart(JSON_OPS.get(), icon).getOrThrow());
            subber.accept(jsonSubber);
            this.icon = new Dynamic<>(JsonOps.INSTANCE, jsonSubber.json());
            return this;
        }

        public Builder infrequency(int infrequency) {
            this.infrequency = OptionalInt.of(infrequency);
            return this;
        }

        public Builder setAntisynergy(String... antisynergy) {
            this.antisynergy = ImmutableSet.builderWithExpectedSize(antisynergy.length);
            return this.antisynergy(antisynergy);
        }

        public Builder antisynergy(String... antisynergy) {
            this.antisynergy.add(antisynergy);
            return this;
        }

        public Builder catalyst(String... catalyst) {
            this.catalyst.add(catalyst);
            return this;
        }

        public Builder reactant(String... reactant) {
            this.reactant.add(reactant);
            return this;
        }

        public Builder difficulty(ResourceKey<BingoDifficulty> difficulty) {
            this.difficulty = Optional.of(difficulty);
            return this;
        }

        public GoalHolder build(HolderLookup.Provider registries) {
            final Map<String, Dynamic<?>> criteria = this.criteria.build();
            return new GoalHolder(id, new BingoGoal(
                subs.buildOrThrow(),
                criteria,
                requirements.or(() -> Optional.of(requirementsStrategy.create(criteria.keySet()))),
                progress,
                requiredCount,
                HolderSet.direct(registries.lookupOrThrow(BingoRegistries.TAG)::getOrThrow, tags.build()),
                name.orElseThrow(() -> new IllegalStateException("Bingo goal name has not been set")),
                tooltip,
                tooltipIcon,
                icon,
                infrequency,
                antisynergy.build(),
                catalyst.build(),
                reactant.build(),
                registries.lookupOrThrow(BingoRegistries.DIFFICULTY).getOrThrow(
                    difficulty.orElseThrow(() -> new IllegalStateException("Bingo goal difficulty has not been set"))
                ),
                registries.lookupOrThrow(Registries.TRIGGER_TYPE)
            ));
        }

    }

    public static class ReloadListener extends SimpleJsonResourceReloadListener<BingoGoal> {
        public static final ResourceLocation ID = ResourceLocations.bingo("goals");

        public ReloadListener(HolderLookup.Provider registries) {
            super(registries, CODEC, "bingo/goal");
        }

        @NotNull
        @Override
        public String getName() {
            return ID.toString();
        }

        @Override
        protected void apply(Map<ResourceLocation, BingoGoal> goals, ResourceManager resourceManager, ProfilerFiller profiler) {
            final ImmutableMap.Builder<ResourceLocation, GoalHolder> result = ImmutableMap.builder();
            final Map<Integer, ImmutableList.Builder<GoalHolder>> byDifficulty = new HashMap<>();
            for (final var entry : goals.entrySet()) {
                final var goal = entry.getValue();
                final GoalHolder holder = new GoalHolder(entry.getKey(), goal);
                result.put(holder.id, holder);
                byDifficulty.computeIfAbsent(goal.difficulty.value().number(), k -> ImmutableList.builder()).add(holder);
            }
            BingoGoal.goals = result.build();
            goalsByDifficulty = byDifficulty.entrySet()
                .stream()
                .collect(ImmutableMap.toImmutableMap(
                    Map.Entry::getKey,
                    e -> e.getValue().build()
                ));
            Bingo.LOGGER.info("Loaded {} bingo goals", BingoGoal.goals.size());
        }
    }
}
