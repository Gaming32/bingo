package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import io.github.gaming32.bingo.subpredicates.ItemEntityPredicate;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;

public class ItemPickedUpTrigger extends SimpleCriterionTrigger<ItemPickedUpTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext context) {
        return new TriggerInstance(
            predicate,
            EntityPredicate.fromJson(json, "item_entity", context)
        );
    }

    public void trigger(ServerPlayer player, ItemEntity itemEntity) {
        final LootContext itemEntityContext = EntityPredicate.createContext(player, itemEntity);
        trigger(player, instance -> instance.matches(itemEntityContext));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ContextAwarePredicate itemEntity;

        public TriggerInstance(ContextAwarePredicate predicate, ContextAwarePredicate itemEntity) {
            super(ID, predicate);
            this.itemEntity = itemEntity;
        }

        public static TriggerInstance pickedUp(EntityPredicate itemEntity) {
            return new TriggerInstance(ContextAwarePredicate.ANY, EntityPredicate.wrap(itemEntity));
        }

        public static TriggerInstance pickedUpFrom(EntityPredicate droppedBy) {
            return pickedUp(EntityPredicate.Builder.entity()
                .subPredicate(ItemEntityPredicate.droppedBy(ItemPredicate.ANY, droppedBy))
                .build()
            );
        }

        public static TriggerInstance pickedUpFrom(ItemPredicate item, EntityPredicate droppedBy) {
            return pickedUp(EntityPredicate.Builder.entity()
                .subPredicate(ItemEntityPredicate.droppedBy(item, droppedBy))
                .build()
            );
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            final JsonObject result = super.serializeToJson(context);
            result.add("item_entity", itemEntity.toJson(context));
            return result;
        }

        public boolean matches(LootContext itemEntity) {
            return this.itemEntity.matches(itemEntity);
        }
    }
}
