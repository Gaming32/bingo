package io.github.gaming32.bingo.game;

import io.github.gaming32.bingo.data.BingoGoal;
import net.minecraft.advancements.Criterion;
import net.minecraft.network.chat.Component;

import java.util.Map;

@SuppressWarnings("ClassCanBeRecord")
public final class ActiveGoal {
    private final BingoGoal goal;
    private final Component name;
    private final Component tooltip;
    private final Map<String, Criterion> criteria;

    public ActiveGoal(BingoGoal goal, Component name, Component tooltip, Map<String, Criterion> criteria) {
        this.goal = goal;
        this.name = name;
        this.tooltip = tooltip;
        this.criteria = criteria;
    }

    public BingoGoal getGoal() {
        return goal;
    }

    public Component getName() {
        return name;
    }

    public Component getTooltip() {
        return tooltip;
    }

    public Map<String, Criterion> getCriteria() {
        return criteria;
    }

    @Override
    public String toString() {
        return "ActiveGoal[" +
            "goal=" + goal + ", " +
            "name=" + name + ", " +
            "tooltip=" + tooltip + ", " +
            "criteria=" + criteria + ']';
    }

}
