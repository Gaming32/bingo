package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import io.github.gaming32.bingo.subpredicates.ItemEntityPredicate;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ItemPickedUpTrigger extends SimpleCriterionTrigger<ItemPickedUpTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(player, EntityPredicate.fromJson(json, "item_entity", context));
    }

    public void trigger(ServerPlayer player, ItemEntity itemEntity) {
        final LootContext itemEntityContext = EntityPredicate.createContext(player, itemEntity);
        trigger(player, instance -> instance.matches(itemEntityContext));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ContextAwarePredicate> itemEntity;

        public TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> itemEntity) {
            super(player);
            this.itemEntity = itemEntity;
        }

        public static Criterion<TriggerInstance> pickedUp(EntityPredicate itemEntity) {
            return BingoTriggers.ITEM_PICKED_UP.createCriterion(new TriggerInstance(
                Optional.empty(), EntityPredicate.wrap(Optional.ofNullable(itemEntity))
            ));
        }

        public static Criterion<TriggerInstance> pickedUpFrom(EntityPredicate droppedBy) {
            return pickedUpFrom(null, droppedBy);
        }

        public static Criterion<TriggerInstance> pickedUpFrom(ItemPredicate item, EntityPredicate droppedBy) {
            return pickedUp(EntityPredicate.Builder.entity()
                .subPredicate(ItemEntityPredicate.droppedBy(Optional.ofNullable(item), Optional.ofNullable(droppedBy)))
                .build()
            );
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            itemEntity.ifPresent(p -> result.add("item_entity", p.toJson()));
            return result;
        }

        public boolean matches(LootContext itemEntity) {
            return this.itemEntity.isEmpty() || this.itemEntity.get().matches(itemEntity);
        }
    }
}
