package io.github.gaming32.bingo.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.game.ActiveGoal;
import io.github.gaming32.bingo.mixin.common.DisplayInfoAccessor;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.MinMaxBounds;
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
import net.minecraft.world.level.storage.loot.LootDataManager;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class BingoGoal {
    public static final Map<String, Function<JsonObject, BingoSub>> SUBS = Map.of(
        "sub", data -> {
            final String key = GsonHelper.getAsString(data, "id");
            return (referable, rand) -> {
                final JsonElement value = referable.get(key);
                if (value == null) {
                    throw new IllegalArgumentException("Unresolved reference in bingo goal: " + key);
                }
                return value;
            };
        },
        "random", data -> {
            final MinMaxBounds.Ints range = MinMaxBounds.Ints.fromJson(data.get("range"));
            final int min = range.getMin() != null ? range.getMin() : Integer.MIN_VALUE;
            final int max = range.getMax() != null ? range.getMax() : Integer.MAX_VALUE;
            return (referable, rand) -> new JsonPrimitive(rand.nextIntBetweenInclusive(min, max));
        }
    );

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
    private final JsonObject icon;
    @Nullable
    private final JsonElement iconText;
    private final Integer infrequency;
    private final List<String> antisynergy;
    private final List<String> catalyst;
    private final List<String> reactant;
    private final int difficulty;

    public BingoGoal(
        ResourceLocation id,
        Map<String, BingoSub> subs,
        Map<String, JsonObject> criteria,
        String[][] requirements,
        List<BingoTag> tags,
        JsonElement name,
        @Nullable JsonElement tooltip,
        @Nullable JsonObject icon,
        @Nullable JsonElement iconText,
        Integer infrequency,
        List<String> antisynergy,
        List<String> catalyst,
        List<String> reactant,
        int difficulty
    ) {
        this.id = id;
        this.subs = subs;
        this.criteria = criteria;
        this.requirements = requirements;
        this.tags = tags;
        this.name = name;
        this.tooltip = tooltip;
        this.icon = icon;
        this.iconText = iconText;
        this.infrequency = infrequency;
        this.antisynergy = antisynergy;
        this.catalyst = catalyst;
        this.reactant = reactant;
        this.difficulty = difficulty;
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
                .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, e -> {
                    final JsonObject data = GsonHelper.convertToJsonObject(e.getValue(), "bingo sub");
                    final String type = GsonHelper.getAsString(data, "type");
                    final Function<JsonObject, BingoSub> factory = SUBS.get(type);
                    if (factory == null) {
                        throw new JsonSyntaxException("Bingo sub type not found: " + type);
                    }
                    return factory.apply(data);
                })),
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
            GsonHelper.getAsJsonObject(json, "icon", null),
            json.get("icon_text"),
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
    public JsonObject getIcon() {
        return icon;
    }

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
            buildIconText(subs, rand),
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
        return Component.Serializer.fromJson(performSubstitutions(name, referable, rand));
    }

    public MutableComponent buildTooltip(Map<String, JsonElement> referable, RandomSource rand) {
        if (tooltip == null) {
            return null;
        }
        return Component.Serializer.fromJson(performSubstitutions(tooltip, referable, rand));
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

    public Component buildIconText(Map<String, JsonElement> referable, RandomSource rand) {
        if (iconText == null) {
            return null;
        }
        return Component.Serializer.fromJson(performSubstitutions(iconText, referable, rand));
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
                final String type = GsonHelper.getAsString(obj, "bingo_type");
                final Function<JsonObject, BingoSub> factory = SUBS.get(type);
                if (factory == null) {
                    throw new JsonSyntaxException("Bingo sub type not found: " + type);
                }
                return factory.apply(obj).substitute(referable, rand);
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

    @FunctionalInterface
    public interface BingoSub {
        JsonElement substitute(Map<String, JsonElement> referable, RandomSource rand);
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
