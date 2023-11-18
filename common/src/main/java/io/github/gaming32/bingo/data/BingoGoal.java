package io.github.gaming32.bingo.data;

import com.demonwav.mcdev.annotations.Translatable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.data.icons.BlockIcon;
import io.github.gaming32.bingo.data.icons.EmptyIcon;
import io.github.gaming32.bingo.data.icons.GoalIcon;
import io.github.gaming32.bingo.data.progresstrackers.CriterionProgressTracker;
import io.github.gaming32.bingo.data.progresstrackers.EmptyProgressTracker;
import io.github.gaming32.bingo.data.progresstrackers.ProgressTracker;
import io.github.gaming32.bingo.data.subs.BingoSub;
import io.github.gaming32.bingo.game.ActiveGoal;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.storage.loot.LootDataManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class BingoGoal {
    private static Map<ResourceLocation, BingoGoal> goals = Map.of();
    private static Map<Integer, List<BingoGoal>> goalsByDifficulty = Map.of();

    private final ResourceLocation id;
    private final Map<String, BingoSub> subs;
    private final Map<String, JsonObject> criteria;
    private final AdvancementRequirements requirements;
    private final ProgressTracker progress;
    private final Set<BingoTag.Holder> tags;
    private final JsonElement name;
    @Nullable
    private final JsonElement tooltip;
    @Nullable
    private final ResourceLocation tooltipIcon;
    @Nullable
    private final JsonObject icon;
    @Nullable
    private final Integer infrequency;
    private final Set<String> antisynergy;
    private final Set<String> catalyst;
    private final Set<String> reactant;
    private final BingoDifficulty.Holder difficulty;

    private final Set<ResourceLocation> tagIds;
    private final BingoTag.SpecialType specialType;
    private final boolean requiredOnClient;

    public BingoGoal(
        ResourceLocation id,
        Map<String, BingoSub> subs,
        Map<String, JsonObject> criteria,
        AdvancementRequirements requirements,
        ProgressTracker progress,
        Collection<BingoTag.Holder> tags,
        JsonElement name,
        @Nullable JsonElement tooltip,
        @Nullable ResourceLocation tooltipIcon,
        @Nullable JsonObject icon,
        @Nullable Integer infrequency,
        Collection<String> antisynergy,
        Collection<String> catalyst,
        Collection<String> reactant,
        BingoDifficulty.Holder difficulty
    ) {
        this.id = id;
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
            if (tag.tag().specialType() == BingoTag.SpecialType.NONE) continue;
            if (specialType == BingoTag.SpecialType.NONE) {
                specialType = tag.tag().specialType();
                continue;
            }
            if (tag.tag().specialType() != specialType) {
                throw new IllegalArgumentException("Inconsistent specialTypes: " + tag.tag().specialType() + " does not match " + specialType);
            }
        }
        if (specialType == BingoTag.SpecialType.FINISH && requirements.size() != 1) {
            throw new IllegalArgumentException("\"finish\" goals must have only ORed requirements");
        }
        this.specialType = specialType;

        boolean requiresClient = false;
        for (final JsonObject criterion : criteria.values()) {
            final ResourceLocation triggerId = new ResourceLocation(GsonHelper.getAsString(criterion, "trigger"));
            final CriterionTrigger<?> trigger = CriteriaTriggers.getCriterion(triggerId);
            if (trigger == null) {
                throw new IllegalArgumentException("Unknown criterion trigger " + triggerId);
            }
            if (trigger.requiresClientCode()) {
                requiresClient = true;
            }
        }
        this.requiredOnClient = requiresClient;

        if (progress != null) {
            progress.validate(this);
        }
    }

    public static Set<ResourceLocation> getGoalIds() {
        return goals.keySet();
    }

    public static BingoGoal getGoal(ResourceLocation id) {
        return goals.get(id);
    }

    public static List<BingoGoal> getGoalsByDifficulty(int difficulty) {
        if (difficulty < 0) {
            throw new IllegalArgumentException("Difficulties < 0 aren't allowed");
        }
        return goalsByDifficulty.getOrDefault(difficulty, List.of());
    }

    public static BingoGoal deserialize(ResourceLocation id, JsonObject json) {
        final Map<String, JsonObject> criteria = GsonHelper.getAsJsonObject(json, "criteria")
            .entrySet()
            .stream()
            .collect(ImmutableMap.toImmutableMap(
                Map.Entry::getKey,
                e -> GsonHelper.convertToJsonObject(e.getValue(), "criterion")
            ));

        final JsonArray reqArray = GsonHelper.getAsJsonArray(json, "requirements", new JsonArray());
        final AdvancementRequirements requirements = reqArray.isEmpty()
            ? AdvancementRequirements.allOf(criteria.keySet())
            : AdvancementRequirements.fromJson(reqArray, criteria.keySet());

        final ResourceLocation difficultyId = new ResourceLocation(GsonHelper.getAsString(json, "difficulty"));
        final BingoDifficulty.Holder difficulty = BingoDifficulty.byId(difficultyId);
        if (difficulty == null) {
            throw new JsonParseException("Unknown difficulty: " + difficultyId);
        }

        return new BingoGoal(
            id,
            GsonHelper.getAsJsonObject(json, "bingo_subs", new JsonObject())
                .entrySet()
                .stream()
                .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, e -> BingoSub.deserialize(e.getValue()))),
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
            GsonHelper.getNonNull(json, "name"),
            json.get("tooltip"),
            json.has("tooltip_icon")
                ? new ResourceLocation(GsonHelper.getAsString(json, "tooltip_icon")) : null,
            GsonHelper.getAsJsonObject(json, "icon", null),
            json.has("infrequency") ? GsonHelper.getAsInt(json, "infrequency") : null,
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
                subsObj.add(entry.getKey(), entry.getValue().serializeToJson());
            }
            result.add("bingo_subs", subsObj);
        }

        final JsonObject criteriaObj = new JsonObject();
        for (final var entry : criteria.entrySet()) {
            criteriaObj.add(entry.getKey(), entry.getValue());
        }
        result.add("criteria", criteriaObj);

        result.add("requirements", requirements.toJson());

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

        result.add("name", name);

        if (tooltip != null) {
            result.add("tooltip", tooltip);
        }

        if (tooltipIcon != null) {
            result.addProperty("tooltip_icon", tooltipIcon.toString());
        }

        if (icon != null) {
            result.add("icon", icon);
        }

        if (infrequency != null) {
            result.addProperty("infrequency", infrequency);
        }

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

    public ResourceLocation getId() {
        return id;
    }

    public Map<String, BingoSub> getSubs() {
        return subs;
    }

    public Map<String, JsonObject> getCriteria() {
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

    public JsonElement getName() {
        return name;
    }

    @Nullable
    public JsonElement getTooltip() {
        return tooltip;
    }

    @Nullable
    public ResourceLocation getTooltipIcon() {
        return tooltipIcon;
    }

    @Nullable
    public JsonObject getIcon() {
        return icon;
    }

    @Nullable
    public Integer getInfrequency() {
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

    public ActiveGoal build(RandomSource rand, LootDataManager lootData) {
        final Map<String, JsonElement> subs = buildSubs(rand);
        final Component tooltip = buildTooltip(subs, rand);
        final MutableComponent name = buildName(subs, rand);
        if (tooltip != null) {
            name.withStyle(s -> s
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))
            );
        }
        return new ActiveGoal(
            this, name, tooltip,
            buildIcon(subs, rand),
            buildCriteria(subs, rand, lootData)
        );
    }

    public Map<String, JsonElement> buildSubs(RandomSource rand) {
        final Map<String, JsonElement> result = new LinkedHashMap<>();
        for (final var entry : subs.entrySet()) {
            result.put(entry.getKey(), entry.getValue().substitute(result, rand));
        }
        return ImmutableMap.copyOf(result);
    }

    public MutableComponent buildName(Map<String, JsonElement> referable, RandomSource rand) {
        MutableComponent component = Component.Serializer.fromJson(performSubstitutions(name, referable, rand));
        return component == null ? Component.empty() : Bingo.ensureHasFallback(component);
    }

    public MutableComponent buildTooltip(Map<String, JsonElement> referable, RandomSource rand) {
        if (tooltip == null) {
            return null;
        }
        MutableComponent component = Component.Serializer.fromJson(performSubstitutions(tooltip, referable, rand));
        return component == null ? Component.empty() : Bingo.ensureHasFallback(component);
    }

    public GoalIcon buildIcon(Map<String, JsonElement> referable, RandomSource rand) {
        if (icon == null) {
            return EmptyIcon.INSTANCE;
        }
        return GoalIcon.deserialize(performSubstitutions(icon, referable, rand));
    }

    public Map<String, Criterion<?>> buildCriteria(
        Map<String, JsonElement> referable,
        RandomSource rand,
        LootDataManager lootData
    ) {
        final DeserializationContext context = new DeserializationContext(id, lootData);
        final ImmutableMap.Builder<String, Criterion<?>> result = ImmutableMap.builderWithExpectedSize(criteria.size());
        for (final var entry : criteria.entrySet()) {
            result.put(entry.getKey(), Criterion.criterionFromJson(GsonHelper.convertToJsonObject(
                performSubstitutions(entry.getValue(), referable, rand), "criterion"
            ), context));
        }
        return result.build();
    }

    public static JsonElement performSubstitutions(
        JsonElement value,
        Map<String, JsonElement> referable,
        RandomSource rand
    ) {
        if (value.isJsonArray()) {
            final JsonArray array = value.getAsJsonArray();
            final JsonArray result = new JsonArray();
            for (final JsonElement subValue : array) {
                result.add(performSubstitutions(subValue, referable, rand));
            }
            return result;
        }
        if (value.isJsonObject()) {
            final JsonObject obj = value.getAsJsonObject();
            if (obj.has("bingo_type")) {
                return BingoSub.deserializeInner(obj).substitute(referable, rand);
            }
            final JsonObject result = new JsonObject();
            for (final var entry : obj.entrySet()) {
                result.add(entry.getKey(), performSubstitutions(entry.getValue(), referable, rand));
            }
            return result;
        }
        return value;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public static final class Builder {
        private final ResourceLocation id;
        private final ImmutableMap.Builder<String, BingoSub> subs = ImmutableMap.builder();
        private final ImmutableMap.Builder<String, JsonObject> criteria = ImmutableMap.builder();
        private Optional<AdvancementRequirements> requirements = Optional.empty();
        private ProgressTracker progress = EmptyProgressTracker.INSTANCE;
        private AdvancementRequirements.Strategy requirementsStrategy = AdvancementRequirements.Strategy.AND;
        private final ImmutableSet.Builder<BingoTag.Holder> tags = ImmutableSet.builder();
        private Optional<JsonElement> name = Optional.empty();
        @Nullable
        private JsonElement tooltip;
        @Nullable
        private ResourceLocation tooltipIcon;
        @Nullable
        private JsonObject icon;
        @Nullable
        private Integer infrequency;
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
            JsonSubber json = new JsonSubber(criterion.serializeToJson());
            subber.accept(json);
            this.criteria.put(key, json.json().getAsJsonObject());
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
            this.name = Optional.of(json.json());
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
            this.tooltip = json.json();
            return this;
        }

        public Builder tooltipIcon(ResourceLocation tooltipIcon) {
            this.tooltipIcon = tooltipIcon;
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
            JsonSubber jsonSubber = new JsonSubber(icon.serializeToJson());
            subber.accept(jsonSubber);
            this.icon = jsonSubber.json().getAsJsonObject();
            return this;
        }

        public Builder infrequency(int infrequency) {
            this.infrequency = infrequency;
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

        public BingoGoal build() {
            final Map<String, JsonObject> criteria = this.criteria.build();
            return new BingoGoal(
                id,
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
            );
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
            final ImmutableMap.Builder<ResourceLocation, BingoGoal> result = ImmutableMap.builder();
            final Map<Integer, ImmutableList.Builder<BingoGoal>> byDifficulty = new HashMap<>();
            for (final var entry : jsons.entrySet()) {
                try {
                    final JsonObject json = GsonHelper.convertToJsonObject(entry.getValue(), "bingo goal");
                    final BingoGoal goal = deserialize(entry.getKey(), json);
                    result.put(entry.getKey(), goal);
                    byDifficulty.computeIfAbsent(goal.difficulty.difficulty().number(), k -> ImmutableList.builder()).add(goal);
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
