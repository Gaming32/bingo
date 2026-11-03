package io.github.gaming32.bingo.triggers;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.event.InventoryChangedCallback;
import io.github.gaming32.bingo.triggers.progress.SimpleProgressibleCriterionTrigger;
import io.github.gaming32.bingo.util.BingoCodecs;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class HasSomeItemsFromTagTrigger extends SimpleProgressibleCriterionTrigger<HasSomeItemsFromTagTrigger.TriggerInstance> {
    static {
        InventoryChangedCallback.HANDLERS.add((player, inventory) -> BingoTriggers.HAS_SOME_ITEMS_FROM_TAG.get().trigger(player, inventory));
    }

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Inventory inventory) {
        final ProgressListener<TriggerInstance> progressListener = getProgressListener(player);
        trigger(player, triggerInstance -> triggerInstance.matches(inventory, progressListener));
    }

    public static Builder builder() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<Holder<LootItemCondition>> player,
        TagKey<Item> tag,
        int requiredCount,
        List<TagKey<Item>> groups
    ) implements SimpleInstance {
        private static final int ALL = -1;
        private static final Codec<Integer> REQUIRED_COUNT_CODEC = BingoCodecs.firstValid(ExtraCodecs.POSITIVE_INT, BingoCodecs.exactly(ALL));
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(TriggerInstance::tag),
                REQUIRED_COUNT_CODEC.fieldOf("required_count").forGetter(TriggerInstance::requiredCount),
                TagKey.codec(Registries.ITEM).listOf().optionalFieldOf("groups", List.of()).forGetter(TriggerInstance::groups)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(Inventory inventory, ProgressListener<TriggerInstance> progressListener) {
            int requiredCount = this.requiredCount;
            if (requiredCount == ALL) {
                var tag = BuiltInRegistries.ITEM.get(this.tag);
                if (tag.isEmpty()) {
                    return false;
                }
                requiredCount = (int)tag.get().stream().map(this::getCountedKey).distinct().count();
            }

            Set<Object> foundItems = new HashSet<>();
            for (int i = 0, l = inventory.getContainerSize(); i < l; i++) {
                final ItemStack item = inventory.getItem(i);
                if (item.is(tag) && foundItems.add(getCountedKey(item.typeHolder())) && foundItems.size() >= requiredCount) {
                    progressListener.update(this, requiredCount, requiredCount);
                    return true;
                }
            }

            progressListener.update(this, foundItems.size(), requiredCount);
            return false;
        }

        // All items in the same group share a key, so the group is only counted once
        private Object getCountedKey(Holder<Item> item) {
            for (final TagKey<Item> group : groups) {
                if (item.is(group)) {
                    return group;
                }
            }
            return item.value();
        }
    }

    public static final class Builder {
        private Optional<Holder<LootItemCondition>> player = Optional.empty();
        private TagKey<Item> tag;
        @Nullable
        private Integer requiredCount = null;
        private final ImmutableList.Builder<TagKey<Item>> groups = ImmutableList.builder();

        private Builder() {
        }

        public Builder player(Holder<LootItemCondition> player) {
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
            return requiredCount(TriggerInstance.ALL);
        }

        /**
         * All items in {@code group} will only count once towards the required count
         */
        public Builder group(TagKey<Item> group) {
            groups.add(group);
            return this;
        }

        public Criterion<TriggerInstance> build() {
            if (tag == null) {
                throw new IllegalStateException("Did not specify tag");
            }
            if (requiredCount == null) {
                throw new IllegalStateException("Did not specify requiredCount");
            }
            return BingoTriggers.HAS_SOME_ITEMS_FROM_TAG.get().createCriterion(
                new TriggerInstance(player, tag, requiredCount, groups.build())
            );
        }
    }
}
