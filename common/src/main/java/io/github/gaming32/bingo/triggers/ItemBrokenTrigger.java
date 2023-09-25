package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemBrokenTrigger extends SimpleCriterionTrigger<ItemBrokenTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext context) {
        return new TriggerInstance(
            predicate,
            ItemPredicate.fromJson(json.get("item"))
        );
    }

    public void trigger(ServerPlayer player, ItemStack item) {
        trigger(player, instance -> instance.matches(item));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ItemPredicate item;

        public TriggerInstance(ContextAwarePredicate predicate, ItemPredicate item) {
            super(ID, predicate);
            this.item = item;
        }

        public static TriggerInstance itemBroken(ItemPredicate.Builder predicate) {
            return new TriggerInstance(ContextAwarePredicate.ANY, predicate.build());
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            final JsonObject result = super.serializeToJson(context);
            result.add("item", item.serializeToJson());
            return result;
        }

        public boolean matches(ItemStack item) {
            if (!this.item.matches(item)) {
                return false;
            }
            return true;
        }
    }
}
