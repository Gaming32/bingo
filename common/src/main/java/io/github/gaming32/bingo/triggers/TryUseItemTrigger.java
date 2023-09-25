package io.github.gaming32.bingo.triggers;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancements.critereon.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class TryUseItemTrigger extends SimpleCriterionTrigger<TryUseItemTrigger.TriggerInstance> {
    private static final BiMap<String, InteractionHand> HANDS = ImmutableBiMap.of(
        "main_hand", InteractionHand.MAIN_HAND,
        "off_hand", InteractionHand.OFF_HAND
    );

    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        final Optional<InteractionHand> hand;
        if (json.has("hand")) {
            final String stringHand = GsonHelper.getAsString(json, "hand");
            hand = Optional.ofNullable(HANDS.get(stringHand));
            if (hand.isEmpty()) {
                throw new JsonSyntaxException("Unknown hand \"" + stringHand + "\"");
            }
        } else {
            hand = Optional.empty();
        }

        return new TriggerInstance(player, ItemPredicate.fromJson(json.get("item")), hand);
    }

    public void trigger(ServerPlayer player, InteractionHand hand) {
        final ItemStack item = player.getItemInHand(hand);
        trigger(player, instance -> instance.matches(item, hand));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ItemPredicate> item;
        private final Optional<InteractionHand> hand;

        public TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item, Optional<InteractionHand> hand) {
            super(player);
            this.item = item;
            this.hand = hand;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            item.ifPresent(p -> result.add("item", p.serializeToJson()));
            hand.ifPresent(p -> result.addProperty("hand", HANDS.inverse().get(p)));
            return result;
        }

        public boolean matches(ItemStack item, InteractionHand hand) {
            if (this.item.isPresent() && !this.item.get().matches(item)) {
                return false;
            }
            if (this.hand.isPresent() && hand != this.hand.get()) {
                return false;
            }
            return true;
        }
    }

    public static final class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private Optional<ItemPredicate> item = Optional.empty();
        private Optional<InteractionHand> hand = Optional.empty();

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.ofNullable(player);
            return this;
        }

        public Builder item(ItemPredicate item) {
            this.item = Optional.ofNullable(item);
            return this;
        }

        public Builder hand(InteractionHand hand) {
            this.hand = Optional.ofNullable(hand);
            return this;
        }

        public TriggerInstance build() {
            return new TriggerInstance(player, item, hand);
        }
    }
}
