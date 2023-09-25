package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ConsumeMilkBucketTrigger extends SimpleCriterionTrigger<ConsumeMilkBucketTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        return new TriggerInstance(player, ItemPredicate.fromJson(json.get("item")));
    }

    public void trigger(ServerPlayer player, ItemStack item) {
        trigger(player, instance -> instance.matches(item));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ItemPredicate item;

        public TriggerInstance(ContextAwarePredicate player, ItemPredicate item) {
            super(ID, player);
            this.item = item;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            final JsonObject result = super.serializeToJson(context);
            result.add("item", item.serializeToJson());
            return result;
        }

        public boolean matches(ItemStack item) {
            return this.item.matches(item);
        }
    }
}
