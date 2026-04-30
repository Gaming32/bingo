package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.util.BingoCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;

public record SpriteIcon(Identifier sprite, ItemStackTemplate item) implements GoalIcon.WithoutContext {
    public static final MapCodec<SpriteIcon> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Identifier.CODEC.fieldOf("sprite").forGetter(SpriteIcon::sprite),
            BingoCodecs.LENIENT_ITEM_STACK_TEMPLATE.fieldOf("item").forGetter(SpriteIcon::item)
        ).apply(instance, SpriteIcon::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SpriteIcon> STREAM_CODEC = StreamCodec.composite(
        Identifier.STREAM_CODEC, SpriteIcon::sprite,
        ItemStackTemplate.STREAM_CODEC, SpriteIcon::item,
        SpriteIcon::new
    );

    @Override
    public ItemStackTemplate getFallback() {
        return item;
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.SPRITE.get();
    }
}
