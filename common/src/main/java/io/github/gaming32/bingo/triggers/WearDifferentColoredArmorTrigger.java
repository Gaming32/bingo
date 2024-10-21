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
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.component.DyedItemColor;
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
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                Codec.BOOL.optionalFieldOf("allow_uncolored", true).forGetter(TriggerInstance::allowUncolored),
                ExtraCodecs.POSITIVE_INT.fieldOf("min_count").forGetter(TriggerInstance::minCount),
                ItemPredicate.CODEC.optionalFieldOf("item_predicate").forGetter(TriggerInstance::itemPredicate)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(Inventory inventory, ProgressListener<TriggerInstance> progressListener) {
            final IntSet discovered = new IntOpenHashSet();
            for (final var item : inventory.armor) {
                if (itemPredicate.isPresent() && !itemPredicate.get().test(item)) {
                    continue;
                }
                if (item.is(ItemTags.DYEABLE)) {
                    final DyedItemColor color = item.get(DataComponents.DYED_COLOR);
                    if (!allowUncolored && color == null) {
                        continue;
                    }
                    if (discovered.add(color != null ? color.rgb() : -1) && discovered.size() >= minCount) {
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
