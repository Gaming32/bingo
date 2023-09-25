package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import io.github.gaming32.bingo.subpredicates.ItemEntityPredicate;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class KillItemTrigger extends SimpleCriterionTrigger<KillItemTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(
            player,
            EntityPredicate.fromJson(json, "item", context),
            DamagePredicate.fromJson(json.get("damage"))
        );
    }

    public void trigger(ItemEntity item, DamageSource source, float amount) {
        if (item.getOwner() instanceof ServerPlayer player) {
            final LootContext itemContext = EntityPredicate.createContext(player, item);
            trigger(player, triggerInstance -> triggerInstance.matches(player, itemContext, source, amount));
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ContextAwarePredicate> item;
        private final Optional<DamagePredicate> damage;

        public TriggerInstance(
            Optional<ContextAwarePredicate> player,
            Optional<ContextAwarePredicate> item,
            Optional<DamagePredicate> damage
        ) {
            super(player);
            this.item = item;
            this.damage = damage;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            item.ifPresent(p -> result.add("item", p.toJson()));
            damage.ifPresent(p -> result.add("damage", p.serializeToJson()));
            return result;
        }

        public boolean matches(ServerPlayer player, LootContext item, DamageSource source, float amount) {
            if (this.item.isPresent() && !this.item.get().matches(item)) {
                return false;
            }
            if (this.damage.isPresent() && !this.damage.get().matches(player, source, amount, amount, false)) {
                return false;
            }
            return true;
        }
    }

    public static final class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private Optional<ContextAwarePredicate> item = Optional.empty();
        private Optional<DamagePredicate> damage = Optional.empty();

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.ofNullable(player);
            return this;
        }

        public Builder item(ContextAwarePredicate item) {
            this.item = Optional.ofNullable(item);
            return this;
        }

        public Builder item(EntityPredicate item) {
            this.item = EntityPredicate.wrap(Optional.ofNullable(item));
            return this;
        }

        public Builder item(ItemPredicate item) {
            return item(EntityPredicate.Builder.entity()
                .subPredicate(ItemEntityPredicate.droppedBy(Optional.ofNullable(item), Optional.empty()))
                .build()
            );
        }

        public Builder damage(DamagePredicate damage) {
            this.damage = Optional.ofNullable(damage);
            return this;
        }

        public TriggerInstance build() {
            return new TriggerInstance(player, item, damage);
        }
    }
}
