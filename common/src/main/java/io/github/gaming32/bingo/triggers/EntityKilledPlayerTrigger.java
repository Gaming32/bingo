package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;

public class EntityKilledPlayerTrigger extends SimpleCriterionTrigger<EntityKilledPlayerTrigger.TriggerInstance> {
    public static final ResourceLocation ID = new ResourceLocation("bingo:entity_killed_player");

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
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
        private final ContextAwarePredicate creditedEntity;
        private final ContextAwarePredicate directEntity;
        private final ContextAwarePredicate sourceEntity;
        private final DamageSourcePredicate source;

        public TriggerInstance(
            ContextAwarePredicate player,
            ContextAwarePredicate creditedEntity,
            ContextAwarePredicate directEntity,
            ContextAwarePredicate sourceEntity,
            DamageSourcePredicate source
        ) {
            super(ID, player);
            this.creditedEntity = creditedEntity;
            this.directEntity = directEntity;
            this.sourceEntity = sourceEntity;
            this.source = source;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            final JsonObject result = super.serializeToJson(context);
            result.add("credited_entity", creditedEntity.toJson(context));
            result.add("direct_entity", directEntity.toJson(context));
            result.add("source_entity", sourceEntity.toJson(context));
            result.add("source", source.serializeToJson());
            return result;
        }

        public boolean matches(
            ServerPlayer player,
            LootContext creditedEntity,
            LootContext directEntity,
            LootContext sourceEntity,
            DamageSource source
        ) {
            if (!this.creditedEntity.matches(creditedEntity)) {
                return false;
            }
            if (!this.directEntity.matches(directEntity)) {
                return false;
            }
            if (!this.sourceEntity.matches(sourceEntity)) {
                return false;
            }
            if (!this.source.matches(player, source)) {
                return false;
            }
            return true;
        }
    }

    public static class Builder {
        private ContextAwarePredicate player = ContextAwarePredicate.ANY;
        private ContextAwarePredicate creditedEntity = ContextAwarePredicate.ANY;
        private ContextAwarePredicate directEntity = ContextAwarePredicate.ANY;
        private ContextAwarePredicate sourceEntity = ContextAwarePredicate.ANY;
        private DamageSourcePredicate source = DamageSourcePredicate.ANY;

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = player;
            return this;
        }

        public Builder player(EntityPredicate player) {
            return player(EntityPredicate.wrap(player));
        }

        public Builder creditedEntity(ContextAwarePredicate creditedEntity) {
            this.creditedEntity = creditedEntity;
            return this;
        }

        public Builder creditedEntity(EntityPredicate creditedEntity) {
            return creditedEntity(EntityPredicate.wrap(creditedEntity));
        }

        public Builder directEntity(ContextAwarePredicate directEntity) {
            this.directEntity = directEntity;
            return this;
        }

        public Builder directEntity(EntityPredicate directEntity) {
            return directEntity(EntityPredicate.wrap(directEntity));
        }

        public Builder sourceEntity(ContextAwarePredicate sourceEntity) {
            this.sourceEntity = sourceEntity;
            return this;
        }

        public Builder sourceEntity(EntityPredicate sourceEntity) {
            return sourceEntity(EntityPredicate.wrap(sourceEntity));
        }

        public Builder source(DamageSourcePredicate source) {
            this.source = source;
            return this;
        }

        public TriggerInstance build() {
            return new TriggerInstance(
                player,
                creditedEntity,
                directEntity,
                sourceEntity,
                source
            );
        }
    }
}
