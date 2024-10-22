package io.github.gaming32.bingo.game;

import com.google.common.collect.Multimap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.icons.GoalIcon;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemLore;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public record ActiveGoal(
    BingoGoal.GoalHolder goal,
    Component name,
    Optional<Component> tooltip,
    GoalIcon icon,
    Map<String, Criterion<?>> criteria,
    int requiredCount
) {
    public static final Codec<ActiveGoal> PERSISTENCE_CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            BingoGoal.GoalHolder.PERSISTENCE_CODEC.fieldOf("goal").forGetter(ActiveGoal::goal),
            ComponentSerialization.CODEC.fieldOf("name").forGetter(ActiveGoal::name),
            ComponentSerialization.CODEC.optionalFieldOf("tooltip").forGetter(ActiveGoal::tooltip),
            GoalIcon.CODEC.fieldOf("icon").forGetter(ActiveGoal::icon),
            Codec.unboundedMap(Codec.STRING, Criterion.CODEC).fieldOf("criteria").forGetter(ActiveGoal::criteria),
            Codec.INT.fieldOf("required_count").forGetter(ActiveGoal::requiredCount)
        ).apply(instance, ActiveGoal::new)
    );

    public boolean hasProgress() {
        return goal.goal().getProgress() != null;
    }

    public ItemStack getFallbackWithComponents(RegistryAccess access) {
        final ItemStack result = icon.getFallback(access);
        result.set(DataComponents.ITEM_NAME, name);
        result.set(DataComponents.RARITY, Rarity.COMMON);
        result.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        if (result.getCount() > result.getMaxStackSize()) {
            result.set(DataComponents.MAX_STACK_SIZE, Math.min(result.getCount(), Item.ABSOLUTE_MAX_STACK_SIZE));
        }
        tooltip.ifPresent(component -> result.set(DataComponents.LORE, new ItemLore(List.of(component))));
        result.set(DataComponents.ATTRIBUTE_MODIFIERS, new ItemAttributeModifiers(List.of(), false));
        return result;
    }

    public void validateAndLog(HolderGetter.Provider lootData) {
        final ProblemReporter.Collector collector = new ProblemReporter.Collector();
        validate(collector, lootData);
        final Multimap<String, String> errors = collector.get();
        if (!errors.isEmpty()) {
            final String message = errors.asMap()
                .entrySet()
                .stream()
                .map(entry -> "  at " + entry.getKey() + ": " + String.join("; ", entry.getValue()))
                .collect(Collectors.joining("\n"));
            Bingo.LOGGER.warn("Found validation problems in goal {}:\n{}", goal.id(), message);
        }
    }

    public void validate(ProblemReporter reporter, HolderGetter.Provider lootData) {
        criteria.forEach((key, criterion) -> {
            final CriterionValidator validator = new CriterionValidator(reporter.forChild(key), lootData);
            criterion.triggerInstance().validate(validator);
        });
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ActiveGoal g && goal.equals(g.goal);
    }

    @Override
    public int hashCode() {
        return goal.hashCode();
    }
}
