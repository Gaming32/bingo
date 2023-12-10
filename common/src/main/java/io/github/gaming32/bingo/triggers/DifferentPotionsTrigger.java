package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
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

public class DifferentPotionsTrigger extends SimpleCriterionTrigger<DifferentPotionsTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Inventory inventory) {
        trigger(player, instance -> instance.matches(player, inventory));
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
            return BingoTriggers.DIFFERENT_POTIONS.createCriterion(
                new TriggerInstance(Optional.empty(), minCount)
            );
        }

        public boolean matches(ServerPlayer player, Inventory inventory) {
            final Set<String> discovered = new HashSet<>();
            for (int i = 0, l = inventory.getContainerSize(); i < l; i++) {
                final ItemStack item = inventory.getItem(i);
                if (item.getItem() instanceof PotionItem) {
                    final Potion potion = PotionUtils.getPotion(item);
                    if (potion != Potions.EMPTY && discovered.add(potion.getName("")) && discovered.size() >= minCount) {
//                        setProgress(player, minCount, minCount);
                        return true;
                    }
                }
            }
//            setProgress(player, discovered.size(), minCount);
            return false;
        }
    }
}
