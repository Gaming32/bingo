package io.github.gaming32.bingo.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.data.subs.BingoSub;
import io.github.gaming32.bingo.game.ActiveGoal;
import io.github.gaming32.bingo.mixin.common.DisplayInfoAccessor;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.level.storage.loot.LootDataManager;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
    private final String[][] requirements;
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

    public BingoGoal(
        ResourceLocation id,
        Map<String, BingoSub> subs,
        Map<String, JsonObject> criteria,
        String[][] requirements,
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
        this.specialType = specialType;

        if (specialType == BingoTag.SpecialType.FINISH && requirements.length != 1) {
            throw new IllegalArgumentException("\"finish\" goals must have only ORed requirements");
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
        if (criteria.isEmpty()) {
            throw new JsonSyntaxException("Bingo goal criteria cannot be empty");
        }

        final JsonArray reqArray = GsonHelper.getAsJsonArray(json, "requirements", new JsonArray());
        String[][] requirements = new String[reqArray.size()][];

        for (int i = 0; i < requirements.length; i++) {
            final JsonArray innerArray = GsonHelper.convertToJsonArray(reqArray.get(i), "requirements[" + i + "]");
            requirements[i] = new String[innerArray.size()];
            for (int j = 0; j < innerArray.size(); j++) {
                requirements[i][j] = GsonHelper.convertToString(innerArray.get(j), "requirements[" + i + "][" + j + "]");
            }
        }

        if (requirements.length == 0) {
            requirements = new String[criteria.size()][];
            int i = 0;
            for (final String criterion : criteria.keySet()) {
                requirements[i++] = new String[] {criterion};
            }
        }

        for (final String[] innerArray : requirements) {
            if (innerArray.length == 0 && criteria.isEmpty()) {
                throw new JsonSyntaxException("Requirement entry cannot be empty");
            }
            for (final String req : innerArray) {
                if (!criteria.containsKey(req)) {
                    throw new JsonSyntaxException("Unknown required criterion '" + req + "'");
                }
            }
        }

        for (final String criterion : criteria.keySet()) {
            boolean isRequired = false;
            for (final String[] innerArray : requirements) {
                if (ArrayUtils.contains(innerArray, criterion)) {
                    isRequired = true;
                    break;
                }
            }
            if (!isRequired) {
                throw new JsonSyntaxException(
                    "Criterion '" + criterion + "' isn't a requirement for completion. " +
                        "This isn't supported behavior, all criteria must be required."
                );
            }
        }

        return new BingoGoal(
            id,
            GsonHelper.getAsJsonObject(json, "bingo_subs", new JsonObject())
                .entrySet()
                .stream()
                .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, e -> BingoSub.deserialize(e.getValue()))),
            criteria, requirements,
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
                .toList(),
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
                    .toList()
                : List.of(GsonHelper.getAsString(json, key))
        ) : List.of();
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

        boolean includeRequirements = false;
        for (final String[] subArray : requirements) {
            if (subArray.length != 1) {
                includeRequirements = true;
                break;
            }
        }
        if (includeRequirements) {
            final JsonArray reqArray = new JsonArray(requirements.length);
            for (final String[] subArray : requirements) {
                final JsonArray subJsonArray = new JsonArray(subArray.length);
                for (final String req : subArray) {
                    subJsonArray.add(req);
                }
                reqArray.add(subJsonArray);
            }
            result.add("requirements", reqArray);
        }

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

    public String[][] getRequirements() {
        return requirements;
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

    public ItemStack buildIcon(Map<String, JsonElement> referable, RandomSource rand) {
        if (icon == null) {
            return ItemStack.EMPTY;
        }
        final JsonObject iconSubbed = GsonHelper.convertToJsonObject(
            performSubstitutions(icon, referable, rand), "icon"
        );
        final ItemStack icon = DisplayInfoAccessor.invokeGetIcon(iconSubbed);
        if (iconSubbed.has("count")) {
            icon.setCount(GsonHelper.getAsInt(iconSubbed, "count"));
        }
        return icon;
    }

    public Map<String, Criterion> buildCriteria(
        Map<String, JsonElement> referable,
        RandomSource rand,
        LootDataManager lootData
    ) {
        final DeserializationContext context = new DeserializationContext(id, lootData);
        //noinspection UnstableApiUsage
        final ImmutableMap.Builder<String, Criterion> result = ImmutableMap.builderWithExpectedSize(criteria.size());
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
        private final Map<String, BingoSub> subs = new LinkedHashMap<>();
        private final Map<String, JsonObject> criteria = new LinkedHashMap<>();
        private List<List<String>> requirements = null;
        private RequirementsStrategy requirementsStrategy = RequirementsStrategy.AND;
        private final List<BingoTag> tags = new ArrayList<>();
        @Nullable
        private JsonElement name;
        @Nullable
        private JsonElement tooltip;
        @Nullable
        private ResourceLocation tooltipIcon;
        @Nullable
        private JsonObject icon;
        @Nullable
        private Integer infrequency;
        private final List<String> antisynergy = new ArrayList<>();
        private final List<String> catalyst = new ArrayList<>();
        private final List<String> reactant = new ArrayList<>();
        @Nullable
        private Integer difficulty;

        private Builder(ResourceLocation id) {
            this.id = id;
        }

        public Builder sub(String key, BingoSub sub) {
            this.subs.put(key, sub);
            return this;
        }

        public Builder criterion(String key, CriterionTriggerInstance criterion) {
            return criterion(key, criterion, subber -> {});
        }

        public Builder criterion(String key, CriterionTriggerInstance criterion, Consumer<JsonSubber> subber) {
            return criterion(key, new Criterion(criterion), subber);
        }

        public Builder criterion(String key, Criterion criterion) {
            return criterion(key, criterion, subber -> {});
        }

        public Builder criterion(String key, Criterion criterion, Consumer<JsonSubber> subber) {
            JsonSubber json = new JsonSubber(criterion.serializeToJson());
            subber.accept(json);
            this.criteria.put(key, json.json().getAsJsonObject());
            return this;
        }

        @SafeVarargs
        public final Builder requirements(List<String>... requirements) {
            this.requirements = Arrays.stream(requirements).collect(Collectors.toCollection(ArrayList::new));
            return this;
        }

        public Builder requirements(RequirementsStrategy strategy) {
            if (this.requirements != null) {
                throw new IllegalStateException("RequirementsStrategy specified after specifying explicit requirements");
            }
            this.requirementsStrategy = strategy;
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
            this.name = json.json();
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

        public Builder icon(ItemLike icon) {
            return this.icon(icon, subber -> {});
        }

        public Builder icon(ItemStack icon) {
            return this.icon(icon, subber -> {});
        }

        public Builder icon(ItemLike icon, Consumer<JsonSubber> subber) {
            return this.icon(new ItemStack(icon), subber);
        }

        public Builder icon(ItemStack icon, Consumer<JsonSubber> subber) {
            JsonObject json = new JsonObject();
            json.addProperty("item", BuiltInRegistries.ITEM.getKey(icon.getItem()).toString());
            json.addProperty("count", icon.getCount());
            if (icon.hasTag()) {
                json.addProperty("nbt", icon.getTag().toString());
            }

            JsonSubber jsonSubber = new JsonSubber(json);
            subber.accept(jsonSubber);
            this.icon = jsonSubber.json().getAsJsonObject();

            // remove count: 1
            JsonElement count = this.icon.get("count");
            if (count.isJsonPrimitive() && count.getAsJsonPrimitive().getAsInt() == 1) {
                this.icon.remove("count");
            }

            return this;
        }

        public Builder infrequency(int infrequency) {
            this.infrequency = infrequency;
            return this;
        }

        public Builder setAntisynergy(String... antisynergy) {
            this.antisynergy.clear();
            return this.antisynergy(antisynergy);
        }

        public Builder antisynergy(String... antisynergy) {
            Collections.addAll(this.antisynergy, antisynergy);
            return this;
        }

        public Builder catalyst(String... catalyst) {
            Collections.addAll(this.catalyst, catalyst);
            return this;
        }

        public Builder reactant(String... reactant) {
            Collections.addAll(this.reactant, reactant);
            return this;
        }

        public Builder difficulty(int difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public BingoGoal build() {
            if (name == null) {
                throw new IllegalStateException("Bingo goal name has not been set");
            }
            if (difficulty == null) {
                throw new IllegalStateException("Bingo goal difficulty has not been set");
            }

            return new BingoGoal(
                id,
                subs,
                criteria,
                requirements != null
                    ? requirements.stream().map(clause -> clause.toArray(String[]::new)).toArray(String[][]::new)
                    : requirementsStrategy.createRequirements(criteria.keySet()),
                tags,
                name,
                tooltip,
                tooltipIcon,
                icon,
                infrequency,
                antisynergy,
                catalyst,
                reactant,
                difficulty
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
