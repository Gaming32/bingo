package io.github.gaming32.bingo.data.goal;

import com.mojang.serialization.Dynamic;
import io.github.gaming32.bingo.data.subs.SubstitutionContext;
import io.github.gaming32.bingo.game.ActiveGoal;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.Map;
import java.util.Optional;

public record GoalHolder(ResourceLocation id, BingoGoal goal) {
    public ActiveGoal build(RandomSource rand) {
        final Map<String, Dynamic<?>> subs = goal.buildSubs(new SubstitutionContext(Map.of(), rand));
        final var context = new SubstitutionContext(subs, rand);

        final Optional<Component> tooltip = goal.buildTooltip(context);
        final MutableComponent name = goal.buildName(context);
        tooltip.ifPresent(t -> name.withStyle(s -> s
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, t))
        ));
        return new ActiveGoal(
            id, name,
            tooltip,
            goal.getTooltipIcon(),
            goal.buildIcon(context),
            goal.buildCriteria(context),
            goal.buildRequiredCount(context),
            Optional.of(goal.getDifficulty()),
            goal.getRequirements(),
            goal.getSpecialType(),
            goal.getProgress()
        );
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof GoalHolder h && id.equals(h.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
