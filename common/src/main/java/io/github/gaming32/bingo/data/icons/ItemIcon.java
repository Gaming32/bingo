package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.MapCodec;
import io.github.gaming32.bingo.util.BingoCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public record ItemIcon(ItemStack item) implements GoalIcon.WithoutContext {
    public static final MapCodec<ItemIcon> CODEC = BingoCodecs.LENIENT_ITEM_STACK
        .fieldOf("item")
        .xmap(ItemIcon::new, ItemIcon::item);
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemIcon> STREAM_CODEC =
        ItemStack.STREAM_CODEC.map(ItemIcon::new, ItemIcon::item);

    public static ItemIcon ofItem(ItemLike item) {
        return new ItemIcon(new ItemStack(item));
    }

    @Override
    public ItemStack getFallback() {
        return item;
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.ITEM.get();
    }
}
