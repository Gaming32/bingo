package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ConsumeMilkBucketTrigger extends SimpleCriterionTrigger<ConsumeMilkBucketTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(player, ItemPredicate.fromJson(json.get("item")));
    }

    public void trigger(ServerPlayer player, ItemStack item) {
        trigger(player, instance -> instance.matches(item));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ItemPredicate> item;

        public TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item) {
            super(player);
            this.item = item;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            item.ifPresent(p -> result.add("item", p.serializeToJson()));
            return result;
        }

        public boolean matches(ItemStack item) {
            return this.item.isEmpty() || this.item.get().matches(item);
        }
    }
}
