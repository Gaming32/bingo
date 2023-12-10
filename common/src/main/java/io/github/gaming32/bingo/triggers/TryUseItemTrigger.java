package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.util.CustomEnumCodec;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class TryUseItemTrigger extends SimpleCriterionTrigger<TryUseItemTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, InteractionHand hand) {
        final ItemStack item = player.getItemInHand(hand);
        trigger(player, instance -> instance.matches(item, hand));
    }

    public static Builder builder() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<ItemPredicate> item,
        Optional<InteractionHand> hand
    ) implements SimpleInstance {
        private static final CustomEnumCodec<InteractionHand> INTERACTION_HAND_CODEC = CustomEnumCodec.of(InteractionHand.class);
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "item").forGetter(TriggerInstance::item),
                ExtraCodecs.strictOptionalField(INTERACTION_HAND_CODEC.codec(), "hand").forGetter(TriggerInstance::hand)
            ).apply(instance, TriggerInstance::new)
        );

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

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.TRY_USE_ITEM.get().createCriterion(new TriggerInstance(player, item, hand));
        }
    }
}
