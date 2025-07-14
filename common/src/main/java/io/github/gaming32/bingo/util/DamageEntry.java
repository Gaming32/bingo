package io.github.gaming32.bingo.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;

import java.util.Optional;

public record DamageEntry(
    Optional<EntityType<?>> entityType,
    Optional<EntityType<?>> directEntityType,
    Holder<DamageType> damageType
) {
    // TODO: Replace these names with snake case. Only pain point is that requires us to inject to Vanilla DFU to convert it
    public static final Codec<DamageEntry> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            EntityType.CODEC.lenientOptionalFieldOf("entityType").forGetter(DamageEntry::entityType),
            EntityType.CODEC.lenientOptionalFieldOf("directEntityType").forGetter(DamageEntry::directEntityType),
            DamageType.CODEC.fieldOf("damageType").forGetter(DamageEntry::damageType)
        ).apply(instance, DamageEntry::new)
    );

    @Override
    public int hashCode() {
        return 31 * (31 * System.identityHashCode(entityType) + System.identityHashCode(directEntityType)) + BingoUtil.<DamageType>holderStrategy().hashCode(damageType);
    }

    @Override
    public boolean equals(Object o) {
        //noinspection DeconstructionCanBeUsed
        if (!(o instanceof DamageEntry that)) {
            return false;
        }
        return entityType == that.entityType &&
               directEntityType == that.directEntityType &&
               BingoUtil.<DamageType>holderStrategy().equals(damageType, that.damageType);
    }
}
