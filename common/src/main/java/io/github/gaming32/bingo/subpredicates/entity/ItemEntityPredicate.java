package io.github.gaming32.bingo.subpredicates.entity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.ext.ItemEntityExt;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record ItemEntityPredicate(
    Optional<ItemPredicate> item,
    MinMaxBounds.Ints age,
    Optional<EntityPredicate> droppedBy
) implements EntitySubPredicate {
    public static final MapCodec<ItemEntityPredicate> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            ItemPredicate.CODEC.optionalFieldOf("item").forGetter(ItemEntityPredicate::item),
            MinMaxBounds.Ints.CODEC.optionalFieldOf("age", MinMaxBounds.Ints.ANY).forGetter(ItemEntityPredicate::age),
            EntityPredicate.CODEC.optionalFieldOf("dropped_by").forGetter(ItemEntityPredicate::droppedBy)
        ).apply(instance, ItemEntityPredicate::new)
    );

    public static ItemEntityPredicate item(ItemPredicate item) {
        return new ItemEntityPredicate(Optional.of(item), MinMaxBounds.Ints.ANY, Optional.empty());
    }

    public static ItemEntityPredicate droppedBy(Optional<ItemPredicate> item, Optional<EntityPredicate> droppedBy) {
        return new ItemEntityPredicate(item, MinMaxBounds.Ints.ANY, droppedBy);
    }

    @Override
    public boolean matches(Entity entity, ServerLevel level, @Nullable Vec3 position) {
        if (!(entity instanceof ItemEntity itemEntity)) {
            return false;
        }
        if (item.isPresent() && !item.get().test(itemEntity.getItem())) {
            return false;
        }
        if (!age.matches(itemEntity.getAge())) {
            return false;
        }
        if (droppedBy.isPresent() && !droppedBy.get().matches(level, position, ((ItemEntityExt)itemEntity).bingo$getDroppedBy())) {
            return false;
        }
        return true;
    }

    @NotNull
    @Override
    public MapCodec<ItemEntityPredicate> codec() {
        return CODEC;
    }
}
