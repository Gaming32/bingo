package io.github.gaming32.bingo.goal;

import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

public class GoalTypes {
    private static final List<GoalType<?>> GOAL_TYPES = new ArrayList<>();

    public static GoalType<?> randomGoalType(RandomSource random) {
        return GOAL_TYPES.get(random.nextInt(GOAL_TYPES.size()));
    }

    public static BingoGoal randomGoal(RandomSource random) {
        return randomGoalType(random).generate(random);
    }

    public static GoalType<?> register(GoalType<?> type) {
        GOAL_TYPES.add(type);
        return type;
    }

    public static GoalType<?> registerSimple(BingoGoal simpleGoal) {
        return register(new SimpleGoalType(simpleGoal));
    }
}
