package io.github.gaming32.bingo.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.data.icons.BlockIcon;
import io.github.gaming32.bingo.data.icons.EmptyIcon;
import io.github.gaming32.bingo.data.icons.GoalIcon;
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
    private static Map<ResourceLocation, BingoGoal> goals = Collections.emptyMap();
    private static List<? extends List<BingoGoal>> goalsByDifficulty = List.of(
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList()
    );

    private final ResourceLocation id;
    private final Map<String, BingoSub> subs;
    private final Map<String, JsonObject> criteria;
    private final AdvancementRequirements requirements;
    @Nullable
    private final String progress;
    private final List<BingoTag> tags;
    private final JsonElement name;
    @Nullable
    private final JsonElement tooltip;
    @Nullable
    private final ResourceLocation tooltipIcon;
    @Nullable
    private final JsonObject icon;
    @Nullable
    private final Integer infrequency;
    private final List<String> antisynergy;
    private final List<String> catalyst;
    private final List<String> reactant;
    private final int difficulty;

    private final Set<ResourceLocation> tagIds;
    private final BingoTag.SpecialType specialType;
    private final boolean requiredOnClient;

    public BingoGoal(
        ResourceLocation id,
        Map<String, BingoSub> subs,
        Map<String, JsonObject> criteria,
        AdvancementRequirements requirements,
        @Nullable String progress,
        List<BingoTag> tags,
        JsonElement name,
        @Nullable JsonElement tooltip,
        @Nullable ResourceLocation tooltipIcon,
        @Nullable JsonObject icon,
        @Nullable Integer infrequency,
        List<String> antisynergy,
        List<String> catalyst,
        List<String> reactant,
        int difficulty
    ) {
        this.id = id;
        this.subs = ImmutableMap.copyOf(subs);
        this.criteria = ImmutableMap.copyOf(criteria);
        this.requirements = requirements;
        this.progress = progress;
        this.tags = ImmutableList.copyOf(tags);
        this.name = name;
        this.tooltip = tooltip;
        this.tooltipIcon = tooltipIcon;
        this.icon = icon;
        this.infrequency = infrequency;
        this.antisynergy = ImmutableList.copyOf(antisynergy);
        this.catalyst = ImmutableList.copyOf(catalyst);
        this.reactant = ImmutableList.copyOf(reactant);
        this.difficulty = difficulty;

        this.tagIds = tags.stream().map(BingoTag::id).collect(ImmutableSet.toImmutableSet());

        BingoTag.SpecialType specialType = BingoTag.SpecialType.NONE;
        for (final BingoTag tag : tags) {
            if (tag.specialType() == BingoTag.SpecialType.NONE) continue;
            if (specialType == BingoTag.SpecialType.NONE) {
                specialType = tag.specialType();
                continue;
            }
            if (tag.specialType() != specialType) {
                throw new IllegalArgumentException("Inconsistent specialTypes: " + tag.specialType() + " does not match " + specialType);
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

        if (progress != null && !criteria.containsKey(progress)) {
            throw new IllegalArgumentException("Specified progress criterion '" + progress + "' does not exist");
        }
    }

    public static Set<ResourceLocation> getGoalIds() {
        return goals.keySet();
    }

    public static BingoGoal getGoal(ResourceLocation id) {
        return goals.get(id);
    }

    public static List<BingoGoal> getGoalsByDifficulty(int difficulty) {
        return goalsByDifficulty.get(difficulty);
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

        return new BingoGoal(
            id,
            GsonHelper.getAsJsonObject(json, "bingo_subs", new JsonObject())
                .entrySet()
                .stream()
                .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, e -> BingoSub.deserialize(e.getValue()))),
            criteria, requirements,
            json.has("progress") ? GsonHelper.getAsString(json, "progress") : null,
            GsonHelper.getAsJsonArray(json, "tags", new JsonArray())
                .asList()
                .stream()
                .map(e -> {
                    final ResourceLocation key = new ResourceLocation(GsonHelper.convertToString(e, "tag"));
                    final BingoTag tag = BingoTag.getTag(key);
                    if (tag == null) {
                        throw new JsonSyntaxException("Unknown bingo tag: " + key);
                    }
                    return tag;
                })
                .collect(ImmutableList.toImmutableList()),
            GsonHelper.getNonNull(json, "name"),
            json.get("tooltip"),
            json.has("tooltip_icon")
                ? new ResourceLocation(GsonHelper.getAsString(json, "tooltip_icon")) : null,
            GsonHelper.getAsJsonObject(json, "icon", null),
            json.has("infrequency") ? GsonHelper.getAsInt(json, "infrequency") : null,
            getListString(json, "antisynergy"),
            getListString(json, "catalyst"),
            getListString(json, "reactant"),
            GsonHelper.getAsInt(json, "difficulty")
        );
    }

    private static List<String> getListString(JsonObject json, String key) {
        return json.has(key) ? (
            json.get(key).isJsonArray()
                ? GsonHelper.getAsJsonArray(json, key)
                    .asList()
                    .stream()
                    .map(a -> GsonHelper.convertToString(a, key))
                    .collect(ImmutableList.toImmutableList())
                : ImmutableList.of(GsonHelper.getAsString(json, key))
        ) : ImmutableList.of();
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

        result.addProperty("progress", progress);

        if (!tags.isEmpty()) {
            final JsonArray array = new JsonArray(tags.size());
            for (final BingoTag tag : tags) {
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

        serializeListString(result, "antisynergy", antisynergy);
        serializeListString(result, "catalyst", catalyst);
        serializeListString(result, "reactant", reactant);

        result.addProperty("difficulty", difficulty);

        return result;
    }

    private static void serializeListString(JsonObject json, String key, List<String> list) {
        if (!list.isEmpty()) {
            if (list.size() == 1) {
                json.addProperty(key, list.get(0));
            } else {
                final JsonArray array = new JsonArray(list.size());
                for (final String value : list) {
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

    @Nullable
    public String getProgress() {
        return progress;
    }

    public List<BingoTag> getTags() {
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

    public List<String> getAntisynergy() {
        return antisynergy;
    }

    public List<String> getCatalyst() {
        return catalyst;
    }

    public List<String> getReactant() {
        return reactant;
    }

    public int getDifficulty() {
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
        if (referable.isEmpty()) {
            return value;
        }
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
        @Nullable
        private String progress;
        private AdvancementRequirements.Strategy requirementsStrategy = AdvancementRequirements.Strategy.AND;
        private final ImmutableList.Builder<BingoTag> tags = ImmutableList.builder();
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
        private OptionalInt difficulty;

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

        public Builder progress(String progress) {
            this.progress = progress;
            return this;
        }

        public Builder tags(ResourceLocation... tags) {
            for (ResourceLocation tag : tags) {
                this.tags.add(BingoTag.builder(tag).build());
            }
            return this;
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

        public Builder difficulty(int difficulty) {
            this.difficulty = OptionalInt.of(difficulty);
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
            final List<ImmutableList.Builder<BingoGoal>> byDifficulty = List.of(
                ImmutableList.builder(),
                ImmutableList.builder(),
                ImmutableList.builder(),
                ImmutableList.builder(),
                ImmutableList.builder()
            );
            for (final var entry : jsons.entrySet()) {
                try {
                    final JsonObject json = GsonHelper.convertToJsonObject(entry.getValue(), "bingo goal");
                    final BingoGoal goal = deserialize(entry.getKey(), json);
                    result.put(entry.getKey(), goal);
                    byDifficulty.get(goal.difficulty).add(goal);
                } catch (Exception e) {
                    Bingo.LOGGER.error("Parsing error in bingo goal {}: {}", entry.getKey(), e.getMessage());
                }
            }
            goals = result.build();
            goalsByDifficulty = byDifficulty.stream()
                .map(ImmutableList.Builder::build)
                .toList();
            Bingo.LOGGER.info("Loaded {} bingo goals", goals.size());
        }
    }
}
