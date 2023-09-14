package io.github.gaming32.bingo.util;

import net.minecraft.Optionull;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public record DamageInstance(
    ResourceKey<DamageType> type,
    @Nullable
    UUID causingEntity,
    @Nullable
    UUID directEntity,
    @Nullable
    Vec3 damageSourcePosition
) {
    public DamageInstance(DamageSource damageSource) {
        this(
            damageSource.typeHolder().unwrapKey().orElseThrow(() -> new IllegalArgumentException("Unregistered damage source")),
            Optionull.map(damageSource.getEntity(), Entity::getUUID),
            Optionull.map(damageSource.getDirectEntity(), Entity::getUUID),
            damageSource.sourcePositionRaw()
        );
    }

    public DamageInstance(CompoundTag nbt) {
        this(
            ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(nbt.getString("type"))),
            nbt.hasUUID("causing_entity") ? nbt.getUUID("causing_entity") : null,
            nbt.hasUUID("direct_entity") ? nbt.getUUID("direct_entity") : null,
            deserializePosition(nbt)
        );
    }

    @Nullable
    private static Vec3 deserializePosition(CompoundTag nbt) {
        if (!nbt.contains("position", Tag.TAG_LIST)) {
            return null;
        }
        ListTag list = nbt.getList("position", Tag.TAG_DOUBLE);
        if (list.size() != 3) {
            return null;
        }
        return new Vec3(list.getDouble(0), list.getDouble(1), list.getDouble(2));
    }

    public CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("type", type.location().toString());
        if (causingEntity != null) {
            nbt.putUUID("causing_entity", causingEntity);
        }
        if (directEntity != null) {
            nbt.putUUID("direct_entity", directEntity);
        }
        if (damageSourcePosition != null) {
            ListTag positionTag = new ListTag();
            positionTag.add(DoubleTag.valueOf(damageSourcePosition.x));
            positionTag.add(DoubleTag.valueOf(damageSourcePosition.y));
            positionTag.add(DoubleTag.valueOf(damageSourcePosition.z));
            nbt.put("position", positionTag);
        }
        return nbt;
    }

    @Nullable
    public DamageSource getDamageSource(MinecraftServer server) {
        Optional<Holder.Reference<DamageType>> damageType = server.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolder(type);
        if (damageType.isEmpty()) {
            return null;
        }

        Entity causingEntity = null;
        Entity directEntity = null;
        if (this.causingEntity != null || this.directEntity != null) {
            for (ServerLevel level : server.getAllLevels()) {
                if (causingEntity == null && this.causingEntity != null) {
                    causingEntity = level.getEntity(this.causingEntity);
                }
                if (directEntity == null && this.directEntity != null) {
                    directEntity = level.getEntity(this.directEntity);
                }

                // check if we have obtained both the causing entity and the direct entity
                if ((causingEntity == null) == (this.causingEntity == null) && (directEntity == null) == (this.directEntity == null)) {
                    break;
                }
            }
        }

        if (causingEntity != null || directEntity != null) {
            return new DamageSource(damageType.get(), causingEntity, directEntity);
        } else {
            return new DamageSource(damageType.get(), damageSourcePosition);
        }
    }
}
