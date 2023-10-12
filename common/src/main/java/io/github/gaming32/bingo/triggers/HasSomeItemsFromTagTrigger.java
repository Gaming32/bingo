package io.github.gaming32.bingo.triggers;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

public class HasSomeItemsFromTagTrigger extends SimpleCriterionTrigger<HasSomeItemsFromTagTrigger.TriggerInstance> {
    private static final int ALL = -1;

    @NotNull
    @Override
    protected TriggerInstance createInstance(JsonObject json, Optional<ContextAwarePredicate> player, DeserializationContext context) {
        return new TriggerInstance(
            player,
            TagKey.create(Registries.ITEM, new ResourceLocation(GsonHelper.getAsString(json, "tag"))),
            GsonHelper.getAsInt(json, "required_count")
        );
    }

    public void trigger(ServerPlayer player, Inventory inventory) {
        trigger(player, triggerInstance -> triggerInstance.matches(inventory));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class TriggerInstance  extends AbstractCriterionTriggerInstance {
        private final TagKey<Item> tag;
        private final int requiredCount;

        public TriggerInstance(Optional<ContextAwarePredicate> player, TagKey<Item> tag, int requiredCount) {
            super(player);
            this.tag = tag;
            this.requiredCount = requiredCount;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson() {
            final JsonObject result = super.serializeToJson();
            result.addProperty("tag", tag.location().toString());
            result.addProperty("required_count", requiredCount);
            return result;
        }

        public boolean matches(Inventory inventory) {
            int requiredCount = this.requiredCount;
            if (requiredCount == ALL) {
                var tag = BuiltInRegistries.ITEM.getTag(this.tag);
                if (tag.isEmpty()) {
                    return false;
                }
                requiredCount = tag.get().size();
            }

            Set<Item> foundItems = Sets.newIdentityHashSet();
            for (int i = 0, l = inventory.getContainerSize(); i < l; i++) {
                final ItemStack item = inventory.getItem(i);
                if (item.is(tag) && foundItems.add(item.getItem()) && foundItems.size() >= requiredCount) {
                    return true;
                }
            }
            return false;
        }
    }

    public static final class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private TagKey<Item> tag;
        @Nullable
        private Integer requiredCount = null;

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.ofNullable(player);
            return this;
        }

        public Builder tag(TagKey<Item> tag) {
            this.tag = tag;
            return this;
        }

        public Builder requiredCount(int requiredCount) {
            this.requiredCount = requiredCount;
            return this;
        }

        public Builder requiresAll() {
            return requiredCount(ALL);
        }

        public Criterion<TriggerInstance> build() {
            if (tag == null) {
                throw new IllegalStateException("Did not specify tag");
            }
            if (requiredCount == null) {
                throw new IllegalStateException("Did not specify requiredCount");
            }
            return BingoTriggers.HAS_SOME_ITEMS_FROM_TAG.createCriterion(
                new TriggerInstance(player, tag, requiredCount)
            );
        }
    }
}
