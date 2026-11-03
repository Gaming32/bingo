package io.github.gaming32.bingo.datagen.goal;

import io.github.gaming32.bingo.data.goal.BingoGoal;
import net.minecraft.data.worldgen.BootstrapContext;

import java.util.List;

public class BingoGoalProvider {
    private static final List<GoalProviderProvider> PROVIDERS = List.of(
        VeryEasyGoalProvider::new,
        EasyGoalProvider::new,
        MediumGoalProvider::new,
        HardGoalProvider::new,
        VeryHardGoalProvider::new
    );

    public static void bootstrap(BootstrapContext<BingoGoal> context) {
        for (GoalProviderProvider provider : PROVIDERS) {
            provider.create(context).addGoals();
        }
    }

    @FunctionalInterface
    private interface GoalProviderProvider {
        DifficultyGoalProvider create(BootstrapContext<BingoGoal> context);
    }
}
