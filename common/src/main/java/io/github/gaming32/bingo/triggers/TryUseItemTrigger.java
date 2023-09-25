package io.github.gaming32.bingo.triggers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class TryUseItemTrigger extends SimpleCriterionTrigger<TryUseItemTrigger.TriggerInstance> {
    private static final Map<String, InteractionHand> HANDS = ImmutableMap.of(
        "main_hand", InteractionHand.MAIN_HAND,
        "off_hand", InteractionHand.OFF_HAND
    );
    private static final Map<InteractionHand, String> INVERSE_HANDS = HANDS.entrySet()
        .stream().collect(Maps.toImmutableEnumMap(Map.Entry::getValue, Map.Entry::getKey));

    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext deserializationContext) {
        final InteractionHand hand;
        if (json.has("hand")) {
            final String stringHand = GsonHelper.getAsString(json, "hand");
            hand = HANDS.get(stringHand);
            if (hand == null) {
                throw new JsonSyntaxException("Unknown hand \"" + stringHand + "\"");
            }
        } else {
            hand = null;
        }
        return new TriggerInstance(predicate, ItemPredicate.fromJson(json.get("item")), hand);
    }

    public void trigger(ServerPlayer player, InteractionHand hand) {
        final ItemStack item = player.getItemInHand(hand);
        trigger(player, instance -> instance.matches(item, hand));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ItemPredicate item;
        @Nullable
        private final InteractionHand hand;

        public TriggerInstance(ContextAwarePredicate player, ItemPredicate item, @Nullable InteractionHand hand) {
            super(ID, player);
            this.item = item;
            this.hand = hand;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            final JsonObject result = super.serializeToJson(context);
            result.add("item", item.serializeToJson());
            result.addProperty("hand", INVERSE_HANDS.get(hand));
            return result;
        }

        public boolean matches(ItemStack item, InteractionHand hand) {
            if (!this.item.matches(item)) {
                return false;
            }
            if (this.hand != null && hand != this.hand) {
                return false;
            }
            return true;
        }
    }

    public static final class Builder {
        private ContextAwarePredicate player = ContextAwarePredicate.ANY;
        private ItemPredicate item = ItemPredicate.ANY;
        @Nullable
        private InteractionHand hand;

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = player;
            return this;
        }

        public Builder item(ItemPredicate item) {
            this.item = item;
            return this;
        }

        public Builder hand(InteractionHand hand) {
            this.hand = hand;
            return this;
        }

        public TriggerInstance build() {
            return new TriggerInstance(player, item, hand);
        }
    }
}
