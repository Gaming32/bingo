package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
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
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(player, GsonHelper.getAsInt(json, "min_count"));
    }

    public void trigger(ServerPlayer player, Inventory inventory) {
        trigger(player, instance -> instance.matches(inventory));
    }

    public static class TriggerInstance extends AbstractProgressibleTriggerInstance {
        private final int minCount;

        public TriggerInstance(Optional<ContextAwarePredicate> player, int minCount) {
            super(player);
            this.minCount = minCount;
        }

        public static Criterion<TriggerInstance> differentPotions(int minCount) {
            return BingoTriggers.DIFFERENT_POTIONS.createCriterion(
                new TriggerInstance(Optional.empty(), minCount)
            );
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            result.addProperty("min_count", minCount);
            return result;
        }

        public boolean matches(Inventory inventory) {
            final Set<String> discovered = new HashSet<>();
            for (int i = 0, l = inventory.getContainerSize(); i < l; i++) {
                final ItemStack item = inventory.getItem(i);
                if (item.getItem() instanceof PotionItem) {
                    final Potion potion = PotionUtils.getPotion(item);
                    if (potion != Potions.EMPTY && discovered.add(potion.getName("")) && discovered.size() >= minCount) {
                        setProgress(minCount, minCount);
                        return true;
                    }
                }
            }
            setProgress(discovered.size(), minCount);
            return false;
        }
    }
}
