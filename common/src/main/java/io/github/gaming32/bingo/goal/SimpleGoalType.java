package io.github.gaming32.bingo.goal;

import net.minecraft.util.RandomSource;

public class SimpleGoalType<G extends BingoGoal> implements GoalType<G> {
    private final G goal;

    public SimpleGoalType(G goal) {
        this.goal = goal;
    }

    @Override
    public G generate(RandomSource random) {
        return goal;
    }
}
