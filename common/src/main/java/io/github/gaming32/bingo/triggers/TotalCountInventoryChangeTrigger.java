package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.event.InventoryChangedCallback;
import io.github.gaming32.bingo.triggers.progress.SimpleProgressibleCriterionTrigger;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TotalCountInventoryChangeTrigger extends SimpleProgressibleCriterionTrigger<TotalCountInventoryChangeTrigger.TriggerInstance> {
    static {
        InventoryChangedCallback.HANDLERS.add((player, inventory) -> BingoTriggers.TOTAL_COUNT_INVENTORY_CHANGED.get().trigger(player, inventory));
    }

    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Inventory inventory) {
        final ProgressListener<TriggerInstance> progressListener = getProgressListener(player);
        trigger(player, triggerInstance -> triggerInstance.matches(inventory, progressListener));
    }

    public static Builder builder() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        List<ItemPredicate> items
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                ItemPredicate.CODEC.listOf().fieldOf("items").forGetter(TriggerInstance::items)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(Inventory inventory, ProgressListener<TriggerInstance> progressListener) {
            int[] counts = new int[items.size()];

            for (int i = 0, l = inventory.getContainerSize(); i < l; i++) {
                final ItemStack item = inventory.getItem(i);
                if (item.isEmpty()) {
                    continue;
                }

                for (int predicateIndex = 0; predicateIndex < counts.length; predicateIndex++) {
                    // test if the predicate matches the item stack excluding the count
                    final ItemPredicate predicate = items.get(predicateIndex);
                    final int itemCount = item.getCount();
                    try {
                        item.setCount(predicate.count().min().orElse(1));
                        if (predicate.test(item)) {
                            counts[predicateIndex] += itemCount;
                        }
                    } finally {
                        item.setCount(itemCount);
                    }
                }
            }

            int validCount = 0;

            // now check the counts after they have been totaled up
            final Integer minCount = getMinCount();
            for (int predicateIndex = 0; predicateIndex < counts.length; predicateIndex++) {
                if (counts[predicateIndex] == 0) {
                    if (minCount == null) {
                        return false;
                    } else {
                        continue;
                    }
                }

                final MinMaxBounds.Ints countPredicate = items.get(predicateIndex).count();
                final boolean matched = countPredicate.matches(counts[predicateIndex]);
                if (minCount == null) {
                    if (!matched) {
                        return false;
                    }
                } else {
                    validCount += matched ? countPredicate.min().orElse(1) : counts[predicateIndex];
                }
            }

            if (minCount != null) {
                progressListener.update(this, validCount, minCount);
                return validCount >= minCount;
            }

            return true;
        }

        private Integer getMinCount() {
            final boolean allMin = items.stream().allMatch(p -> p.count().max().isEmpty());
            if (!allMin) {
                return null;
            }
            return items.stream().mapToInt(p -> p.count().min().orElse(1)).sum();
        }
    }

    public static final class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private final List<ItemPredicate> items = new ArrayList<>();

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.ofNullable(player);
            return this;
        }

        public Builder items(ItemPredicate... items) {
            Collections.addAll(this.items, items);
            return this;
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.TOTAL_COUNT_INVENTORY_CHANGED.get().createCriterion(
                new TriggerInstance(player, List.copyOf(items))
            );
        }
    }
}
