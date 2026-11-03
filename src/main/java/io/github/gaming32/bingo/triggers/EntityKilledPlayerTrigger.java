package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.conditions.BingoContextKeySets;
import net.minecraft.advancements.predicates.DamageSourcePredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContextSource;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public class EntityKilledPlayerTrigger extends SimpleCriterionTrigger<EntityKilledPlayerTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(
        ServerPlayer player,
        Entity creditedEntity,
        DamageSource source
    ) {
        final LootContext creditedEntityContext = EntityPredicate.createContext(player, creditedEntity);
        final LootContext directEntityContext = BingoContextKeySets.wrapOptionalEntity(player, source.getDirectEntity());
        final LootContext sourceEntityContext = BingoContextKeySets.wrapOptionalEntity(player, source.getEntity());
        trigger(player, instance -> instance.matches(player, creditedEntityContext, directEntityContext, sourceEntityContext, source));
    }

    public static Builder builder() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<Holder<LootItemCondition>> player,
        Optional<Holder<LootItemCondition>> creditedEntity,
        Optional<Holder<LootItemCondition>> directEntity,
        Optional<Holder<LootItemCondition>> sourceEntity,
        Optional<DamageSourcePredicate> source
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                LootItemCondition.CODEC.optionalFieldOf("credited_entity").forGetter(TriggerInstance::creditedEntity),
                LootItemCondition.CODEC.optionalFieldOf("direct_entity").forGetter(TriggerInstance::directEntity),
                LootItemCondition.CODEC.optionalFieldOf("source_entity").forGetter(TriggerInstance::sourceEntity),
                DamageSourcePredicate.CODEC.optionalFieldOf("source").forGetter(TriggerInstance::source)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(
            ServerPlayer player,
            LootContext creditedEntity,
            LootContext directEntity,
            LootContext sourceEntity,
            DamageSource source
        ) {
            if (this.creditedEntity.isPresent() && !this.creditedEntity.get().value().test(creditedEntity)) {
                return false;
            }
            if (this.directEntity.isPresent() && !this.directEntity.get().value().test(directEntity)) {
                return false;
            }
            if (this.sourceEntity.isPresent() && !this.sourceEntity.get().value().test(sourceEntity)) {
                return false;
            }
            if (this.source.isPresent() && !this.source.get().matches(player, source)) {
                return false;
            }
            return true;
        }

        @Override
        public void validate(ValidationContextSource validator) {
            SimpleInstance.super.validate(validator);
            Validatable.validateHolder(validator.entityContext(), "credited_entity", creditedEntity);
            Validatable.validateHolder(validator.entityContext(), "direct_entity", directEntity);
            Validatable.validateHolder(validator.entityContext(), "source_entity", sourceEntity);
        }
    }

    public static class Builder {
        private Optional<Holder<LootItemCondition>> player = Optional.empty();
        private Optional<Holder<LootItemCondition>> creditedEntity = Optional.empty();
        private Optional<Holder<LootItemCondition>> directEntity = Optional.empty();
        private Optional<Holder<LootItemCondition>> sourceEntity = Optional.empty();
        private Optional<DamageSourcePredicate> source = Optional.empty();

        private Builder() {
        }

        public Builder player(Holder<LootItemCondition> player) {
            this.player = Optional.ofNullable(player);
            return this;
        }

        public Builder player(EntityPredicate player) {
            return player(EntityPredicate.wrap(player));
        }

        public Builder creditedEntity(Holder<LootItemCondition> creditedEntity) {
            this.creditedEntity = Optional.ofNullable(creditedEntity);
            return this;
        }

        public Builder creditedEntity(EntityPredicate creditedEntity) {
            return creditedEntity(EntityPredicate.wrap(creditedEntity));
        }

        public Builder directEntity(Holder<LootItemCondition> directEntity) {
            this.directEntity = Optional.ofNullable(directEntity);
            return this;
        }

        public Builder directEntity(EntityPredicate directEntity) {
            return directEntity(EntityPredicate.wrap(directEntity));
        }

        public Builder sourceEntity(Holder<LootItemCondition> sourceEntity) {
            this.sourceEntity = Optional.ofNullable(sourceEntity);
            return this;
        }

        public Builder sourceEntity(EntityPredicate sourceEntity) {
            return sourceEntity(EntityPredicate.wrap(sourceEntity));
        }

        public Builder source(DamageSourcePredicate source) {
            this.source = Optional.ofNullable(source);
            return this;
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.ENTITY_KILLED_PLAYER.get().createCriterion(
                new TriggerInstance(player, creditedEntity, directEntity, sourceEntity, source)
            );
        }
    }
}
