package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.Codec;
import io.github.gaming32.bingo.util.BingoCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface GoalIcon {
    Codec<GoalIcon> CODEC = BingoCodecs.registrarByName(GoalIconType.REGISTRAR)
        .dispatch(GoalIcon::type, type -> type.codec().codec());

    /**
     * Used for rendering count, as well as for a fallback for Vanilla clients.
     */
    ItemStack item();

    GoalIconType<?> type();

    @SuppressWarnings("unchecked")
    static GoalIcon infer(Object obj) {
        if (obj == null) {
            return EmptyIcon.INSTANCE;
        }
        if (obj instanceof GoalIcon icon) {
            return icon;
        }
        if (obj instanceof ItemStack stack) {
            return new ItemIcon(stack);
        }
        if (obj instanceof Block block) {
            return BlockIcon.ofBlock(block);
        }
        if (obj instanceof BlockState state) {
            return BlockIcon.ofBlock(state);
        }
        if (obj instanceof ItemLike item) {
            return ItemIcon.ofItem(item);
        }
        if (obj instanceof EntityType<?> entityType) {
            return EntityIcon.ofSpawnEgg(entityType);
        }
        if (obj instanceof TagKey<?> tagKey) {
            if (tagKey.registry() == Registries.ITEM) {
                return new ItemTagCycleIcon((TagKey<Item>) tagKey);
            }
            if (tagKey.registry() == Registries.ENTITY_TYPE) {
                return new EntityTypeTagCycleIcon((TagKey<EntityType<?>>) tagKey);
            }
        }
        throw new IllegalArgumentException("Couldn't infer GoalIcon from " + obj);
    }
}
