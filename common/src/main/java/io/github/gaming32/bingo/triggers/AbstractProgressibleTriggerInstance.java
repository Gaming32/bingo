package io.github.gaming32.bingo.triggers;

import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractProgressibleTriggerInstance extends AbstractCriterionTriggerInstance {
    private final List<ProgressListener> progressListeners = new ArrayList<>();

    public AbstractProgressibleTriggerInstance(Optional<ContextAwarePredicate> player) {
        super(player);
    }

    public void addProgressListener(ProgressListener listener) {
        progressListeners.add(listener);
    }

    public void removeProgressListener(ProgressListener listener) {
        progressListeners.remove(listener);
    }

    protected void setProgress(int progress, int maxProgress) {
        for (ProgressListener listener : progressListeners) {
            listener.update(progress, maxProgress);
        }
    }

    @FunctionalInterface
    public interface ProgressListener {
        void update(int progress, int maxProgress);
    }
}
