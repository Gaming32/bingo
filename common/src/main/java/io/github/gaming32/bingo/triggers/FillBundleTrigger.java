package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.event.InventoryChangedCallback;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class FillBundleTrigger extends SimpleCriterionTrigger<FillBundleTrigger.TriggerInstance> {
    static {
        InventoryChangedCallback.HANDLERS.add((player, inventory) -> BingoTriggers.FILL_BUNDLE.get().trigger(player, inventory));
    }

    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Inventory inventory) {
        trigger(player, instance -> instance.matches(inventory));
    }

    public static Builder builder() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        int amount
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                Codec.INT.optionalFieldOf("amount", 64).forGetter(TriggerInstance::amount)
            ).apply(instance, TriggerInstance::new)
        );

        private static final Fraction FULL_STACK = Fraction.getFraction(64, 1);

        public boolean matches(Inventory inventory) {
            for (ItemStack item : inventory) {
                if (item.is(ItemTags.BUNDLES)) {
                    BundleContents bundleContents = item.get(DataComponents.BUNDLE_CONTENTS);
                    if (bundleContents != null && bundleContents.weight().multiplyBy(FULL_STACK).intValue() >= amount) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public static final class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private int amount = 64;

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.ofNullable(player);
            return this;
        }

        public Builder amount(int amount) {
            this.amount = amount;
            return this;
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.FILL_BUNDLE.get().createCriterion(new TriggerInstance(player, amount));
        }
    }
}
