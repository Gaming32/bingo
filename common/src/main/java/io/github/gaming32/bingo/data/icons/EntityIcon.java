package io.github.gaming32.bingo.data.icons;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import java.util.Objects;

public record EntityIcon(EntityType<?> entity, CompoundTag data, ItemStack item) implements GoalIcon {
    public static final MapCodec<EntityIcon> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(EntityIcon::entity),
            ExtraCodecs.strictOptionalField(CompoundTag.CODEC, "data", new CompoundTag()).forGetter(EntityIcon::data),
            ItemStack.CODEC.fieldOf("item").forGetter(EntityIcon::item)
        ).apply(instance, EntityIcon::new)
    );

    public static EntityIcon ofSpawnEgg(EntityType<?> entity, CompoundTag data, int count) {
        return new EntityIcon(entity, data, new ItemStack(
            Objects.requireNonNull(SpawnEggItem.byId(entity), () -> "entity \"" + entity + "\" doesn't have a spawn egg"),
            count
        ));
    }

    public static EntityIcon ofSpawnEgg(EntityType<?> entity, CompoundTag data) {
        return ofSpawnEgg(entity, data, 1);
    }

    public static EntityIcon ofSpawnEgg(EntityType<?> entity, int count) {
        return ofSpawnEgg(entity, new CompoundTag(), count);
    }

    public static EntityIcon ofSpawnEgg(EntityType<?> entity) {
        return ofSpawnEgg(entity, new CompoundTag(), 1);
    }

    public static EntityIcon ofSpawnEgg(Entity entity, int count) {
        return ofSpawnEgg(entity.getType(), entity.saveWithoutId(new CompoundTag()), count);
    }

    public static EntityIcon ofSpawnEgg(Entity entity) {
        return ofSpawnEgg(entity, 1);
    }

    public static EntityIcon of(EntityType<?> entity, ItemStack item) {
        return new EntityIcon(entity, new CompoundTag(), item);
    }

    public static EntityIcon of(Entity entity, ItemStack item) {
        return new EntityIcon(entity.getType(), entity.saveWithoutId(new CompoundTag()), item);
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.ENTITY.get();
    }
}
