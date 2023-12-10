package io.github.gaming32.bingo.triggers.progress;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.server.level.ServerPlayer;

public interface ProgressibleTrigger<T extends CriterionTriggerInstance> extends CriterionTrigger<T> {
    void addProgressListener(ServerPlayer player, ProgressListener<T> listener);

    void removeProgressListener(ServerPlayer player, ProgressListener<T> listener);

    void removeProgressListeners(ServerPlayer player);

    @FunctionalInterface
    interface ProgressListener<T extends CriterionTriggerInstance> {
        void update(T triggerInstance, int progress, int maxProgress);
    }
}
