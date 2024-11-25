package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.util.BingoCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record SpriteIcon(ResourceLocation sprite, ItemStack fallback) implements GoalIcon.WithoutContext {
    public static final MapCodec<SpriteIcon> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("sprite").forGetter(SpriteIcon::sprite),
            BingoCodecs.LENIENT_ITEM_STACK.fieldOf("fallback").forGetter(SpriteIcon::fallback)
        ).apply(instance, SpriteIcon::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SpriteIcon> STREAM_CODEC = StreamCodec.composite(
        ResourceLocation.STREAM_CODEC, SpriteIcon::sprite,
        ItemStack.STREAM_CODEC, SpriteIcon::fallback,
        SpriteIcon::new
    );

    @Override
    public ItemStack getFallback() {
        return fallback;
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.SPRITE.get();
    }
}
