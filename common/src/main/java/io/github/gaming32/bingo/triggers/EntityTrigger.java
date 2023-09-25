package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;

public class EntityTrigger extends SimpleCriterionTrigger<EntityTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        return new TriggerInstance(id, player, EntityPredicate.fromJson(json, "entity", context));
    }

    public void trigger(ServerPlayer player, Entity entity) {
        final LootContext mobContext = EntityPredicate.createContext(player, entity);
        trigger(player, instance -> instance.matches(mobContext));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ContextAwarePredicate entity;

        public TriggerInstance(ResourceLocation id, ContextAwarePredicate player, ContextAwarePredicate entity) {
            super(id, player);
            this.entity = entity;
        }

        public static TriggerInstance brokeCrossbow(EntityPredicate mob) {
            return new TriggerInstance(BingoTriggers.MOB_BROKE_CROSSBOW.getId(), ContextAwarePredicate.ANY, EntityPredicate.wrap(mob));
        }

        public static TriggerInstance stunnedRavager() {
            return new TriggerInstance(BingoTriggers.STUN_RAVAGER.getId(), ContextAwarePredicate.ANY, ContextAwarePredicate.ANY);
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            final JsonObject result = super.serializeToJson(context);
            result.add("entity", entity.toJson(context));
            return result;
        }

        public boolean matches(LootContext mob) {
            return this.entity.matches(mob);
        }
    }
}
