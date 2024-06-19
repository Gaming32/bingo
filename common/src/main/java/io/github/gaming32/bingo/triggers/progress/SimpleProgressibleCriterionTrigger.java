package io.github.gaming32.bingo.triggers.progress;

import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public abstract class SimpleProgressibleCriterionTrigger<T extends SimpleCriterionTrigger.SimpleInstance>
    extends SimpleCriterionTrigger<T> implements ProgressibleTrigger<T> {
    private final Map<PlayerAdvancements, List<ProgressListener<T>>> progressListeners = new IdentityHashMap<>();

    @Override
    public void addProgressListener(PlayerAdvancements player, ProgressListener<T> listener) {
        progressListeners.computeIfAbsent(player, k -> new ArrayList<>()).add(listener);
    }

    @Override
    public void removeProgressListener(PlayerAdvancements player, ProgressListener<T> listener) {
        final List<ProgressListener<T>> listeners = progressListeners.get(player);
        if (listeners == null) return;
        listeners.remove(listener);
        if (listeners.isEmpty()) {
            progressListeners.remove(player);
        }
    }

    @Override
    public void removeProgressListeners(PlayerAdvancements player) {
        progressListeners.remove(player);
    }

    protected ProgressListener<T> getProgressListener(ServerPlayer player) {
        final List<ProgressListener<T>> listeners = progressListeners.get(player.getAdvancements());
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
