package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EntityKilledPlayerTrigger extends SimpleCriterionTrigger<EntityKilledPlayerTrigger.TriggerInstance> {
    @NotNull
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
        final LootContext directEntityContext = EntityPredicate.createContext(player, source.getDirectEntity());
        final LootContext sourceEntityContext = EntityPredicate.createContext(player, source.getEntity());
        trigger(player, instance -> instance.matches(player, creditedEntityContext, directEntityContext, sourceEntityContext, source));
    }

    public static Builder builder() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<ContextAwarePredicate> creditedEntity,
        Optional<ContextAwarePredicate> directEntity,
        Optional<ContextAwarePredicate> sourceEntity,
        Optional<DamageSourcePredicate> source
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("credited_entity").forGetter(TriggerInstance::creditedEntity),
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("direct_entity").forGetter(TriggerInstance::directEntity),
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("source_entity").forGetter(TriggerInstance::sourceEntity),
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
            if (this.creditedEntity.isPresent() && !this.creditedEntity.get().matches(creditedEntity)) {
                return false;
            }
            if (this.directEntity.isPresent() && !this.directEntity.get().matches(directEntity)) {
                return false;
            }
            if (this.sourceEntity.isPresent() && !this.sourceEntity.get().matches(sourceEntity)) {
                return false;
            }
            if (this.source.isPresent() && !this.source.get().matches(player, source)) {
                return false;
            }
            return true;
        }

        @Override
        public void validate(CriterionValidator criterionValidator) {
            SimpleInstance.super.validate(criterionValidator);
            criterionValidator.validateEntity(creditedEntity, ".credited_entity");
            criterionValidator.validateEntity(directEntity, ".direct_entity");
            criterionValidator.validateEntity(sourceEntity, ".source_entity");
        }
    }

    public static class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private Optional<ContextAwarePredicate> creditedEntity = Optional.empty();
        private Optional<ContextAwarePredicate> directEntity = Optional.empty();
        private Optional<ContextAwarePredicate> sourceEntity = Optional.empty();
        private Optional<DamageSourcePredicate> source = Optional.empty();

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.ofNullable(player);
            return this;
        }

        public Builder player(EntityPredicate player) {
            return player(EntityPredicate.wrap(player));
        }

        public Builder creditedEntity(ContextAwarePredicate creditedEntity) {
            this.creditedEntity = Optional.ofNullable(creditedEntity);
            return this;
        }

        public Builder creditedEntity(EntityPredicate creditedEntity) {
            return creditedEntity(EntityPredicate.wrap(creditedEntity));
        }

        public Builder directEntity(ContextAwarePredicate directEntity) {
            this.directEntity = Optional.ofNullable(directEntity);
            return this;
        }

        public Builder directEntity(EntityPredicate directEntity) {
            return directEntity(EntityPredicate.wrap(directEntity));
        }

        public Builder sourceEntity(ContextAwarePredicate sourceEntity) {
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
