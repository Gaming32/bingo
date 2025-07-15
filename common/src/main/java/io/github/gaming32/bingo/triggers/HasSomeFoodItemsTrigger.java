package io.github.gaming32.bingo.triggers;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.event.InventoryChangedCallback;
import io.github.gaming32.bingo.triggers.progress.SimpleProgressibleCriterionTrigger;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

public class HasSomeFoodItemsTrigger extends SimpleProgressibleCriterionTrigger<HasSomeFoodItemsTrigger.TriggerInstance> {
    static {
        InventoryChangedCallback.HANDLERS.add((player, inventory) -> BingoTriggers.HAS_SOME_FOOD_ITEMS.get().trigger(player, inventory));
    }

    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Inventory inventory) {
        final ProgressListener<TriggerInstance> progressListener = getProgressListener(player);
        trigger(player, instance -> instance.matches(player, inventory, progressListener));
    }

    public static Builder builder() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        int requiredCount,
        Optional<TagPredicate<Item>> tag
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                ExtraCodecs.POSITIVE_INT.fieldOf("required_count").forGetter(TriggerInstance::requiredCount),
                TagPredicate.codec(Registries.ITEM).optionalFieldOf("tag").forGetter(TriggerInstance::tag)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(ServerPlayer player, Inventory inventory, ProgressListener<TriggerInstance> progressListener) {
            final var level = player.level();
            final RecipeManager recipeManager = level.getServer().getRecipeManager();

            Set<Item> foundItems = Sets.newIdentityHashSet();
            for (int i = 0, l = inventory.getContainerSize(); i < l; i++) {
                ItemStack item = inventory.getItem(i);
                if (tag.isPresent() && !tag.get().matches(item.getItemHolder())) {
                    continue;
                }
                final FoodProperties food = item.get(DataComponents.FOOD);
                if (food != null) {
                    // attempt to smelt the item
                    final var input = new SingleRecipeInput(item);
                    var recipe = recipeManager.getRecipeFor(RecipeType.SMELTING, input, level);
                    if (recipe.isPresent()) {
                        item = recipe.get().value().assemble(input, level.registryAccess());
                    }

                    if (foundItems.add(item.getItem()) && foundItems.size() >= requiredCount) {
                        progressListener.update(this, requiredCount, requiredCount);
                        return true;
                    }
                }
            }

            progressListener.update(this, foundItems.size(), requiredCount);
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
            return BingoTriggers.HAS_SOME_FOOD_ITEMS.get().createCriterion(
                new HasSomeFoodItemsTrigger.TriggerInstance(player, requiredCount, tag)
            );
        }
    }
}
