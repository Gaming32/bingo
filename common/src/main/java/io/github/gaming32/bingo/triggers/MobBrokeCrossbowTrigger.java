package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;

public class MobBrokeCrossbowTrigger extends SimpleCriterionTrigger<MobBrokeCrossbowTrigger.TriggerInstance> {
    public static final ResourceLocation ID = new ResourceLocation("bingo:mob_broke_crossbow");

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        return new TriggerInstance(player, EntityPredicate.fromJson(json, "mob", context));
    }

    public void trigger(ServerPlayer player, LivingEntity mob) {
        final LootContext mobContext = EntityPredicate.createContext(player, mob);
        trigger(player, instance -> instance.matches(mobContext));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ContextAwarePredicate mob;

        public TriggerInstance(ContextAwarePredicate player, ContextAwarePredicate mob) {
            super(ID, player);
            this.mob = mob;
        }

        public static TriggerInstance brokeCrossbow(EntityPredicate mob) {
            return new TriggerInstance(ContextAwarePredicate.ANY, EntityPredicate.wrap(mob));
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            final JsonObject result = super.serializeToJson(context);
            result.add("mob", mob.toJson(context));
            return result;
        }

        public boolean matches(LootContext mob) {
            return this.mob.matches(mob);
        }
    }
}
