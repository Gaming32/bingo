package io.github.gaming32.bingo.triggers.progress;

import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class SimpleProgressibleCriterionTrigger<T extends SimpleCriterionTrigger.SimpleInstance>
    extends SimpleCriterionTrigger<T> implements ProgressibleTrigger<T> {
    private final Map<UUID, List<ProgressListener<T>>> progressListeners = new HashMap<>();

    @Override
    public void addProgressListener(ServerPlayer player, ProgressListener<T> listener) {
        progressListeners.computeIfAbsent(player.getUUID(), k -> new ArrayList<>()).add(listener);
    }

    @Override
    public void removeProgressListener(ServerPlayer player, ProgressListener<T> listener) {
        final List<ProgressListener<T>> listeners = progressListeners.get(player.getUUID());
        if (listeners == null) return;
        listeners.remove(listener);
        if (listeners.isEmpty()) {
            progressListeners.remove(player.getUUID());
        }
    }

    @Override
    public void removeProgressListeners(ServerPlayer player) {
        progressListeners.remove(player.getUUID());
    }

    protected ProgressListener<T> getProgressListener(ServerPlayer player) {
        final List<ProgressListener<T>> listeners = progressListeners.get(player.getUUID());
        if (listeners == null) {
            return (triggerInstance, progress, maxProgress) -> {};
        }
        if (listeners.size() == 1) {
            return listeners.getFirst();
        }
        return (triggerInstance, progress, maxProgress) -> {
            for (final ProgressListener<T> listener : listeners) {
                listener.update(triggerInstance, progress, maxProgress);
            }
        };
    }
}
