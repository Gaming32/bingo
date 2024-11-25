package io.github.gaming32.bingo.data.goal;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.data.BingoDifficulty;
import io.github.gaming32.bingo.data.BingoRegistries;
import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.data.icons.EmptyIcon;
import io.github.gaming32.bingo.data.icons.GoalIcon;
import io.github.gaming32.bingo.data.progresstrackers.EmptyProgressTracker;
import io.github.gaming32.bingo.data.progresstrackers.ProgressTracker;
import io.github.gaming32.bingo.data.subs.BingoSub;
import io.github.gaming32.bingo.util.BingoCodecs;
import io.github.gaming32.bingo.util.BingoUtil;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

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
            GoalSubstitutionSystem.performSubstitutions(name, referable, rand)
        ).copy());
    }

    public Optional<Component> buildTooltip(Map<String, Dynamic<?>> referable, RandomSource rand) {
        if (tooltip.getValue() == tooltip.getOps().empty()) {
            return Optional.empty();
        }
        return Optional.of(
            BingoUtil.ensureHasFallback(BingoUtil.fromDynamic(
                ComponentSerialization.CODEC,
                GoalSubstitutionSystem.performSubstitutions(tooltip, referable, rand)
            ).copy())
        );
    }

    public GoalIcon buildIcon(Map<String, Dynamic<?>> referable, RandomSource rand) {
        if (icon.getValue() == icon.getOps().empty()) {
            return EmptyIcon.INSTANCE;
        }
        return BingoUtil.fromDynamic(GoalIcon.CODEC, GoalSubstitutionSystem.performSubstitutions(icon, referable, rand));
    }

    public Map<String, Criterion<?>> buildCriteria(Map<String, Dynamic<?>> referable, RandomSource rand) {
        final ImmutableMap.Builder<String, Criterion<?>> result = ImmutableMap.builderWithExpectedSize(criteria.size());
        for (final var entry : criteria.entrySet()) {
            result.put(entry.getKey(), BingoUtil.fromDynamic(Criterion.CODEC, GoalSubstitutionSystem.performSubstitutions(entry.getValue(), referable, rand)));
        }
        return result.build();
    }

    public int buildRequiredCount(Map<String, Dynamic<?>> referable, RandomSource rand) {
        return BingoUtil.fromDynamic(ExtraCodecs.POSITIVE_INT, GoalSubstitutionSystem.performSubstitutions(requiredCount, referable, rand));
    }

    public static GoalBuilder builder(ResourceLocation id) {
        return new GoalBuilder(id);
    }
}
