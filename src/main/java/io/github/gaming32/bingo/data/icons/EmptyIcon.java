package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public enum EmptyIcon implements GoalIcon.WithoutContext {
    INSTANCE;

    public static final MapCodec<EmptyIcon> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<ByteBuf, EmptyIcon> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public ItemStack getFallback() {
        return Items.STONE.getDefaultInstance();
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.EMPTY.get();
    }
}
