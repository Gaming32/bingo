package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.subpredicates.ItemEntityPredicate;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContextSource;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public class ItemPickedUpTrigger extends SimpleCriterionTrigger<ItemPickedUpTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ItemEntity itemEntity) {
        final LootContext itemEntityContext = EntityPredicate.createContext(player, itemEntity);
        trigger(player, instance -> instance.matches(itemEntityContext));
    }

    public record TriggerInstance(
        Optional<Holder<LootItemCondition>> player,
        Optional<Holder<LootItemCondition>> itemEntity
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                LootItemCondition.CODEC.optionalFieldOf("item_entity").forGetter(TriggerInstance::itemEntity)
            ).apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> pickedUp(EntityPredicate itemEntity) {
            return BingoTriggers.ITEM_PICKED_UP.get().createCriterion(new TriggerInstance(
                Optional.empty(), EntityPredicate.wrap(Optional.ofNullable(itemEntity))
            ));
        }

        public static Criterion<TriggerInstance> pickedUpFrom(EntityPredicate droppedBy) {
            return pickedUpFrom(null, droppedBy);
        }

        public static Criterion<TriggerInstance> pickedUpFrom(ItemPredicate item, EntityPredicate droppedBy) {
            return pickedUp(EntityPredicate.Builder.entity()
                .put(ItemEntityPredicate.CODEC, ItemEntityPredicate.droppedBy(Optional.ofNullable(item), Optional.ofNullable(droppedBy)))
                .build()
            );
        }

        public boolean matches(LootContext itemEntity) {
            return this.itemEntity.isEmpty() || this.itemEntity.get().value().test(itemEntity);
        }

        @Override
        public void validate(ValidationContextSource validator) {
            SimpleInstance.super.validate(validator);
            Validatable.validateHolder(validator.entityContext(), "item_entity", itemEntity);
        }
    }
}
