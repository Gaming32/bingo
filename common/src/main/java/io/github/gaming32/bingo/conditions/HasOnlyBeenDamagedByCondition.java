package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.ext.LivingEntityExt;
import io.github.gaming32.bingo.util.BingoUtil;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public record HasOnlyBeenDamagedByCondition(
    Optional<EntityType<?>> entityType,
    Optional<TagPredicate<EntityType<?>>> entityTypeTag,
    Optional<EntityType<?>> directEntityType,
    Optional<TagPredicate<EntityType<?>>> directEntityTypeTag,
    Optional<ResourceKey<DamageType>> damageType,
    Optional<TagPredicate<DamageType>> damageTypeTag
) implements LootItemCondition {

    public static final Codec<HasOnlyBeenDamagedByCondition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ExtraCodecs.strictOptionalField(BuiltInRegistries.ENTITY_TYPE.byNameCodec(), "entity_type").forGetter(HasOnlyBeenDamagedByCondition::entityType),
            ExtraCodecs.strictOptionalField(TagPredicate.codec(Registries.ENTITY_TYPE), "entity_type_tag").forGetter(HasOnlyBeenDamagedByCondition::entityTypeTag),
            ExtraCodecs.strictOptionalField(BuiltInRegistries.ENTITY_TYPE.byNameCodec(), "direct_entity_type").forGetter(HasOnlyBeenDamagedByCondition::directEntityType),
            ExtraCodecs.strictOptionalField(TagPredicate.codec(Registries.ENTITY_TYPE), "direct_entity_type_tag").forGetter(HasOnlyBeenDamagedByCondition::directEntityTypeTag),
            ExtraCodecs.strictOptionalField(ResourceKey.codec(Registries.DAMAGE_TYPE), "damage_type").forGetter(HasOnlyBeenDamagedByCondition::damageType),
            ExtraCodecs.strictOptionalField(TagPredicate.codec(Registries.DAMAGE_TYPE), "damage_type_tag").forGetter(HasOnlyBeenDamagedByCondition::damageTypeTag)
        ).apply(instance, HasOnlyBeenDamagedByCondition::new)
    );

    @NotNull
    @Override
    public LootItemConditionType getType() {
        return BingoConditions.HAS_ONLY_BEEN_DAMAGED_BY.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.getParam(LootContextParams.THIS_ENTITY);
        return entity instanceof LivingEntityExt living && living.bingo$hasOnlyBeenDamagedBy(damageEntry -> {
            if (this.entityType.isPresent() && damageEntry.entityType() != this.entityType.get()) {
                return false;
            }
            if (this.entityTypeTag.isPresent() && (damageEntry.entityType() == null || !this.entityTypeTag.get().matches(BingoUtil.getBuiltInHolder(BuiltInRegistries.ENTITY_TYPE, damageEntry.entityType())))) {
                return false;
            }
            if (this.directEntityType.isPresent() && damageEntry.directEntityType() != this.directEntityType.get()) {
                return false;
            }
            if (this.directEntityTypeTag.isPresent() && (damageEntry.directEntityType() == null || !this.directEntityTypeTag.get().matches(BingoUtil.getBuiltInHolder(BuiltInRegistries.ENTITY_TYPE, damageEntry.directEntityType())))) {
                return false;
            }
            if (this.damageType.isPresent() && !damageEntry.damageType().is(this.damageType.get())) {
                return false;
            }
            if (this.damageTypeTag.isPresent() && !this.damageTypeTag.get().matches(damageEntry.damageType())) {
                return false;
            }
            return true;
        });
    }

    @NotNull
    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.THIS_ENTITY);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Optional<EntityType<?>> entityType = Optional.empty();
        private Optional<TagPredicate<EntityType<?>>> entityTypeTag = Optional.empty();
        private Optional<EntityType<?>> directEntityType = Optional.empty();
        private Optional<TagPredicate<EntityType<?>>> directEntityTypeTag = Optional.empty();
        private Optional<ResourceKey<DamageType>> damageType = Optional.empty();
        private Optional<TagPredicate<DamageType>> damageTypeTag = Optional.empty();

        private Builder() {
        }

        public Builder entityType(EntityType<?> entityType) {
            this.entityType = Optional.of(entityType);
            return this;
        }

        public Builder entityTypeTag(TagKey<EntityType<?>> entityTypeTag) {
            return entityTypeTag(TagPredicate.is(entityTypeTag));
        }

        public Builder entityTypeTag(TagPredicate<EntityType<?>> entityTypeTag) {
            this.entityTypeTag = Optional.of(entityTypeTag);
            return this;
        }

        public Builder directEntityType(EntityType<?> directEntityType) {
            this.directEntityType = Optional.of(directEntityType);
            return this;
        }

        public Builder directEntityTypeTag(TagKey<EntityType<?>> directEntityTypeTag) {
            return directEntityTypeTag(TagPredicate.is(directEntityTypeTag));
        }

        public Builder directEntityTypeTag(TagPredicate<EntityType<?>> directEntityTypeTag) {
            this.directEntityTypeTag = Optional.of(directEntityTypeTag);
            return this;
        }

        public Builder damageType(ResourceKey<DamageType> damageType) {
            this.damageType = Optional.of(damageType);
            return this;
        }

        public Builder damageTypeTag(TagKey<DamageType> damageTypeTag) {
            return damageTypeTag(TagPredicate.is(damageTypeTag));
        }

        public Builder damageTypeTag(TagPredicate<DamageType> damageTypeTag) {
            this.damageTypeTag = Optional.of(damageTypeTag);
            return this;
        }

        public HasOnlyBeenDamagedByCondition build() {
            return new HasOnlyBeenDamagedByCondition(entityType, entityTypeTag, directEntityType, directEntityTypeTag, damageType, damageTypeTag);
        }
    }
}
