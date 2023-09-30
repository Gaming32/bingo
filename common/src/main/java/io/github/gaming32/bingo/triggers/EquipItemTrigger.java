package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.gaming32.bingo.util.BingoUtil;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EquipItemTrigger extends SimpleCriterionTrigger<EquipItemTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        final Set<EquipmentSlot> slots;
        if (json.has("slots")) {
            slots = EnumSet.noneOf(EquipmentSlot.class);
            final JsonArray slotArray = GsonHelper.getAsJsonArray(json, "slots");
            for (int i = 0; i < slotArray.size(); i++) {
                slots.add(EquipmentSlot.byName(GsonHelper.convertToString(slotArray.get(i), "slots[" + i + "]")));
            }
        } else {
            slots = EnumSet.allOf(EquipmentSlot.class);
        }
        return new TriggerInstance(
            player,
            ItemPredicate.fromJson(json.get("old_item")),
            ItemPredicate.fromJson(json.get("new_item")),
            slots
        );
    }

    public void trigger(ServerPlayer player, ItemStack oldItem, ItemStack newItem, EquipmentSlot slot) {
        trigger(player, instance -> instance.matches(oldItem, newItem, slot));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ItemPredicate> oldItem;
        private final Optional<ItemPredicate> newItem;
        private final Set<EquipmentSlot> slots;

        public TriggerInstance(
            Optional<ContextAwarePredicate> player,
            Optional<ItemPredicate> oldItem,
            Optional<ItemPredicate> newItem,
            Set<EquipmentSlot> slots
        ) {
            super(player);
            this.oldItem = oldItem;
            this.newItem = newItem;
            this.slots = slots;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            oldItem.ifPresent(p -> result.add("old_item", p.serializeToJson()));
            newItem.ifPresent(p -> result.add("new_item", p.serializeToJson()));
            if (slots.size() != EquipmentSlot.values().length) {
                result.add("slots", slots.stream()
                    .map(EquipmentSlot::getName)
                    .map(JsonPrimitive::new)
                    .collect(BingoUtil.toJsonArray())
                );
            }
            return result;
        }

        public boolean matches(ItemStack oldItem, ItemStack newItem, EquipmentSlot slot) {
            if (this.oldItem.isPresent() && !this.oldItem.get().matches(oldItem)) {
                return false;
            }
            if (this.newItem.isPresent() && !this.newItem.get().matches(newItem)) {
                return false;
            }
            if (!this.slots.contains(slot)) {
                return false;
            }
            return true;
        }
    }

    public static final class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private Optional<ItemPredicate> oldItem = Optional.empty();
        private Optional<ItemPredicate> newItem = Optional.empty();
        private Set<EquipmentSlot> slots = EnumSet.allOf(EquipmentSlot.class);

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.ofNullable(player);
            return this;
        }

        public Builder oldItem(ItemPredicate item) {
            this.oldItem = Optional.ofNullable(item);
            return this;
        }

        public Builder newItem(ItemPredicate item) {
            this.newItem = Optional.ofNullable(item);
            return this;
        }

        public Builder slots(EquipmentSlot... slots) {
            this.slots.clear();
            Collections.addAll(this.slots, slots);
            return this;
        }

        public Builder slots(EquipmentSlot.Type type) {
            slots.clear();
            Arrays.stream(EquipmentSlot.values()).filter(s -> s.getType() == type).forEach(slots::add);
            return this;
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.EQUIP_ITEM.createCriterion(
                new TriggerInstance(player, oldItem, newItem, slots)
            );
        }
    }
}
