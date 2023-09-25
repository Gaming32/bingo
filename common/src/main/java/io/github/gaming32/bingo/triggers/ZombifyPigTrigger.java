package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ZombifyPigTrigger extends SimpleCriterionTrigger<ZombifyPigTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(
            player,
            EntityPredicate.fromJson(json, "pig", context),
            EntityPredicate.fromJson(json, "zombified_piglin", context),
            json.has("direct") ? Optional.of(GsonHelper.getAsBoolean(json, "direct")) : Optional.empty()
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
        private final Optional<ContextAwarePredicate> pig;
        private final Optional<ContextAwarePredicate> zombifiedPiglin;
        private final Optional<Boolean> direct;

        public TriggerInstance(
            Optional<ContextAwarePredicate> player,
            Optional<ContextAwarePredicate> pig,
            Optional<ContextAwarePredicate> zombifiedPiglin,
            Optional<Boolean> direct
        ) {
            super(player);
            this.pig = pig;
            this.zombifiedPiglin = zombifiedPiglin;
            this.direct = direct;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            pig.ifPresent(p -> result.add("pig", p.toJson()));
            zombifiedPiglin.ifPresent(p -> result.add("zombified_piglin", p.toJson()));
            direct.ifPresent(p -> result.addProperty("direct", p));
            return result;
        }

        public boolean matches(LootContext pig, LootContext zombifiedPiglin, boolean direct) {
            if (this.pig.isPresent() && !this.pig.get().matches(pig)) {
                return false;
            }
            if (this.zombifiedPiglin.isPresent() && !this.zombifiedPiglin.get().matches(zombifiedPiglin)) {
                return false;
            }
            if (this.direct.isPresent() && direct != this.direct.get()) {
                return false;
            }
            return true;
        }
    }

    public static class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private Optional<ContextAwarePredicate> pig = Optional.empty();
        private Optional<ContextAwarePredicate> zombifiedPiglin = Optional.empty();
        private Optional<Boolean> direct = Optional.empty();

        private Builder() {
        }

        public Builder player(ContextAwarePredicate predicate) {
            this.player = Optional.ofNullable(predicate);
            return this;
        }

        public Builder player(EntityPredicate predicate) {
            return player(EntityPredicate.wrap(predicate));
        }

        public Builder pig(ContextAwarePredicate predicate) {
            this.pig = Optional.ofNullable(predicate);
            return this;
        }

        public Builder pig(EntityPredicate predicate) {
            return pig(EntityPredicate.wrap(predicate));
        }

        public Builder zombifiedPiglin(ContextAwarePredicate predicate) {
            this.zombifiedPiglin = Optional.ofNullable(predicate);
            return this;
        }

        public Builder zombifiedPiglin(EntityPredicate predicate) {
            return zombifiedPiglin(EntityPredicate.wrap(predicate));
        }

        public Builder direct(boolean direct) {
            this.direct = Optional.of(direct);
            return this;
        }

        public TriggerInstance build() {
            return new TriggerInstance(player, pig, zombifiedPiglin, direct);
        }
    }
}
