package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import io.github.gaming32.bingo.subpredicates.ItemEntityPredicate;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import org.jetbrains.annotations.NotNull;

public class ItemPickedUpTrigger extends SimpleCriterionTrigger<ItemPickedUpTrigger.TriggerInstance> {
    public static final ResourceLocation ID = new ResourceLocation("bingo:item_picked_up");

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext context) {
        return new TriggerInstance(
            predicate,
            EntityPredicate.fromJson(json.get("item_entity"))
        );
    }

    public void trigger(ServerPlayer player, ItemEntity itemEntity) {
        trigger(player, instance -> instance.matches(player, itemEntity));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final EntityPredicate itemEntity;

        public TriggerInstance(ContextAwarePredicate predicate, EntityPredicate itemEntity) {
            super(ID, predicate);
            this.itemEntity = itemEntity;
        }

        public static TriggerInstance pickedUp(EntityPredicate itemEntity) {
            return new TriggerInstance(ContextAwarePredicate.ANY, itemEntity);
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
            result.add("item_entity", itemEntity.serializeToJson());
            return result;
        }

        public boolean matches(ServerPlayer player, ItemEntity itemEntity) {
            return this.itemEntity.matches(player, itemEntity);
        }
    }
}
