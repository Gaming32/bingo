package io.github.gaming32.bingo.data.goal;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.data.BingoDifficulty;
import io.github.gaming32.bingo.data.BingoRegistries;
import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.data.icons.EmptyIcon;
import io.github.gaming32.bingo.data.icons.GoalIcon;
import io.github.gaming32.bingo.data.progresstrackers.EmptyProgressTracker;
import io.github.gaming32.bingo.data.progresstrackers.ProgressTracker;
import io.github.gaming32.bingo.data.subs.BingoSub;
import io.github.gaming32.bingo.data.subs.ParsedOrSub;
import io.github.gaming32.bingo.data.subs.SubstitutionContext;
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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

public class BingoGoal {
    public static final Codec<BingoGoal> CODEC = RecordCodecBuilder.<BingoGoal>create(
        instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, BingoSub.CODEC).optionalFieldOf("bingo_subs", Map.of()).forGetter(BingoGoal::getSubs),
            Codec.unboundedMap(Codec.STRING, ParsedOrSub.codec(Criterion.CODEC)).fieldOf("criteria").forGetter(BingoGoal::getCriteria),
            AdvancementRequirements.CODEC.optionalFieldOf("requirements").forGetter(g -> Optional.of(g.requirements)),
            ProgressTracker.CODEC.optionalFieldOf("progress", EmptyProgressTracker.INSTANCE).forGetter(BingoGoal::getProgress),
            ParsedOrSub.optionalCodec(ExtraCodecs.POSITIVE_INT, "required_count", 1).forGetter(BingoGoal::getRequiredCount),
            RegistryCodecs.homogeneousList(BingoRegistries.TAG).optionalFieldOf("tags", HolderSet.empty()).forGetter(BingoGoal::getTags),
            ParsedOrSub.codec(ComponentSerialization.CODEC).fieldOf("name").forGetter(BingoGoal::getName),
            ParsedOrSub.codec(ComponentSerialization.CODEC).optionalFieldOf("tooltip").forGetter(BingoGoal::getTooltip),
            ResourceLocation.CODEC.optionalFieldOf("tooltip_icon").forGetter(BingoGoal::getTooltipIcon),
            ParsedOrSub.optionalCodec(GoalIcon.CODEC, "icon", EmptyIcon.INSTANCE).forGetter(BingoGoal::getIcon),
            BingoCodecs.optionalPositiveInt("infrequency").forGetter(BingoGoal::getInfrequency),
            BingoCodecs.minifiedSetField(Codec.STRING, "antisynergy").forGetter(BingoGoal::getAntisynergy),
            BingoCodecs.minifiedSetField(Codec.STRING, "catalyst").forGetter(BingoGoal::getCatalyst),
            BingoCodecs.minifiedSetField(Codec.STRING, "reactant").forGetter(BingoGoal::getReactant),
            RegistryFixedCodec.create(BingoRegistries.DIFFICULTY).fieldOf("difficulty").forGetter(BingoGoal::getDifficulty),
            RegistryOps.retrieveGetter(Registries.TRIGGER_TYPE)
        ).apply(instance, BingoGoal::new)
    ).validate(BingoGoal::validate);

    private final Map<String, BingoSub> subs;
    private final Map<String, ParsedOrSub<Criterion<?>>> criteria;
    private final AdvancementRequirements requirements;
    private final ProgressTracker progress;
    private final ParsedOrSub<Integer> requiredCount;
    private final HolderSet<BingoTag> tags;
    private final ParsedOrSub<Component> name;
    private final Optional<ParsedOrSub<Component>> tooltip;
    private final Optional<ResourceLocation> tooltipIcon;
    private final ParsedOrSub<GoalIcon> icon;
    private final OptionalInt infrequency;
    private final Set<String> antisynergy;
    private final Set<String> catalyst;
    private final Set<String> reactant;
    private final Holder<BingoDifficulty> difficulty;

    private final BingoTag.SpecialType specialType;
    private final boolean requiredOnClient;

    public BingoGoal(
        Map<String, BingoSub> subs,
        Map<String, ParsedOrSub<Criterion<?>>> criteria,
        Optional<AdvancementRequirements> requirements,
        ProgressTracker progress,
        ParsedOrSub<Integer> requiredCount,
        HolderSet<BingoTag> tags,
        ParsedOrSub<Component> name,
        Optional<ParsedOrSub<Component>> tooltip,
        Optional<ResourceLocation> tooltipIcon,
        ParsedOrSub<GoalIcon> icon,
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
        for (final var criterion : criteria.values()) {
            final var triggerKey = criterion
                .serialized()
                .get("trigger")
                .read(triggerCodec)
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
        var result = DataResult.success(this);

        final var availableSubs = LinkedHashSet.<String>newLinkedHashSet(subs.size());
        var substitutionContext = SubstitutionContext.createValidationContext(availableSubs);
        for (final var sub : subs.entrySet()) {
            result = BingoUtil.combineError(result, sub.getValue().validate(substitutionContext));
            availableSubs.add(sub.getKey());
        }

        final var triggerCodec = ResourceKey.codec(Registries.TRIGGER_TYPE);
        for (final var criterion : criteria.values()) {
            result = BingoUtil.combineError(result, criterion.serialized().get("trigger").read(triggerCodec));
            result = BingoUtil.combineError(result, criterion.validate(substitutionContext));
        }

        result = BingoUtil.combineError(result, requirements.validate(criteria.keySet()));
        result = BingoUtil.combineError(result, progress.validate(this));
        result = BingoUtil.combineError(result, requiredCount.validate(substitutionContext));

        for (final var tag : tags) {
            final BingoTag.SpecialType type = tag.value().specialType();
            if (type != BingoTag.SpecialType.NONE && type != specialType) {
                result = BingoUtil.combineError(result, () -> "Inconsistent specialTypes: " + type + " does not match " + specialType);
            }
        }

        result = BingoUtil.combineError(result, name.validate(substitutionContext));

        if (tooltip.isPresent()) {
            result = BingoUtil.combineError(result, tooltip.get().validate(substitutionContext));
        }

        result = BingoUtil.combineError(result, icon.validate(substitutionContext));

        if (specialType == BingoTag.SpecialType.FINISH && requirements.size() != 1) {
            result = BingoUtil.combineError(result, () -> "\"finish\" goals must have only ORed requirements");
        }

        return result;
    }

    public Map<String, BingoSub> getSubs() {
        return subs;
    }

    public Map<String, ParsedOrSub<Criterion<?>>> getCriteria() {
        return criteria;
    }

    public AdvancementRequirements getRequirements() {
        return requirements;
    }

    public ProgressTracker getProgress() {
        return progress;
    }

    public ParsedOrSub<Integer> getRequiredCount() {
        return requiredCount;
    }

    public HolderSet<BingoTag> getTags() {
        return tags;
    }

    public ParsedOrSub<Component> getName() {
        return name;
    }

    public Optional<ParsedOrSub<Component>> getTooltip() {
        return tooltip;
    }

    public Optional<ResourceLocation> getTooltipIcon() {
        return tooltipIcon;
    }

    public ParsedOrSub<GoalIcon> getIcon() {
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

    public SubstitutionContext buildSubstitutionContext(RandomSource rand) {
        final var result = new SubstitutionContext(LinkedHashMap.newLinkedHashMap(subs.size()), rand);
        for (final var entry : subs.entrySet()) {
            result.referable().put(entry.getKey(), entry.getValue().substitute(result));
        }
        return result.harden();
    }

    public MutableComponent buildName(SubstitutionContext context) {
        return BingoUtil.ensureHasFallback(name.substituteOrThrow(context).copy());
    }

    public Optional<Component> buildTooltip(SubstitutionContext context) {
        return tooltip.map(t -> BingoUtil.ensureHasFallback(t.substituteOrThrow(context).copy()));
    }

    public GoalIcon buildIcon(SubstitutionContext context) {
        return icon.substituteOrThrow(context);
    }

    public Map<String, Criterion<?>> buildCriteria(SubstitutionContext context) {
        final ImmutableMap.Builder<String, Criterion<?>> result = ImmutableMap.builderWithExpectedSize(criteria.size());
        for (final var entry : criteria.entrySet()) {
            result.put(entry.getKey(), entry.getValue().substituteOrThrow(context));
        }
        return result.build();
    }

    public int buildRequiredCount(SubstitutionContext context) {
        return requiredCount.substituteOrThrow(context);
    }

    public static GoalBuilder builder(ResourceLocation id) {
        return new GoalBuilder(id);
    }
}
