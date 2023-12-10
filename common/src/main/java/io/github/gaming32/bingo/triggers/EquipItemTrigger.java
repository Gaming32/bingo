package io.github.gaming32.bingo.triggers;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.util.BingoCodecs;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EquipItemTrigger extends SimpleCriterionTrigger<EquipItemTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ItemStack oldItem, ItemStack newItem, EquipmentSlot slot) {
        trigger(player, instance -> instance.matches(oldItem, newItem, slot));
    }

    public static Builder builder() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<ItemPredicate> oldItem,
        Optional<ItemPredicate> newItem,
        Set<EquipmentSlot> slots
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "old_item").forGetter(TriggerInstance::oldItem),
                ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "new_item").forGetter(TriggerInstance::newItem),
                ExtraCodecs.strictOptionalField(
                    BingoCodecs.enumSetOf(EquipmentSlot.CODEC), "slots", ImmutableSet.copyOf(EnumSet.allOf(EquipmentSlot.class))
                ).forGetter(TriggerInstance::slots)
            ).apply(instance, TriggerInstance::new)
        );

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
        private final Set<EquipmentSlot> slots = EnumSet.allOf(EquipmentSlot.class);

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
            return BingoTriggers.EQUIP_ITEM.get().createCriterion(
                new TriggerInstance(player, oldItem, newItem, slots)
            );
        }
    }
}
