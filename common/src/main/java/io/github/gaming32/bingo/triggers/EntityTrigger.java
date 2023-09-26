package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EntityTrigger extends SimpleCriterionTrigger<EntityTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(player, EntityPredicate.fromJson(json, "entity", context));
    }

    public void trigger(ServerPlayer player, Entity entity) {
        final LootContext mobContext = EntityPredicate.createContext(player, entity);
        trigger(player, instance -> instance.matches(mobContext));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ContextAwarePredicate> entity;

        public TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> entity) {
            super(player);
            this.entity = entity;
        }

        public static Criterion<TriggerInstance> brokeCrossbow(EntityPredicate mob) {
            return BingoTriggers.MOB_BROKE_CROSSBOW.createCriterion(new TriggerInstance(
                Optional.empty(), EntityPredicate.wrap(Optional.ofNullable(mob))
            ));
        }

        public static Criterion<TriggerInstance> stunnedRavager() {
            return BingoTriggers.STUN_RAVAGER.createCriterion(new TriggerInstance(Optional.empty(), Optional.empty()));
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            entity.ifPresent(p -> result.add("entity", p.toJson()));
            return result;
        }

        public boolean matches(LootContext mob) {
            return this.entity.isEmpty() || this.entity.get().matches(mob);
        }
    }
}
