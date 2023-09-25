package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;

public class ZombifyPigTrigger extends SimpleCriterionTrigger<ZombifyPigTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext context) {
        return new TriggerInstance(
            predicate,
            EntityPredicate.fromJson(json, "pig", context),
            EntityPredicate.fromJson(json, "zombified_piglin", context),
            json.has("direct") ? GsonHelper.getAsBoolean(json, "direct") : null
        );
    }

    public void trigger(ServerPlayer player, Pig pig, Entity zombifiedPiglin, boolean direct) {
        final LootContext pigContext = EntityPredicate.createContext(player, pig);
        final LootContext zombifiedPiglinContext = EntityPredicate.createContext(player, zombifiedPiglin);
        trigger(player, instance -> instance.matches(pigContext, zombifiedPiglinContext, direct));
    }

    public static Builder zombifyPig() {
        return new Builder();
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ContextAwarePredicate pig;
        private final ContextAwarePredicate zombifiedPiglin;
        private final Boolean direct;

        public TriggerInstance(ContextAwarePredicate player, ContextAwarePredicate pig, ContextAwarePredicate zombifiedPiglin, Boolean direct) {
            super(ID, player);
            this.pig = pig;
            this.zombifiedPiglin = zombifiedPiglin;
            this.direct = direct;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            final JsonObject result = super.serializeToJson(context);
            result.add("pig", pig.toJson(context));
            result.add("zombified_piglin", zombifiedPiglin.toJson(context));
            result.addProperty("direct", direct);
            return result;
        }

        public boolean matches(LootContext pig, LootContext zombifiedPiglin, boolean direct) {
            if (!this.pig.matches(pig)) {
                return false;
            }
            if (!this.zombifiedPiglin.matches(zombifiedPiglin)) {
                return false;
            }
            if (this.direct != null && direct != this.direct) {
                return false;
            }
            return true;
        }
    }

    public static class Builder {
        private ContextAwarePredicate player = ContextAwarePredicate.ANY;
        private ContextAwarePredicate pig = ContextAwarePredicate.ANY;
        private ContextAwarePredicate zombifiedPiglin = ContextAwarePredicate.ANY;
        private Boolean direct = null;

        private Builder() {
        }

        public Builder player(ContextAwarePredicate predicate) {
            this.player = predicate;
            return this;
        }

        public Builder player(EntityPredicate predicate) {
            return player(EntityPredicate.wrap(predicate));
        }

        public Builder pig(ContextAwarePredicate predicate) {
            this.pig = predicate;
            return this;
        }

        public Builder pig(EntityPredicate predicate) {
            return pig(EntityPredicate.wrap(predicate));
        }

        public Builder zombifiedPiglin(ContextAwarePredicate predicate) {
            this.zombifiedPiglin = predicate;
            return this;
        }

        public Builder zombifiedPiglin(EntityPredicate predicate) {
            return zombifiedPiglin(EntityPredicate.wrap(predicate));
        }

        public Builder direct(boolean direct) {
            this.direct = direct;
            return this;
        }

        public TriggerInstance build() {
            return new TriggerInstance(player, pig, zombifiedPiglin, direct);
        }
    }
}
