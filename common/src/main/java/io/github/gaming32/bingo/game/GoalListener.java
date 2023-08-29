package io.github.gaming32.bingo.game;

import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.mixin.common.PlayerAdvancementsAccessor;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.server.PlayerAdvancements;

public class GoalListener<T extends CriterionTriggerInstance> extends CriterionTrigger.Listener<T> {
    private final T trigger;
    private final ActiveGoal goal;
    private final String criterion;

    public GoalListener(T trigger, ActiveGoal goal, String criterion) {
        super(trigger, null, criterion);
        this.trigger = trigger;
        this.goal = goal;
        this.criterion = criterion;
    }

    @Override
    public void run(PlayerAdvancements playerAdvancements) {
        if (Bingo.activeGame == null) {
            Bingo.LOGGER.error("Didn't unregister goal listeners when game ended?");
            return;
        }
        Bingo.activeGame.award(((PlayerAdvancementsAccessor)playerAdvancements).getPlayer(), goal, criterion);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object != null && this.getClass() == object.getClass()) {
            final GoalListener<?> listener = (GoalListener<?>)object;
            return trigger.equals(listener.trigger) && goal.equals(listener.goal) && criterion.equals(listener.criterion);
        }
        return false;
    }

    public int hashCode() {
        int h = trigger.hashCode();
        h = 31 * h + goal.hashCode();
        return 31 * h + criterion.hashCode();
    }
}
