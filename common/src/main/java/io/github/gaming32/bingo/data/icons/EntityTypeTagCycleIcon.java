package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;

import java.util.Objects;
import java.util.stream.StreamSupport;

public record EntityTypeTagCycleIcon(TagKey<EntityType<?>> tag, int count) implements GoalIcon {
    public static final MapCodec<EntityTypeTagCycleIcon> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            TagKey.codec(Registries.ENTITY_TYPE).fieldOf("tag").forGetter(EntityTypeTagCycleIcon::tag),
            Codec.INT.optionalFieldOf("count", 1).forGetter(EntityTypeTagCycleIcon::count)
        ).apply(instance, EntityTypeTagCycleIcon::new)
    );

    public EntityTypeTagCycleIcon(TagKey<EntityType<?>> tag) {
        this(tag, 1);
    }

    @Override
    @SuppressWarnings("deprecation")
    public ItemStack item() {
        return new ItemStack(
            StreamSupport.stream(BuiltInRegistries.ENTITY_TYPE.getTagOrEmpty(tag).spliterator(), false)
                .map(holder -> SpawnEggItem.byId(holder.value()))
                .filter(Objects::nonNull)
                .findFirst()
                .map(Item::builtInRegistryHolder)
                .orElseGet(Items.AIR::builtInRegistryHolder),
            count
        );
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.ENTITY_TYPE_TAG_CYCLE.get();
    }
}
