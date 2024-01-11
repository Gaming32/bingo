package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.event.InventoryChangedEvent;
import io.github.gaming32.bingo.triggers.progress.SimpleProgressibleCriterionTrigger;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class DifferentColoredShields extends SimpleProgressibleCriterionTrigger<DifferentColoredShields.TriggerInstance> {
    static {
        InventoryChangedEvent.EVENT.register((player, inventory) -> BingoTriggers.DIFFERENT_COLORED_SHIELDS.get().trigger(player, inventory));
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
        int minCount
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.strictOptionalField(Codec.BOOL, "allow_uncolored", false).forGetter(TriggerInstance::allowUncolored),
                ExtraCodecs.POSITIVE_INT.fieldOf("min_count").forGetter(TriggerInstance::minCount)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(Inventory inventory, ProgressListener<TriggerInstance> progressListener) {
            final Set<@Nullable DyeColor> discovered = new HashSet<>();
            for (int i = 0, l = inventory.getContainerSize(); i < l; i++) {
                final ItemStack item = inventory.getItem(i);
                if (item.getItem() instanceof ShieldItem) {
                    final DyeColor color = BlockItem.getBlockEntityData(item) == null ? null : ShieldItem.getColor(item);
                    if (!allowUncolored && color == null) {
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
        private boolean allowUncolored = false;
        private final int minCount;

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

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.DIFFERENT_COLORED_SHIELDS.get().createCriterion(
                new TriggerInstance(player, allowUncolored, minCount)
            );
        }
    }
}
