package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface GoalIcon {
    Codec<GoalIcon> CODEC = GoalIconType.REGISTER
        .registry()
        .byNameCodec()
        .dispatch(GoalIcon::type, GoalIconType::codec);
    StreamCodec<ByteBuf, GoalIcon> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    /**
     * Used for rendering count, as well as for a fallback for Vanilla clients.
     */
    ItemStack getFallback(RegistryAccess registries);

    default ItemStack getFallbackWithStaticContext() {
        return getFallback(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
    }

    GoalIconType<?> type();

    @SuppressWarnings("unchecked")
    static GoalIcon infer(Object obj) {
        return switch (obj) {
            case null -> EmptyIcon.INSTANCE;
            case GoalIcon icon -> icon;
            case ItemStack stack -> new ItemIcon(stack);
            case Block block -> BlockIcon.ofBlock(block);
            case BlockState state -> BlockIcon.ofBlock(state);
            case ItemLike item -> ItemIcon.ofItem(item);
            case EntityType<?> entityType -> EntityIcon.ofSpawnEgg(entityType);
            case TagKey<?> tagKey -> {
                if (tagKey.registry() == Registries.ITEM) {
                    yield new ItemTagCycleIcon((TagKey<Item>)tagKey);
                }
                if (tagKey.registry() == Registries.ENTITY_TYPE) {
                    yield new EntityTypeTagCycleIcon((TagKey<EntityType<?>>)tagKey);
                }
                throw new IllegalArgumentException("No TagCycleIcon for registry " + tagKey.registry().location());
            }
            default -> throw new IllegalArgumentException("Couldn't infer GoalIcon from " + obj);
        };
    }

    interface WithoutContext extends GoalIcon {
        ItemStack getFallback();

        @Override
        default ItemStack getFallback(RegistryAccess registries) {
            return getFallback();
        }

        @Override
        default ItemStack getFallbackWithStaticContext() {
            return getFallback();
        }
    }
}
