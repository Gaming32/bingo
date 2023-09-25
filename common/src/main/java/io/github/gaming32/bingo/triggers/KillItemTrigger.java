package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import io.github.gaming32.bingo.subpredicates.ItemEntityPredicate;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import org.jetbrains.annotations.NotNull;

public class KillItemTrigger extends SimpleCriterionTrigger<KillItemTrigger.TriggerInstance> {
    @Override
    @NotNull
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext deserializationContext) {
        return new TriggerInstance(player, EntityPredicate.fromJson(json.get("item")), DamagePredicate.fromJson(json.get("damage")));
    }

    public void trigger(ItemEntity item, DamageSource source, float amount) {
        if (item.getOwner() instanceof ServerPlayer player) {
            trigger(player, triggerInstance -> triggerInstance.matches(player, item, source, amount));
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final EntityPredicate item;
        private final DamagePredicate damage;

        public TriggerInstance(ContextAwarePredicate player, EntityPredicate item, DamagePredicate damage) {
            super(ID, player);
            this.item = item;
            this.damage = damage;
        }

        @Override
        @NotNull
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.add("item", item.serializeToJson());
            json.add("damage", damage.serializeToJson());
            return json;
        }

        public boolean matches(ServerPlayer player, ItemEntity item, DamageSource source, float amount) {
            return this.item.matches(player, item) && this.damage.matches(player, source, amount, amount, false);
        }
    }

    public static final class Builder {
        private ContextAwarePredicate player = ContextAwarePredicate.ANY;
        private EntityPredicate item = EntityPredicate.ANY;
        private DamagePredicate damage = DamagePredicate.ANY;

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = player;
            return this;
        }

        public Builder item(EntityPredicate item) {
            this.item = item;
            return this;
        }

        public Builder item(ItemPredicate item) {
            return item(EntityPredicate.Builder.entity().subPredicate(ItemEntityPredicate.droppedBy(item, EntityPredicate.ANY)).build());
        }

        public Builder damage(DamagePredicate damage) {
            this.damage = damage;
            return this;
        }

        public TriggerInstance build() {
            return new TriggerInstance(player, item, damage);
        }
    }
}
