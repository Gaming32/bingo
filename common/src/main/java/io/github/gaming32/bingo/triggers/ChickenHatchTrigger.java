package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ChickenHatchTrigger extends SimpleCriterionTrigger<ChickenHatchTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(
            player,
            EntityPredicate.fromJson(json, "projectile", context),
            MinMaxBounds.Ints.fromJson(json.get("num_chickens"))
        );
    }

    public void trigger(ServerPlayer player, ThrownEgg projectile, int numChickens) {
        LootContext projectileContext = EntityPredicate.createContext(player, projectile);
        trigger(player, instance -> instance.matches(projectileContext, numChickens));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ContextAwarePredicate> projectile;
        private final MinMaxBounds.Ints numChickens;

        public TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ContextAwarePredicate> projectile, MinMaxBounds.Ints numChickens) {
            super(player);
            this.projectile = projectile;
            this.numChickens = numChickens;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            projectile.ifPresent(projectile -> result.add("projectile", projectile.toJson()));
            result.add("num_chickens", numChickens.serializeToJson());
            return result;
        }

        public boolean matches(LootContext projectileContext, int numChickens) {
            if (this.projectile.isPresent() && !this.projectile.get().matches(projectileContext)) {
                return false;
            }
            return this.numChickens.matches(numChickens);
        }
    }

    public static class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private Optional<ContextAwarePredicate> projectile = Optional.empty();
        private MinMaxBounds.Ints numChickens = MinMaxBounds.Ints.ANY;

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.of(player);
            return this;
        }

        public Builder projectile(EntityPredicate projectile) {
            return projectile(EntityPredicate.wrap(projectile));
        }

        public Builder projectile(ContextAwarePredicate projectile) {
            this.projectile = Optional.of(projectile);
            return this;
        }

        public Builder numChickens(MinMaxBounds.Ints numChickens) {
            this.numChickens = numChickens;
            return this;
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.CHICKEN_HATCH.createCriterion(new TriggerInstance(player, projectile, numChickens));
        }
    }
}
