package io.github.gaming32.bingo.triggers;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
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
import java.util.Set;

public class DifferentPotionsTrigger extends SimpleCriterionTrigger<DifferentPotionsTrigger.TriggerInstance> {
    public static final ResourceLocation ID = new ResourceLocation("bingo:different_potions");

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        return new TriggerInstance(player, GsonHelper.getAsInt(json, "min_count"));
    }

    public void trigger(ServerPlayer player, Inventory inventory) {
        trigger(player, instance -> instance.matches(inventory));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final int minCount;

        public TriggerInstance(ContextAwarePredicate player, int minCount) {
            super(ID, player);
            this.minCount = minCount;
        }

        public static TriggerInstance differentPotions(int minCount) {
            return new TriggerInstance(ContextAwarePredicate.ANY, minCount);
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            final JsonObject result = super.serializeToJson(context);
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
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
