package io.github.gaming32.bingo.triggers;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import io.github.gaming32.bingo.util.BingoUtil;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

public class HasSomeFoodItemsTrigger extends SimpleCriterionTrigger<HasSomeFoodItemsTrigger.TriggerInstance> {
    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(
            player,
            GsonHelper.getAsInt(json, "required_count"),
            BingoUtil.fromOptionalJsonElement(TagPredicate.codec(Registries.ITEM), json.get("tag"))
        );
    }

    public void trigger(ServerPlayer player, Inventory inventory) {
        trigger(player, instance -> instance.matches(player, inventory));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class TriggerInstance extends AbstractProgressibleTriggerInstance {
        private final int requiredCount;
        private final Optional<TagPredicate<Item>> tag;

        public TriggerInstance(Optional<ContextAwarePredicate> player, int requiredCount, Optional<TagPredicate<Item>> tag) {
            super(player);
            this.requiredCount = requiredCount;
            this.tag = tag;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            result.addProperty("required_count", requiredCount);
            tag.ifPresent(tag -> result.add("tag", BingoUtil.toJsonElement(TagPredicate.codec(Registries.ITEM), tag)));
            return result;
        }

        public boolean matches(ServerPlayer player, Inventory inventory) {
            final Container fakeFurnace = new SimpleContainer(3);
            fakeFurnace.setItem(1, new ItemStack(Items.COAL));

            final Level level = player.level();
            final RecipeManager recipeManager = level.getRecipeManager();

            Set<Item> foundItems = Sets.newIdentityHashSet();
            for (int i = 0, l = inventory.getContainerSize(); i < l; i++) {
                ItemStack item = inventory.getItem(i);
                if (tag.isPresent() && !tag.get().matches(item.getItemHolder())) {
                    continue;
                }
                final FoodProperties food = item.getItem().getFoodProperties();
                if (food != null) {
                    // attempt to smelt the item
                    fakeFurnace.setItem(0, item);
                    Optional<RecipeHolder<SmeltingRecipe>> recipe = recipeManager.getRecipeFor(RecipeType.SMELTING, fakeFurnace, level);
                    if (recipe.isPresent()) {
                        item = recipe.get().value().getResultItem(level.registryAccess());
                    }

                    if (foundItems.add(item.getItem()) && foundItems.size() >= requiredCount) {
                        setProgress(player, requiredCount, requiredCount);
                        return true;
                    }
                }
            }

            setProgress(player, foundItems.size(), requiredCount);
            return false;
        }
    }

    public static final class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        @Nullable
        private Integer requiredCount = null;
        private Optional<TagPredicate<Item>> tag = Optional.empty();

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.ofNullable(player);
            return this;
        }

        public Builder requiredCount(int requiredCount) {
            this.requiredCount = requiredCount;
            return this;
        }

        public Builder tag(TagKey<Item> tag) {
            return tag(TagPredicate.is(tag));
        }

        public Builder tag(TagPredicate<Item> tag) {
            this.tag = Optional.of(tag);
            return this;
        }

        public Criterion<HasSomeFoodItemsTrigger.TriggerInstance> build() {
            if (requiredCount == null) {
                throw new IllegalStateException("Did not specify requiredCount");
            }
            return BingoTriggers.HAS_SOME_FOOD_ITEMS.createCriterion(
                new HasSomeFoodItemsTrigger.TriggerInstance(player, requiredCount, tag)
            );
        }
    }
}
