package io.github.gaming32.bingo.triggers.progress;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.server.PlayerAdvancements;

public interface ProgressibleTrigger<T extends CriterionTriggerInstance> extends CriterionTrigger<T> {
    void addProgressListener(PlayerAdvancements player, ProgressListener<T> listener);

    void removeProgressListener(PlayerAdvancements player, ProgressListener<T> listener);

    void removeProgressListeners(PlayerAdvancements player);

    @FunctionalInterface
    interface ProgressListener<T extends CriterionTriggerInstance> {
        void update(T triggerInstance, int progress, int maxProgress);
    }
}
