package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContextSource;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public class MobConvertedTrigger extends SimpleCriterionTrigger<MobConvertedTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Mob convertedFrom, Mob convertedTo) {
        trigger(player, instance -> instance.matches(player, convertedFrom, convertedTo));
    }

    public static Builder builder() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<Holder<LootItemCondition>> player,
        Optional<Holder<LootItemCondition>> convertedFrom,
        Optional<Holder<LootItemCondition>> convertedTo
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                LootItemCondition.CODEC.optionalFieldOf("converted_from").forGetter(TriggerInstance::convertedFrom),
                LootItemCondition.CODEC.optionalFieldOf("converted_to").forGetter(TriggerInstance::convertedTo)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(ServerPlayer player, Mob convertedFrom, Mob convertedTo) {
            if (this.convertedFrom.isPresent() && !this.convertedFrom.get().value().test(EntityPredicate.createContext(player, convertedFrom))) {
                return false;
            }
            if (this.convertedTo.isPresent() && !this.convertedTo.get().value().test(EntityPredicate.createContext(player, convertedTo))) {
                return false;
            }
            return true;
        }

        @Override
        public void validate(ValidationContextSource validator) {
            SimpleInstance.super.validate(validator);
            Validatable.validateHolder(validator.entityContext(), "converted_from", convertedFrom);
            Validatable.validateHolder(validator.entityContext(), "converted_to", convertedTo);
        }
    }

    public static final class Builder {
        private Optional<Holder<LootItemCondition>> player = Optional.empty();
        private Optional<Holder<LootItemCondition>> convertedFrom = Optional.empty();
        private Optional<Holder<LootItemCondition>> convertedTo = Optional.empty();

        private Builder() {
        }

        public Builder player(EntityPredicate player) {
            return player(EntityPredicate.wrap(player));
        }

        public Builder player(Holder<LootItemCondition> player) {
            this.player = Optional.of(player);
            return this;
        }

        public Builder convertedFrom(HolderGetter<EntityType<?>> entityTypes, EntityType<?> convertedFromType) {
            return convertedFrom(EntityPredicate.Builder.entity().of(entityTypes, convertedFromType).build());
        }

        public Builder convertedFrom(EntityPredicate convertedFrom) {
            return convertedFrom(EntityPredicate.wrap(convertedFrom));
        }

        public Builder convertedFrom(Holder<LootItemCondition> convertedFrom) {
            this.convertedFrom = Optional.of(convertedFrom);
            return this;
        }

        public Builder convertedTo(HolderGetter<EntityType<?>> entityTypes, EntityType<?> convertedToType) {
            return convertedTo(EntityPredicate.Builder.entity().of(entityTypes, convertedToType).build());
        }

        public Builder convertedTo(EntityPredicate convertedTo) {
            return convertedTo(EntityPredicate.wrap(convertedTo));
        }

        public Builder convertedTo(Holder<LootItemCondition> convertedTo) {
            this.convertedTo = Optional.of(convertedTo);
            return this;
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.MOB_CONVERTED.get().createCriterion(
                new TriggerInstance(player, convertedFrom, convertedTo)
            );
        }
    }
}
