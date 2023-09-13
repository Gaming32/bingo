package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.gaming32.bingo.Bingo;
import io.github.gaming32.bingo.mixin.common.ItemPredicateAccessor;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TotalCountInventoryChangeTrigger extends SimpleCriterionTrigger<TotalCountInventoryChangeTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation(Bingo.MOD_ID, "total_count_inventory_changed");

    @Override
    @NotNull
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        JsonArray itemsArray = GsonHelper.getAsJsonArray(json, "items");

        ItemPredicate[] items = new ItemPredicate[itemsArray.size()];
        for (int i = 0; i < items.length; i++) {
            items[i] = ItemPredicate.fromJson(itemsArray.get(i));
        }

        return new TriggerInstance(player, items);
    }

    @Override
    @NotNull
    public ResourceLocation getId() {
        return ID;
    }

    public void trigger(ServerPlayer player, Inventory inventory) {
        trigger(player, triggerInstance -> triggerInstance.matches(inventory));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ItemPredicate[] items;

        public TriggerInstance(ContextAwarePredicate player, ItemPredicate[] items) {
            super(ID, player);
            this.items = items;
        }

        @Override
        @NotNull
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);

            JsonArray items = new JsonArray(this.items.length);
            for (ItemPredicate item : this.items) {
                items.add(item.serializeToJson());
            }
            json.add("items", items);

            return json;
        }

        public boolean matches(Inventory inventory) {
            int[] counts = new int[items.length];

            for (int i = 0, l = inventory.getContainerSize(); i < l; i++) {
                final ItemStack item = inventory.getItem(i);
                if (item.isEmpty()) {
                    continue;
                }

                for (int predicateIndex = 0; predicateIndex < counts.length; predicateIndex++) {
                    // test if the predicate matches the item stack excluding the count
                    MinMaxBounds.Ints originalCount = ((ItemPredicateAccessor) items[predicateIndex]).getCount();
                    try {
                        ((ItemPredicateAccessor) items[predicateIndex]).setCount(MinMaxBounds.Ints.exactly(item.getCount()));
                        if (items[predicateIndex].matches(item)) {
                            counts[predicateIndex] += item.getCount();
                        }
                    } finally {
                        ((ItemPredicateAccessor) items[predicateIndex]).setCount(originalCount);
                    }
                }
            }

            // now check the counts after they have been totaled up
            for (int predicateIndex = 0; predicateIndex < counts.length; predicateIndex++) {
                if (counts[predicateIndex] == 0) {
                    return false;
                }

                if (!((ItemPredicateAccessor) items[predicateIndex]).getCount().matches(counts[predicateIndex])) {
                    return false;
                }
            }

            return true;
        }
    }

    public static final class Builder {
        private ContextAwarePredicate player = ContextAwarePredicate.ANY;
        private final List<ItemPredicate> items = new ArrayList<>();

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = player;
            return this;
        }

        public Builder items(ItemPredicate... items) {
            Collections.addAll(this.items, items);
            return this;
        }

        public TriggerInstance build() {
            return new TriggerInstance(player, items.toArray(ItemPredicate[]::new));
        }
    }
}
