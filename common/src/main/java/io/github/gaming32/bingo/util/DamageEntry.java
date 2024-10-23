package io.github.gaming32.bingo.util;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

public record DamageEntry(
    @Nullable EntityType<?> entityType,
    @Nullable EntityType<?> directEntityType,
    Holder<DamageType> damageType
) {
    @Override
    public int hashCode() {
        return 31 * (31 * System.identityHashCode(entityType) + System.identityHashCode(directEntityType)) + BingoUtil.<DamageType>holderStrategy().hashCode(damageType);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DamageEntry that)) {
            return false;
        }
        return entityType == that.entityType && directEntityType == that.directEntityType && BingoUtil.<DamageType>holderStrategy().equals(damageType, that.damageType);
    }

    @Nullable
    public CompoundTag toNbt() {
        var key = damageType.unwrapKey();
        if (key.isEmpty()) {
            return null;
        }

        CompoundTag entryTag = new CompoundTag();
        entryTag.putString("damageType", key.get().location().toString());
        if (entityType != null) {
            entryTag.putString("entityType", BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());
        }
        if (directEntityType != null) {
            entryTag.putString("directEntityType", BuiltInRegistries.ENTITY_TYPE.getKey(directEntityType).toString());
        }

        return entryTag;
    }

    @Nullable
    public static DamageEntry fromNbt(RegistryAccess registryAccess, CompoundTag entryTag) {
        ResourceLocation location = ResourceLocation.tryParse(entryTag.getString("damageType"));
        if (location == null) {
            return null;
        }
        ResourceKey<DamageType> key = ResourceKey.create(Registries.DAMAGE_TYPE, location);
        var holder = registryAccess.lookupOrThrow(Registries.DAMAGE_TYPE).get(key);
        if (holder.isEmpty()) {
            return null;
        }
        EntityType<?> entityType = null;
        if (entryTag.contains("entityType", Tag.TAG_STRING)) {
            ResourceLocation entityKey = ResourceLocation.tryParse(entryTag.getString("entityType"));
            if (entityKey != null) {
                entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(entityKey).orElse(null);
            }
        }

        EntityType<?> directEntityType = null;
        if (entryTag.contains("directEntityType", Tag.TAG_STRING)) {
            ResourceLocation entityKey = ResourceLocation.tryParse(entryTag.getString("directEntityType"));
            if (entityKey != null) {
                directEntityType = BuiltInRegistries.ENTITY_TYPE.getOptional(entityKey).orElse(null);
            }
        }

        return new DamageEntry(entityType, directEntityType, holder.get());
    }
}
