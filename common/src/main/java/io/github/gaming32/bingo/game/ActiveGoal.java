package io.github.gaming32.bingo.game;

import io.github.gaming32.bingo.data.BingoGoal;
import io.github.gaming32.bingo.data.icons.GoalIcon;
import net.minecraft.advancements.Criterion;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;

public record ActiveGoal(
    BingoGoal.Holder goal,
    Component name,
    Optional<Component> tooltip,
    GoalIcon icon,
    Map<String, Criterion<?>> criteria
) {
    public boolean hasProgress() {
        return goal.goal().getProgress() != null;
    }

    public ItemStack toSingleStack() {
        final ItemStack result = icon.item().copy();
        result.setHoverName(name);
        if (tooltip.isPresent()) {
            final ListTag lore = new ListTag();
            lore.add(StringTag.valueOf(Component.Serializer.toJson(tooltip.get())));
            result.getOrCreateTagElement("display").put("Lore", lore);
        }
        return result;
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
