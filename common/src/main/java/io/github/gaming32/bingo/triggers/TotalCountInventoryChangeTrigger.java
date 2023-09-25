package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import io.github.gaming32.bingo.util.BingoUtil;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class TotalCountInventoryChangeTrigger extends SimpleCriterionTrigger<TotalCountInventoryChangeTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(player, BingoUtil.fromJsonElement(TriggerInstance.ITEMS_CODEC, json.get("items")));
    }

    public void trigger(ServerPlayer player, Inventory inventory) {
        trigger(player, triggerInstance -> triggerInstance.matches(inventory));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private static final Codec<List<ItemPredicate>> ITEMS_CODEC = ItemPredicate.CODEC.listOf();

        private final List<ItemPredicate> items;

        public TriggerInstance(Optional<ContextAwarePredicate> player, List<ItemPredicate> items) {
            super(player);
            this.items = items;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            result.add("items", BingoUtil.toJsonElement(ITEMS_CODEC, items));
            return result;
        }

        public boolean matches(Inventory inventory) {
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
                        if (predicate.matches(item)) {
                            counts[predicateIndex] += itemCount;
                        }
                    } finally {
                        item.setCount(itemCount);
                    }
                }
            }

            // now check the counts after they have been totaled up
            for (int predicateIndex = 0; predicateIndex < counts.length; predicateIndex++) {
                if (counts[predicateIndex] == 0) {
                    return false;
                }

                if (!items.get(predicateIndex).count().matches(counts[predicateIndex])) {
                    return false;
                }
            }

            return true;
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

        public TriggerInstance build() {
            return new TriggerInstance(player, List.copyOf(items));
        }
    }
}
