package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.subpredicates.ItemEntityPredicate;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ItemPickedUpTrigger extends SimpleCriterionTrigger<ItemPickedUpTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ItemEntity itemEntity) {
        final LootContext itemEntityContext = EntityPredicate.createContext(player, itemEntity);
        trigger(player, instance -> instance.matches(itemEntityContext));
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<ContextAwarePredicate> itemEntity
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "item_entity").forGetter(TriggerInstance::itemEntity)
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
                .subPredicate(ItemEntityPredicate.droppedBy(Optional.ofNullable(item), Optional.ofNullable(droppedBy)))
                .build()
            );
        }

        public boolean matches(LootContext itemEntity) {
            return this.itemEntity.isEmpty() || this.itemEntity.get().matches(itemEntity);
        }

        @Override
        public void validate(CriterionValidator criterionValidator) {
            SimpleInstance.super.validate(criterionValidator);
            criterionValidator.validateEntity(itemEntity, ".item_entity");
        }
    }
}
