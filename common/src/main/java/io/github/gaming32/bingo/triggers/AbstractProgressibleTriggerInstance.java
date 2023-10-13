package io.github.gaming32.bingo.triggers;

import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class AbstractProgressibleTriggerInstance extends AbstractCriterionTriggerInstance {
    private final Map<UUID, List<ProgressListener>> progressListeners = new HashMap<>();

    public AbstractProgressibleTriggerInstance(Optional<ContextAwarePredicate> player) {
        super(player);
    }

    public void addProgressListener(ServerPlayer player, ProgressListener listener) {
        progressListeners.computeIfAbsent(player.getUUID(), k -> new ArrayList<>()).add(listener);
    }

    public void removeProgressListener(ServerPlayer player, ProgressListener listener) {
        List<ProgressListener> listeners = progressListeners.get(player.getUUID());
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    protected void setProgress(ServerPlayer player, int progress, int maxProgress) {
        List<ProgressListener> listeners = progressListeners.get(player.getUUID());
        for (ProgressListener listener : listeners) {
            listener.update(player, progress, maxProgress);
        }
    }

    @FunctionalInterface
    public interface ProgressListener {
        void update(ServerPlayer player, int progress, int maxProgress);
    }
}
