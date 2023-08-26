package io.github.gaming32.bingo.goal;

import net.minecraft.util.RandomSource;

public interface GoalType<G extends BingoGoal> {
    G generate(RandomSource random);
}
