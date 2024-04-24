package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.conditions.BingoParamSets;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class UseGrindstoneTrigger extends SimpleCriterionTrigger<UseGrindstoneTrigger.TriggerInstance> {
    @NotNull
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
        Optional<ContextAwarePredicate> player,
        Optional<LocationPredicate> location,
        Optional<ContextAwarePredicate> firstItem,
        Optional<ContextAwarePredicate> secondItem
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                LocationPredicate.CODEC.optionalFieldOf("location").forGetter(TriggerInstance::location),
                ContextAwarePredicate.CODEC.optionalFieldOf("first_item").forGetter(TriggerInstance::firstItem),
                ContextAwarePredicate.CODEC.optionalFieldOf("second_item").forGetter(TriggerInstance::secondItem)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(ServerPlayer player, double x, double y, double z, ItemStack firstItem, ItemStack secondItem) {
            if (location.isPresent() && !location.get().matches(player.serverLevel(), x, y, z)) {
                return false;
            }
            if (this.firstItem.isPresent() && !this.firstItem.get().matches(BingoParamSets.wrapTool(player, firstItem))) {
                return false;
            }
            if (this.secondItem.isPresent() && !this.secondItem.get().matches(BingoParamSets.wrapTool(player, secondItem))) {
                return false;
            }
            return true;
        }

        @Override
        public void validate(CriterionValidator validator) {
            SimpleInstance.super.validate(validator);
            firstItem.ifPresent(p -> validator.validate(p, BingoParamSets.TOOL, ".first_item"));
            secondItem.ifPresent(p -> validator.validate(p, BingoParamSets.TOOL, ".second_item"));
        }
    }

    public static final class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private Optional<LocationPredicate> location = Optional.empty();
        private Optional<ContextAwarePredicate> firstItem = Optional.empty();
        private Optional<ContextAwarePredicate> secondItem = Optional.empty();

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.of(player);
            return this;
        }

        public Builder location(LocationPredicate location) {
            this.location = Optional.of(location);
            return this;
        }

        public Builder firstItem(ContextAwarePredicate firstItem) {
            this.firstItem = Optional.of(firstItem);
            return this;
        }

        public Builder firstItem(ItemPredicate firstItem) {
            return firstItem(ContextAwarePredicate.create(new MatchTool(Optional.of(firstItem))));
        }

        public Builder secondItem(ContextAwarePredicate secondItem) {
            this.secondItem = Optional.of(secondItem);
            return this;
        }

        public Builder secondItem(ItemPredicate secondItem) {
            return secondItem(ContextAwarePredicate.create(new MatchTool(Optional.of(secondItem))));
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.USE_GRINDSTONE.get().createCriterion(
                new TriggerInstance(player, location, firstItem, secondItem)
            );
        }
    }
}
