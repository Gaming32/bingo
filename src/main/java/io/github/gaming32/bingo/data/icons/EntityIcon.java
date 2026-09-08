package io.github.gaming32.bingo.data.icons;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.util.BingoCodecs;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.storage.TagValueOutput;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public record EntityIcon(EntityType<?> entity, CompoundTag data, ItemStackTemplate item) implements GoalIcon.WithoutContext {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final MapCodec<EntityIcon> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(EntityIcon::entity),
            CompoundTag.CODEC.optionalFieldOf("data", new CompoundTag()).forGetter(EntityIcon::data),
            BingoCodecs.LENIENT_ITEM_STACK_TEMPLATE.fieldOf("item").forGetter(EntityIcon::item)
        ).apply(instance, EntityIcon::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityIcon> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.registry(Registries.ENTITY_TYPE), EntityIcon::entity,
        ByteBufCodecs.COMPOUND_TAG, EntityIcon::data,
        ItemStackTemplate.STREAM_CODEC, EntityIcon::item,
        EntityIcon::new
    );

    public static EntityIcon ofSpawnEgg(EntityType<?> entity, CompoundTag data, int count) {
        Item spawnEggItem = getSpawnEggItem(entity);
        if (spawnEggItem == null) {
            throw new IllegalArgumentException("entity \"" + entity + "\" doesn't have a spawn egg");
        }
        return new EntityIcon(entity, data, new ItemStackTemplate(spawnEggItem, count));
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

    public static EntityIcon of(EntityType<?> entity, ItemStackTemplate item) {
        return new EntityIcon(entity, new CompoundTag(), item);
    }

    public static EntityIcon of(Entity entity, ItemStackTemplate item) {
        try (ProblemReporter.ScopedCollector collector = new ProblemReporter.ScopedCollector(new ProblemReporter.FieldPathElement("data"), LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(collector, entity.registryAccess());
            entity.saveWithoutId(output);
            return new EntityIcon(entity.getType(), output.buildResult(), item);
        }
    }

    @Override
    public ItemStackTemplate getFallback() {
        return item;
    }

    @Override
    public GoalIconType<?> type() {
        return GoalIconType.ENTITY.get();
    }

    @Nullable
    public static Item getSpawnEggItem(EntityType<?> entityType) {
        Identifier name = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        Identifier spawnEggName = name.withSuffix("_spawn_egg");
        var holder = BuiltInRegistries.ITEM.get(spawnEggName);
        return holder.map(Holder.Reference::value).orElse(null);
    }
}
