package io.github.gaming32.bingo.data;

import com.demonwav.mcdev.annotations.Translatable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.data.icons.BlockIcon;
import io.github.gaming32.bingo.data.icons.EmptyIcon;
import io.github.gaming32.bingo.data.icons.GoalIcon;
import io.github.gaming32.bingo.data.progresstrackers.CriterionProgressTracker;
import io.github.gaming32.bingo.data.progresstrackers.EmptyProgressTracker;
import io.github.gaming32.bingo.data.progresstrackers.ProgressTracker;
import io.github.gaming32.bingo.data.subs.BingoSub;
import io.github.gaming32.bingo.game.ActiveGoal;
import io.github.gaming32.bingo.util.BingoUtil;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

// TODO: Move this class to a Codec
public class BingoGoal {
    private static final Dynamic<?> EMPTY_JSON = new Dynamic<>(JsonOps.INSTANCE);

    private static Map<ResourceLocation, Holder> goals = Map.of();
    private static Map<Integer, List<Holder>> goalsByDifficulty = Map.of();

    private final Map<String, BingoSub> subs;
    private final Map<String, Dynamic<?>> criteria;
    private final AdvancementRequirements requirements;
    private final ProgressTracker progress;
    private final Set<BingoTag.Holder> tags;
    private final Dynamic<?> name;
    private final Dynamic<?> tooltip;
    private final Optional<ResourceLocation> tooltipIcon;
    private final Dynamic<?> icon;
    private final Optional<Integer> infrequency;
    private final Set<String> antisynergy;
    private final Set<String> catalyst;
    private final Set<String> reactant;
    private final BingoDifficulty.Holder difficulty;

    private final Set<ResourceLocation> tagIds;
    private final BingoTag.SpecialType specialType;
    private final boolean requiredOnClient;

    public BingoGoal(
        Map<String, BingoSub> subs,
        Map<String, Dynamic<?>> criteria,
        AdvancementRequirements requirements,
        ProgressTracker progress,
        Collection<BingoTag.Holder> tags,
        Dynamic<?> name,
        Dynamic<?> tooltip,
        Optional<ResourceLocation> tooltipIcon,
        Dynamic<?> icon,
        Optional<Integer> infrequency,
        Collection<String> antisynergy,
        Collection<String> catalyst,
        Collection<String> reactant,
        BingoDifficulty.Holder difficulty
    ) {
        this.subs = ImmutableMap.copyOf(subs);
        this.criteria = ImmutableMap.copyOf(criteria);
        this.requirements = requirements;
        this.progress = progress;
        this.tags = ImmutableSet.copyOf(tags);
        this.name = name;
        this.tooltip = tooltip;
        this.tooltipIcon = tooltipIcon;
        this.icon = icon;
        this.infrequency = infrequency;
        this.antisynergy = ImmutableSet.copyOf(antisynergy);
        this.catalyst = ImmutableSet.copyOf(catalyst);
        this.reactant = ImmutableSet.copyOf(reactant);
        this.difficulty = difficulty;

        this.tagIds = tags.stream().map(BingoTag.Holder::id).collect(ImmutableSet.toImmutableSet());

        BingoTag.SpecialType specialType = BingoTag.SpecialType.NONE;
        for (final BingoTag.Holder tag : tags) {
            if (tag.tag().specialType() != BingoTag.SpecialType.NONE) {
                specialType = tag.tag().specialType();
                break;
            }
        }
        this.specialType = specialType;

        boolean requiresClient = false;
        for (final Dynamic<?> criterion : criteria.values()) {
            final ResourceLocation triggerId = new ResourceLocation(criterion.get("trigger").asString("no_trigger_present"));
            final CriterionTrigger<?> trigger = BuiltInRegistries.TRIGGER_TYPES.get(triggerId);
            if (trigger != null && trigger.requiresClientCode()) {
                requiresClient = true;
                break;
            }
        }
        this.requiredOnClient = requiresClient;
    }

    public DataResult<BingoGoal> validate() {
        for (final BingoTag.Holder tag : tags) {
            final BingoTag.SpecialType type = tag.tag().specialType();
            if (type != BingoTag.SpecialType.NONE && type != specialType) {
                return DataResult.error(() -> "Inconsistent specialTypes: " + type + " does not match " + specialType);
            }
        }
        if (specialType == BingoTag.SpecialType.FINISH && requirements.size() != 1) {
            return DataResult.error(() -> "\"finish\" goals must have only ORed requirements");
        }
        final DataResult<ProgressTracker> progressTrackerResult = progress.validate(this);
        if (progressTrackerResult.error().isPresent()) {
            return DataResult.error(progressTrackerResult.error().get()::message);
        }
        return DataResult.success(this);
    }

    public static Set<ResourceLocation> getGoalIds() {
        return goals.keySet();
    }

    @Nullable
    public static Holder getGoal(ResourceLocation id) {
        return goals.get(id);
    }

    public static List<Holder> getGoalsByDifficulty(int difficulty) {
        if (difficulty < 0) {
            throw new IllegalArgumentException("Difficulties < 0 aren't allowed");
        }
        return goalsByDifficulty.getOrDefault(difficulty, List.of());
    }

    public static BingoGoal deserialize(JsonObject json) {
        final Map<String, Dynamic<?>> criteria = GsonHelper.getAsJsonObject(json, "criteria")
            .entrySet()
            .stream()
            .collect(ImmutableMap.toImmutableMap(
                Map.Entry::getKey,
                e -> new Dynamic<>(JsonOps.INSTANCE, e.getValue())
            ));

        final JsonArray reqArray = GsonHelper.getAsJsonArray(json, "requirements", new JsonArray());
        final AdvancementRequirements requirements = reqArray.isEmpty()
            ? AdvancementRequirements.allOf(criteria.keySet())
            : BingoUtil.fromJsonElement(AdvancementRequirements.CODEC, reqArray);
        requirements.validate(criteria.keySet())
            .error()
            .ifPresent(p -> {
                throw new JsonParseException(p.message());
            });

        final ResourceLocation difficultyId = new ResourceLocation(GsonHelper.getAsString(json, "difficulty"));
        final BingoDifficulty.Holder difficulty = BingoDifficulty.byId(difficultyId);
        if (difficulty == null) {
            throw new JsonParseException("Unknown difficulty: " + difficultyId);
        }

        return new BingoGoal(
            GsonHelper.getAsJsonObject(json, "bingo_subs", new JsonObject())
                .entrySet()
                .stream()
                .collect(ImmutableMap.toImmutableMap(
                    Map.Entry::getKey,
                    e -> BingoUtil.fromJsonElement(BingoSub.CODEC, e.getValue())
                )),
            criteria, requirements,
            json.has("progress")
                ? ProgressTracker.deserialize(GsonHelper.getAsJsonObject(json, "progress"))
                : EmptyProgressTracker.INSTANCE,
            GsonHelper.getAsJsonArray(json, "tags", new JsonArray())
                .asList()
                .stream()
                .map(e -> {
                    final ResourceLocation key = new ResourceLocation(GsonHelper.convertToString(e, "tag"));
                    final BingoTag.Holder tag = BingoTag.getTag(key);
                    if (tag == null) {
                        throw new JsonParseException("Unknown bingo tag: " + key);
                    }
                    return tag;
                })
                .collect(ImmutableSet.toImmutableSet()),
            new Dynamic<>(JsonOps.INSTANCE, GsonHelper.getNonNull(json, "name")),
            json.has("tooltip") ? new Dynamic<>(JsonOps.INSTANCE, json.get("tooltip")) : EMPTY_JSON,
            json.has("tooltip_icon")
                ? Optional.of(new ResourceLocation(GsonHelper.getAsString(json, "tooltip_icon")))
                : Optional.empty(),
            json.has("icon") ? new Dynamic<>(JsonOps.INSTANCE, json.get("icon")) : EMPTY_JSON,
            json.has("infrequency")
                ? Optional.of(GsonHelper.getAsInt(json, "infrequency"))
                : Optional.empty(),
            getSetString(json, "antisynergy"),
            getSetString(json, "catalyst"),
            getSetString(json, "reactant"),
            difficulty
        );
    }

    private static Set<String> getSetString(JsonObject json, String key) {
        return json.has(key) ? (
            json.get(key).isJsonArray()
                ? GsonHelper.getAsJsonArray(json, key)
                    .asList()
                    .stream()
                    .map(a -> GsonHelper.convertToString(a, key))
                    .collect(ImmutableSet.toImmutableSet())
                : ImmutableSet.of(GsonHelper.getAsString(json, key))
        ) : ImmutableSet.of();
    }

    public JsonObject serialize() {
        final JsonObject result = new JsonObject();

        if (!subs.isEmpty()) {
            final JsonObject subsObj = new JsonObject();
            for (final var entry : subs.entrySet()) {
                subsObj.add(entry.getKey(), BingoUtil.toJsonElement(BingoSub.CODEC, entry.getValue()));
            }
            result.add("bingo_subs", subsObj);
        }

        final JsonObject criteriaObj = new JsonObject();
        for (final var entry : criteria.entrySet()) {
            criteriaObj.add(entry.getKey(), entry.getValue().convert(JsonOps.INSTANCE).getValue());
        }
        result.add("criteria", criteriaObj);

        result.add("requirements", BingoUtil.toJsonElement(AdvancementRequirements.CODEC, requirements));

        if (progress != EmptyProgressTracker.INSTANCE) {
            result.add("progress", progress.serializeToJson());
        }

        if (!tags.isEmpty()) {
            final JsonArray array = new JsonArray(tags.size());
            for (final BingoTag.Holder tag : tags) {
                array.add(tag.id().toString());
            }
            result.add("tags", array);
        }

        result.add("name", name.convert(JsonOps.INSTANCE).getValue());

        if (tooltip.getValue() != tooltip.getOps().empty()) {
            result.add("tooltip", tooltip.convert(JsonOps.INSTANCE).getValue());
        }

        tooltipIcon.ifPresent(location -> result.addProperty("tooltip_icon", location.toString()));

        if (icon.getValue() != icon.getOps().empty()) {
            result.add("icon", icon.convert(JsonOps.INSTANCE).getValue());
        }

        infrequency.ifPresent(integer -> result.addProperty("infrequency", integer));

        serializeSetString(result, "antisynergy", antisynergy);
        serializeSetString(result, "catalyst", catalyst);
        serializeSetString(result, "reactant", reactant);

        result.addProperty("difficulty", difficulty.id().toString());

        return result;
    }

    private static void serializeSetString(JsonObject json, String key, Set<String> set) {
        if (!set.isEmpty()) {
            if (set.size() == 1) {
                json.addProperty(key, set.iterator().next());
            } else {
                final JsonArray array = new JsonArray(set.size());
                for (final String value : set) {
                    array.add(value);
                }
                json.add(key, array);
            }
        }
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

    public Set<BingoTag.Holder> getTags() {
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

    public Optional<Integer> getInfrequency() {
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

    public BingoDifficulty.Holder getDifficulty() {
        return difficulty;
    }

    public Set<ResourceLocation> getTagIds() {
        return tagIds;
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
        return Bingo.ensureHasFallback(BingoUtil.fromDynamic(
            ComponentSerialization.CODEC,
            performSubstitutions(name, referable, rand)
        ).copy());
    }

    public Optional<Component> buildTooltip(Map<String, Dynamic<?>> referable, RandomSource rand) {
        if (tooltip.getValue() == tooltip.getOps().empty()) {
            return Optional.empty();
        }
        return Optional.of(
            Bingo.ensureHasFallback(BingoUtil.fromDynamic(
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

    public static Dynamic<?> performSubstitutions(
        Dynamic<?> value,
        Map<String, Dynamic<?>> referable,
        RandomSource rand
    ) {
        final var asList = value.asStreamOpt();
        if (asList.error().isEmpty()) {
            return value.createList(
                asList.result()
                    .orElseThrow()
                    .map(d -> performSubstitutions(d, referable, rand))
            );
        }

        if (value.get("bingo_type").result().isPresent()) {
            return BingoUtil.fromDynamic(BingoSub.INNER_CODEC, value).substitute(referable, rand);
        }

        final var asMap = value.getMapValues();
        if (asMap.error().isEmpty()) {
            return value.createMap(
                asMap.result()
                    .orElseThrow()
                    .entrySet()
                    .stream()
                    .collect(ImmutableMap.toImmutableMap(
                        Map.Entry::getKey,
                        e -> performSubstitutions(e.getValue(), referable, rand)
                    ))
            );
        }

        return value;
    }

    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public record Holder(ResourceLocation id, BingoGoal goal) {
        public ActiveGoal build(RandomSource rand) {
            final Map<String, Dynamic<?>> subs = goal.buildSubs(rand);
            final Optional<Component> tooltip = goal.buildTooltip(subs, rand);
            final MutableComponent name = goal.buildName(subs, rand);
            tooltip.ifPresent(t -> name.withStyle(s -> s
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, t))
            ));
            return new ActiveGoal(
                this, name, tooltip,
                goal.buildIcon(subs, rand),
                goal.buildCriteria(subs, rand)
            );
        }

        @Override
        public String toString() {
            return id.toString();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Holder h && id.equals(h.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }

    public static final class Builder {
        private final ResourceLocation id;
        private final ImmutableMap.Builder<String, BingoSub> subs = ImmutableMap.builder();
        private final ImmutableMap.Builder<String, Dynamic<?>> criteria = ImmutableMap.builder();
        private Optional<AdvancementRequirements> requirements = Optional.empty();
        private ProgressTracker progress = EmptyProgressTracker.INSTANCE;
        private AdvancementRequirements.Strategy requirementsStrategy = AdvancementRequirements.Strategy.AND;
        private final ImmutableSet.Builder<BingoTag.Holder> tags = ImmutableSet.builder();
        private Optional<Dynamic<?>> name = Optional.empty();
        private Dynamic<?> tooltip = EMPTY_JSON;
        private Optional<ResourceLocation> tooltipIcon = Optional.empty();
        private Dynamic<?> icon = EMPTY_JSON;
        private Optional<Integer> infrequency = Optional.empty();
        private ImmutableList.Builder<String> antisynergy = ImmutableList.builder();
        private final ImmutableList.Builder<String> catalyst = ImmutableList.builder();
        private final ImmutableList.Builder<String> reactant = ImmutableList.builder();
        private Optional<BingoDifficulty.Holder> difficulty;

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
            JsonSubber json = new JsonSubber(BingoUtil.toJsonElement(Criterion.CODEC, criterion));
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

        public Builder tags(ResourceLocation... tags) {
            for (ResourceLocation tag : tags) {
                this.tags.add(Optional.ofNullable(BingoTag.getTag(tag))
                    .orElseGet(() -> BingoTag.builder(tag).build())
                );
            }
            return this;
        }

        public Builder name(@Translatable(prefix = "bingo.goal.") String name) {
            return this.name(Component.translatable("bingo.goal." + name));
        }

        public Builder name(Component name) {
            return this.name(name, subber -> {});
        }

        public Builder name(Component name, Consumer<JsonSubber> subber) {
            JsonSubber json = new JsonSubber(Component.Serializer.toJsonTree(name));
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
            JsonSubber json = new JsonSubber(Component.Serializer.toJsonTree(tooltip));
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
            return icon(new BlockIcon(icon.defaultBlockState(), new ItemStack(fallback)), subber);
        }

        public Builder icon(GoalIcon icon) {
            return icon(icon, subber -> {});
        }

        public Builder icon(GoalIcon icon, Consumer<JsonSubber> subber) {
            JsonSubber jsonSubber = new JsonSubber(BingoUtil.toJsonElement(GoalIcon.CODEC, icon));
            subber.accept(jsonSubber);
            this.icon = new Dynamic<>(JsonOps.INSTANCE, jsonSubber.json());
            return this;
        }

        public Builder infrequency(int infrequency) {
            this.infrequency = Optional.of(infrequency);
            return this;
        }

        public Builder setAntisynergy(String... antisynergy) {
            this.antisynergy = ImmutableList.builderWithExpectedSize(antisynergy.length);
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

        public Builder difficulty(ResourceLocation difficulty) {
            this.difficulty = Optional.ofNullable(BingoDifficulty.byId(difficulty))
                .or(() -> Optional.of(BingoDifficulty.builder(difficulty).number(0).build()));
            return this;
        }

        public BingoGoal.Holder build() {
            final Map<String, Dynamic<?>> criteria = this.criteria.build();
            return new Holder(id, new BingoGoal(
                subs.buildOrThrow(),
                criteria,
                requirements.orElseGet(() -> requirementsStrategy.create(criteria.keySet())),
                progress,
                tags.build(),
                name.orElseThrow(() -> new IllegalStateException("Bingo goal name has not been set")),
                tooltip,
                tooltipIcon,
                icon,
                infrequency,
                antisynergy.build(),
                catalyst.build(),
                reactant.build(),
                difficulty.orElseThrow(() -> new IllegalStateException("Bingo goal difficulty has not been set"))
            ));
        }

    }

    public static class ReloadListener extends SimpleJsonResourceReloadListener {
        public static final ResourceLocation ID = new ResourceLocation("bingo:goals");
        private static final Gson GSON = new GsonBuilder().create();

        public ReloadListener() {
            super(GSON, "bingo/goals");
        }

        @NotNull
        @Override
        public String getName() {
            return ID.toString();
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> jsons, ResourceManager resourceManager, ProfilerFiller profiler) {
            final ImmutableMap.Builder<ResourceLocation, Holder> result = ImmutableMap.builder();
            final Map<Integer, ImmutableList.Builder<Holder>> byDifficulty = new HashMap<>();
            for (final var entry : jsons.entrySet()) {
                try {
                    final JsonObject json = GsonHelper.convertToJsonObject(entry.getValue(), "bingo goal");
                    final BingoGoal goal = deserialize(json);
                    final Holder holder = new Holder(entry.getKey(), goal);
                    result.put(holder.id, holder);
                    byDifficulty.computeIfAbsent(goal.difficulty.difficulty().number(), k -> ImmutableList.builder()).add(holder);
                } catch (Exception e) {
                    Bingo.LOGGER.error("Parsing error in bingo goal {}: {}", entry.getKey(), e.getMessage());
                }
            }
            goals = result.build();
            goalsByDifficulty = byDifficulty.entrySet()
                .stream()
                .collect(ImmutableMap.toImmutableMap(
                    Map.Entry::getKey,
                    e -> e.getValue().build()
                ));
            Bingo.LOGGER.info("Loaded {} bingo goals", goals.size());
        }
    }
}
