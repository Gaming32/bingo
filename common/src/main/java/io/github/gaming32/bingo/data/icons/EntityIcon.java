package io.github.gaming32.bingo.data.icons;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.util.BingoCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.storage.TagValueOutput;
import org.slf4j.Logger;

import java.util.Objects;

public record EntityIcon(EntityType<?> entity, CompoundTag data, ItemStack item) implements GoalIcon.WithoutContext {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final MapCodec<EntityIcon> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(EntityIcon::entity),
            CompoundTag.CODEC.optionalFieldOf("data", new CompoundTag()).forGetter(EntityIcon::data),
            BingoCodecs.LENIENT_ITEM_STACK.fieldOf("item").forGetter(EntityIcon::item)
        ).apply(instance, EntityIcon::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityIcon> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.registry(Registries.ENTITY_TYPE), EntityIcon::entity,
        ByteBufCodecs.COMPOUND_TAG, EntityIcon::data,
        ItemStack.STREAM_CODEC, EntityIcon::item,
        EntityIcon::new
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
        try (ProblemReporter.ScopedCollector collector = new ProblemReporter.ScopedCollector(new ProblemReporter.FieldPathElement("data"), LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(collector, entity.registryAccess());
            entity.saveWithoutId(output);
            return ofSpawnEgg(entity.getType(), output.buildResult(), count);
        }
    }

    public static EntityIcon ofSpawnEgg(Entity entity) {
        return ofSpawnEgg(entity, 1);
    }

    public static EntityIcon of(EntityType<?> entity, ItemStack item) {
        return new EntityIcon(entity, new CompoundTag(), item);
    }

    public static EntityIcon of(Entity entity, ItemStack item) {
        try (ProblemReporter.ScopedCollector collector = new ProblemReporter.ScopedCollector(new ProblemReporter.FieldPathElement("data"), LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(collector, entity.registryAccess());
            entity.saveWithoutId(output);
            return new EntityIcon(entity.getType(), output.buildResult(), item);
        }
    }

    @Override
    public ItemStack getFallback() {
        return item;
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.ENTITY.get();
    }
}
