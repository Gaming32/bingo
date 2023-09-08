package io.github.gaming32.bingo.triggers;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import io.github.gaming32.bingo.Bingo;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
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

import java.util.Set;

public class HasSomeItemsFromTagTrigger extends SimpleCriterionTrigger<HasSomeItemsFromTagTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation(Bingo.MOD_ID, "has_some_items_from_tag");

    @Override
    @NotNull
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    @NotNull
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        TagKey<Item> tag = TagKey.create(Registries.ITEM, new ResourceLocation(GsonHelper.getAsString(json, "tag")));
        int requiredCount = GsonHelper.getAsInt(json, "requiredCount");
        return new TriggerInstance(player, tag, requiredCount);
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

        public TriggerInstance(ContextAwarePredicate player, TagKey<Item> tag, int requiredCount) {
            super(ID, player);
            this.tag = tag;
            this.requiredCount = requiredCount;
        }

        @Override
        @NotNull
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.addProperty("tag", tag.location().toString());
            json.addProperty("requiredCount", requiredCount);
            return json;
        }

        public boolean matches(Inventory inventory) {
            Set<Item> foundItems = Sets.newIdentityHashSet();
            for (ItemStack item : inventory.items) {
                if (item.is(tag) && foundItems.add(item.getItem()) && foundItems.size() >= requiredCount) {
                    return true;
                }
            }
            return false;
        }
    }

    public static final class Builder {
        private ContextAwarePredicate player = ContextAwarePredicate.ANY;
        @Nullable
        private TagKey<Item> tag;
        @Nullable
        private Integer requiredCount;

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = player;
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

        public TriggerInstance build() {
            if (tag == null) {
                throw new IllegalStateException("Did not specify tag");
            }
            if (requiredCount == null) {
                throw new IllegalStateException("Did not specify requiredCount");
            }
            return new TriggerInstance(player, tag, requiredCount);
        }
    }
}
