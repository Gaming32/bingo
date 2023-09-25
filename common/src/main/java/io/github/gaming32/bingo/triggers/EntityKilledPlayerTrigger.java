package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EntityKilledPlayerTrigger extends SimpleCriterionTrigger<EntityKilledPlayerTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(
            player,
            EntityPredicate.fromJson(json, "credited_entity", context),
            EntityPredicate.fromJson(json, "direct_entity", context),
            EntityPredicate.fromJson(json, "source_entity", context),
            DamageSourcePredicate.fromJson(json.get("source"))
        );
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

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ContextAwarePredicate> creditedEntity;
        private final Optional<ContextAwarePredicate> directEntity;
        private final Optional<ContextAwarePredicate> sourceEntity;
        private final Optional<DamageSourcePredicate> source;

        public TriggerInstance(
            Optional<ContextAwarePredicate> player,
            Optional<ContextAwarePredicate> creditedEntity,
            Optional<ContextAwarePredicate> directEntity,
            Optional<ContextAwarePredicate> sourceEntity,
            Optional<DamageSourcePredicate> source
        ) {
            super(player);
            this.creditedEntity = creditedEntity;
            this.directEntity = directEntity;
            this.sourceEntity = sourceEntity;
            this.source = source;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            creditedEntity.ifPresent(p -> result.add("credited_entity", p.toJson()));
            directEntity.ifPresent(p -> result.add("direct_entity", p.toJson()));
            sourceEntity.ifPresent(p -> result.add("source_entity", p.toJson()));
            source.ifPresent(p -> result.add("source", p.serializeToJson()));
            return result;
        }

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
            return BingoTriggers.ENTITY_KILLED_PLAYER.createCriterion(
                new TriggerInstance(player, creditedEntity, directEntity, sourceEntity, source)
            );
        }
    }
}
