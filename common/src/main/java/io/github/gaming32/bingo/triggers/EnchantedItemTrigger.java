package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class EnchantedItemTrigger extends SimpleCriterionTrigger<EnchantedItemTrigger.TriggerInstance> {
    public static final ResourceLocation ID = new ResourceLocation("bingo:enchanted_item");

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext context) {
        return new TriggerInstance(
            predicate,
//            ItemPredicate.fromJson(json.get("item")),
            MinMaxBounds.Ints.fromJson(json.get("levels_spent")),
            MinMaxBounds.Ints.fromJson(json.get("required_levels"))
        );
    }

    public void trigger(ServerPlayer player, /* ItemStack item, */ int levelsSpent, int levelsRequired) {
        trigger(player, instance -> instance.matches(/* item, */ levelsSpent, levelsRequired));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
//        private final ItemPredicate item;
        private final MinMaxBounds.Ints levelsSpent;
        private final MinMaxBounds.Ints requiredLevels;

        public TriggerInstance(ContextAwarePredicate predicate, /* ItemPredicate item, */ MinMaxBounds.Ints levelsSpent, MinMaxBounds.Ints requiredLevels) {
            super(ID, predicate);
//            this.item = item;
            this.levelsSpent = levelsSpent;
            this.requiredLevels = requiredLevels;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            final JsonObject result = super.serializeToJson(context);
//            if (item != ItemPredicate.ANY) {
//                result.add("item", item.serializeToJson());
//            }
            if (!levelsSpent.isAny()) {
                result.add("levels_spent", levelsSpent.serializeToJson());
            }
            if (!requiredLevels.isAny()) {
                result.add("required_levels", requiredLevels.serializeToJson());
            }
            return result;
        }

        public boolean matches(/* ItemStack item, */ int levelsSpent, int levelsRequired) {
//            if (!this.item.matches(item)) {
//                return false;
//            }
            if (!this.levelsSpent.matches(levelsSpent)) {
                return false;
            }
            if (!this.requiredLevels.matches(levelsRequired)) {
                return false;
            }
            return true;
        }
    }
}
