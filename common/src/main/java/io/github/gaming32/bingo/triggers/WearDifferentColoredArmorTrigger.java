package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.event.InventoryChangedCallback;
import io.github.gaming32.bingo.triggers.progress.SimpleProgressibleCriterionTrigger;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class WearDifferentColoredArmorTrigger extends SimpleProgressibleCriterionTrigger<WearDifferentColoredArmorTrigger.TriggerInstance> {
    static {
        InventoryChangedCallback.HANDLERS.add((player, inventory) -> BingoTriggers.WEAR_DIFFERENT_COLORED_ARMOR.get().trigger(player, inventory));
    }

    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Inventory inventory) {
        final ProgressListener<TriggerInstance> progressListener = getProgressListener(player);
        trigger(player, instance -> instance.matches(inventory, progressListener));
    }

    public static Builder builder(int minCount) {
        return new Builder(minCount);
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        boolean allowUncolored,
        int minCount,
        Optional<ItemPredicate> itemPredicate
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.strictOptionalField(Codec.BOOL, "allow_uncolored", true).forGetter(TriggerInstance::allowUncolored),
                ExtraCodecs.POSITIVE_INT.fieldOf("min_count").forGetter(TriggerInstance::minCount),
                ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "item_predicate").forGetter(TriggerInstance::itemPredicate)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(Inventory inventory, ProgressListener<TriggerInstance> progressListener) {
            final IntSet discovered = new IntOpenHashSet();
            for (int i : Inventory.ALL_ARMOR_SLOTS) {
                final ItemStack item = inventory.getArmor(i);
                if (itemPredicate.isPresent() && !itemPredicate.get().matches(item)) {
                    continue;
                }
                if (item.getItem() instanceof DyeableArmorItem dyeableItem) {
                    final int color = dyeableItem.getColor(item);
                    if (!allowUncolored && color == dyeableItem.getColor(new ItemStack(dyeableItem))) {
                        continue;
                    }
                    if (discovered.add(color) && discovered.size() >= minCount) {
                        progressListener.update(this, minCount, minCount);
                        return true;
                    }
                }
            }
            progressListener.update(this, discovered.size(), minCount);
            return false;
        }
    }

    public static final class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private boolean allowUncolored = true;
        private final int minCount;
        private Optional<ItemPredicate> itemPredicate = Optional.empty();

        private Builder(int minCount) {
            this.minCount = minCount;
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.of(player);
            return this;
        }

        public Builder allowUncolored() {
            this.allowUncolored = true;
            return this;
        }

        public Builder itemPredicate(ItemPredicate itemPredicate) {
            this.itemPredicate = Optional.of(itemPredicate);
            return this;
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.WEAR_DIFFERENT_COLORED_ARMOR.get().createCriterion(
                new TriggerInstance(player, allowUncolored, minCount, itemPredicate)
            );
        }
    }
}
