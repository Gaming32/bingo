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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class DifferentPotionsTrigger extends SimpleProgressibleCriterionTrigger<DifferentPotionsTrigger.TriggerInstance> {
    static {
        InventoryChangedEvent.EVENT.register((player, inventory) -> BingoTriggers.DIFFERENT_POTIONS.get().trigger(player, inventory));
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

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        int minCount
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.POSITIVE_INT.fieldOf("min_count").forGetter(TriggerInstance::minCount)
            ).apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> differentPotions(int minCount) {
            return BingoTriggers.DIFFERENT_POTIONS.get().createCriterion(
                new TriggerInstance(Optional.empty(), minCount)
            );
        }

        public boolean matches(Inventory inventory, ProgressListener<TriggerInstance> progressListener) {
            final Set<String> discovered = new HashSet<>();
            for (int i = 0, l = inventory.getContainerSize(); i < l; i++) {
                final ItemStack item = inventory.getItem(i);
                if (item.getItem() instanceof PotionItem) {
                    final Potion potion = PotionUtils.getPotion(item);
                    if (potion != Potions.EMPTY && discovered.add(potion.getName("")) && discovered.size() >= minCount) {
                        progressListener.update(this, minCount, minCount);
                        return true;
                    }
                }
            }
            progressListener.update(this, discovered.size(), minCount);
            return false;
        }
    }
}
