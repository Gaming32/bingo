package io.github.gaming32.bingo.data.goal;

import com.demonwav.mcdev.annotations.Translatable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.github.gaming32.bingo.data.BingoDifficulty;
import io.github.gaming32.bingo.data.BingoRegistries;
import io.github.gaming32.bingo.data.BingoTag;
import io.github.gaming32.bingo.data.JsonSubber;
import io.github.gaming32.bingo.data.icons.BlockIcon;
import io.github.gaming32.bingo.data.icons.GoalIcon;
import io.github.gaming32.bingo.data.progresstrackers.CriterionProgressTracker;
import io.github.gaming32.bingo.data.progresstrackers.EmptyProgressTracker;
import io.github.gaming32.bingo.data.progresstrackers.ProgressTracker;
import io.github.gaming32.bingo.data.subs.BingoSub;
import io.github.gaming32.bingo.util.BingoCodecs;
import io.github.gaming32.bingo.util.BingoUtil;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;

public final class GoalBuilder {
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

    GoalBuilder(ResourceLocation id) {
        this.id = id;
    }

    public GoalBuilder sub(String key, BingoSub sub) {
        this.subs.put(key, sub);
        return this;
    }

    public GoalBuilder criterion(String key, Criterion<?> criterion) {
        return criterion(key, criterion, subber -> {
        });
    }

    public GoalBuilder criterion(String key, Criterion<?> criterion, Consumer<JsonSubber> subber) {
        JsonSubber json = new JsonSubber(Criterion.CODEC.encodeStart(JSON_OPS.get(), criterion).getOrThrow());
        subber.accept(json);
        this.criteria.put(key, new Dynamic<>(JsonOps.INSTANCE, json.json()));
        return this;
    }

    public GoalBuilder requirements(AdvancementRequirements requirements) {
        this.requirements = Optional.of(requirements);
        return this;
    }

    public GoalBuilder requirements(AdvancementRequirements.Strategy strategy) {
        this.requirementsStrategy = strategy;
        return this;
    }

    public GoalBuilder progress(ProgressTracker progress) {
        this.progress = progress;
        return this;
    }

    public GoalBuilder progress(String criterion) {
        return progress(CriterionProgressTracker.unscaled(criterion));
    }

    public GoalBuilder requiredCount(int requiredCount) {
        this.requiredCount = BingoCodecs.EMPTY_DYNAMIC.createInt(requiredCount);
        return this;
    }

    public GoalBuilder requiredCount(BingoSub requiredCountSub) {
        this.requiredCount = BingoUtil.toDynamic(BingoSub.INNER_CODEC, requiredCountSub);
        return this;
    }

    @SafeVarargs
    public final GoalBuilder tags(ResourceKey<BingoTag>... tags) {
        this.tags.add(tags);
        return this;
    }

    public GoalBuilder name(@Translatable(prefix = "bingo.goal.") String name) {
        return this.name(Component.translatable("bingo.goal." + name));
    }

    public GoalBuilder name(Component name) {
        return this.name(name, subber -> {
        });
    }

    public GoalBuilder name(Component name, Consumer<JsonSubber> subber) {
        JsonSubber json = new JsonSubber(ComponentSerialization.CODEC.encodeStart(JSON_OPS.get(), name).getOrThrow());
        subber.accept(json);
        this.name = Optional.of(new Dynamic<>(JsonOps.INSTANCE, json.json()));
        return this;
    }

    public GoalBuilder tooltip(@Translatable(prefix = "bingo.goal.", suffix = ".tooltip") String tooltip) {
        return this.tooltip(Component.translatable("bingo.goal." + tooltip + ".tooltip"));
    }

    public GoalBuilder tooltip(Component tooltip) {
        return this.tooltip(tooltip, subber -> {
        });
    }

    public GoalBuilder tooltip(Component tooltip, Consumer<JsonSubber> subber) {
        JsonSubber json = new JsonSubber(ComponentSerialization.CODEC.encodeStart(JSON_OPS.get(), tooltip).getOrThrow());
        subber.accept(json);
        this.tooltip = new Dynamic<>(JsonOps.INSTANCE, json.json());
        return this;
    }

    public GoalBuilder tooltipIcon(ResourceLocation tooltipIcon) {
        this.tooltipIcon = Optional.of(tooltipIcon);
        return this;
    }

    public GoalBuilder icon(Object icon) {
        return icon(icon, subber -> {
        });
    }

    public GoalBuilder icon(Object icon, Consumer<JsonSubber> subber) {
        return icon(GoalIcon.infer(icon), subber);
    }

    public GoalBuilder icon(Block icon, ItemLike fallback) {
        return icon(icon, fallback, subber -> {
        });
    }

    public GoalBuilder icon(Block icon, ItemLike fallback, Consumer<JsonSubber> subber) {
        return icon(BlockIcon.ofBlockAndItem(icon, fallback), subber);
    }

    public GoalBuilder icon(GoalIcon icon) {
        return icon(icon, subber -> {
        });
    }

    public GoalBuilder icon(GoalIcon icon, Consumer<JsonSubber> subber) {
        JsonSubber jsonSubber = new JsonSubber(GoalIcon.CODEC.encodeStart(JSON_OPS.get(), icon).getOrThrow());
        subber.accept(jsonSubber);
        this.icon = new Dynamic<>(JsonOps.INSTANCE, jsonSubber.json());
        return this;
    }

    public GoalBuilder infrequency(int infrequency) {
        this.infrequency = OptionalInt.of(infrequency);
        return this;
    }

    public GoalBuilder setAntisynergy(String... antisynergy) {
        this.antisynergy = ImmutableSet.builderWithExpectedSize(antisynergy.length);
        return this.antisynergy(antisynergy);
    }

    public GoalBuilder antisynergy(String... antisynergy) {
        this.antisynergy.add(antisynergy);
        return this;
    }

    public GoalBuilder catalyst(String... catalyst) {
        this.catalyst.add(catalyst);
        return this;
    }

    public GoalBuilder reactant(String... reactant) {
        this.reactant.add(reactant);
        return this;
    }

    public GoalBuilder difficulty(ResourceKey<BingoDifficulty> difficulty) {
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
