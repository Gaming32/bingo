package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.conditions.BingoContextKeySets;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.LocationPredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContextSource;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

import java.util.Optional;

public class UseGrindstoneTrigger extends SimpleCriterionTrigger<UseGrindstoneTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, BlockPos grindstonePos, ItemStack firstItem, ItemStack secondItem) {
        double x = grindstonePos.getX() + 0.5;
        double y = grindstonePos.getY() + 0.5;
        double z = grindstonePos.getZ() + 0.5;
        trigger(player, instance -> instance.matches(player, x, y, z, firstItem, secondItem));
    }

    public static Builder builder() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<Holder<LootItemCondition>> player,
        Optional<LocationPredicate> location,
        Optional<Holder<LootItemCondition>> firstItem,
        Optional<Holder<LootItemCondition>> secondItem
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                LocationPredicate.CODEC.optionalFieldOf("location").forGetter(TriggerInstance::location),
                LootItemCondition.CODEC.optionalFieldOf("first_item").forGetter(TriggerInstance::firstItem),
                LootItemCondition.CODEC.optionalFieldOf("second_item").forGetter(TriggerInstance::secondItem)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(ServerPlayer player, double x, double y, double z, ItemStack firstItem, ItemStack secondItem) {
            if (location.isPresent() && !location.get().matches(player.level(), x, y, z)) {
                return false;
            }
            if (this.firstItem.isPresent() && !this.firstItem.get().value().test(BingoContextKeySets.wrapTool(player, firstItem))) {
                return false;
            }
            if (this.secondItem.isPresent() && !this.secondItem.get().value().test(BingoContextKeySets.wrapTool(player, secondItem))) {
                return false;
            }
            return true;
        }

        @Override
        public void validate(ValidationContextSource validator) {
            SimpleInstance.super.validate(validator);
            firstItem.ifPresent(p -> Validatable.validateHolder(validator.context(BingoContextKeySets.TOOL_ONLY), "first_item", p));
            secondItem.ifPresent(p -> Validatable.validateHolder(validator.context(BingoContextKeySets.TOOL_ONLY), "second_item", p));
        }
    }

    public static final class Builder {
        private Optional<Holder<LootItemCondition>> player = Optional.empty();
        private Optional<LocationPredicate> location = Optional.empty();
        private Optional<Holder<LootItemCondition>> firstItem = Optional.empty();
        private Optional<Holder<LootItemCondition>> secondItem = Optional.empty();

        private Builder() {
        }

        public Builder player(Holder<LootItemCondition> player) {
            this.player = Optional.of(player);
            return this;
        }

        public Builder location(LocationPredicate location) {
            this.location = Optional.of(location);
            return this;
        }

        public Builder firstItem(Holder<LootItemCondition> firstItem) {
            this.firstItem = Optional.of(firstItem);
            return this;
        }

        public Builder firstItem(ItemPredicate firstItem) {
            return firstItem(Holder.direct(new MatchTool(Optional.of(firstItem))));
        }

        public Builder secondItem(Holder<LootItemCondition> secondItem) {
            this.secondItem = Optional.of(secondItem);
            return this;
        }

        public Builder secondItem(ItemPredicate secondItem) {
            return secondItem(Holder.direct(new MatchTool(Optional.of(secondItem))));
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.USE_GRINDSTONE.get().createCriterion(
                new TriggerInstance(player, location, firstItem, secondItem)
            );
        }
    }
}
