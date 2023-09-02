package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.gaming32.bingo.util.Util;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;

public class EquipItemTrigger extends SimpleCriterionTrigger<EquipItemTrigger.TriggerInstance> {
    public static final ResourceLocation ID = new ResourceLocation("bingo:equip_item");

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext context) {
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
            predicate,
            ItemPredicate.fromJson(json.get("old_item")),
            ItemPredicate.fromJson(json.get("new_item")),
            slots
        );
    }

    public void trigger(ServerPlayer player, ItemStack oldItem, ItemStack newItem, EquipmentSlot slot) {
        trigger(player, instance -> instance.matches(oldItem, newItem, slot));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ItemPredicate oldItem;
        private final ItemPredicate newItem;
        private final Set<EquipmentSlot> slots;

        public TriggerInstance(ContextAwarePredicate predicate, ItemPredicate oldItem, ItemPredicate newItem, Set<EquipmentSlot> slots) {
            super(ID, predicate);
            this.oldItem = oldItem;
            this.newItem = newItem;
            this.slots = slots;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            final JsonObject result = super.serializeToJson(context);
            result.add("old_item", oldItem.serializeToJson());
            result.add("new_item", newItem.serializeToJson());
            result.add(
                "slots", slots.size() == EquipmentSlot.values().length ? JsonNull.INSTANCE :
                    slots.stream()
                        .map(EquipmentSlot::getName)
                        .map(JsonPrimitive::new)
                        .collect(Util.toJsonArray())
            );
            return result;
        }

        public boolean matches(ItemStack oldItem, ItemStack newItem, EquipmentSlot slot) {
            if (!this.oldItem.matches(oldItem)) {
                return false;
            }
            if (!this.newItem.matches(newItem)) {
                return false;
            }
            if (!this.slots.contains(slot)) {
                return false;
            }
            return true;
        }
    }
}
