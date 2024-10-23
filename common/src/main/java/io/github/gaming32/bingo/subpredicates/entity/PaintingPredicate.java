package io.github.gaming32.bingo.subpredicates.entity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record PaintingPredicate(
    MinMaxBounds.Ints width,
    MinMaxBounds.Ints height,
    MinMaxBounds.Ints area,
    Optional<HolderSet<PaintingVariant>> variant
) implements EntitySubPredicate {
    public static final MapCodec<PaintingPredicate> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            MinMaxBounds.Ints.CODEC.optionalFieldOf("width", MinMaxBounds.Ints.ANY).forGetter(PaintingPredicate::width),
            MinMaxBounds.Ints.CODEC.optionalFieldOf("height", MinMaxBounds.Ints.ANY).forGetter(PaintingPredicate::height),
            MinMaxBounds.Ints.CODEC.optionalFieldOf("area", MinMaxBounds.Ints.ANY).forGetter(PaintingPredicate::area),
            RegistryCodecs.homogeneousList(Registries.PAINTING_VARIANT)
                .optionalFieldOf("variant")
                .forGetter(PaintingPredicate::variant)
        ).apply(instance, PaintingPredicate::new)
    );

    @NotNull
    @Override
    public MapCodec<? extends EntitySubPredicate> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(Entity entity, ServerLevel level, @Nullable Vec3 position) {
        if (!(entity instanceof Painting painting)) {
            return false;
        }
        final var variantHolder = painting.getVariant();
        final var variant = variantHolder.value();
        if (!width.matches(variant.width())) {
            return false;
        }
        if (!height.matches(variant.height())) {
            return false;
        }
        if (!area.matches(variant.area())) {
            return false;
        }
        if (this.variant.isPresent() && !this.variant.get().contains(variantHolder)) {
            return false;
        }
        return true;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private MinMaxBounds.Ints width = MinMaxBounds.Ints.ANY;
        private MinMaxBounds.Ints height = MinMaxBounds.Ints.ANY;
        private MinMaxBounds.Ints area = MinMaxBounds.Ints.ANY;
        private Optional<HolderSet<PaintingVariant>> variant = Optional.empty();

        private Builder() {
        }

        public Builder width(MinMaxBounds.Ints width) {
            this.width = width;
            return this;
        }

        public Builder height(MinMaxBounds.Ints height) {
            this.height = height;
            return this;
        }

        public Builder area(MinMaxBounds.Ints area) {
            this.area = area;
            return this;
        }

        public Builder variant(HolderSet<PaintingVariant> variant) {
            this.variant = Optional.of(variant);
            return this;
        }

        @SafeVarargs
        public final Builder variant(HolderLookup<PaintingVariant> lookup, ResourceKey<PaintingVariant>... variants) {
            return variant(HolderSet.direct(lookup::getOrThrow, variants));
        }

        public Builder variant(HolderLookup<PaintingVariant> lookup, TagKey<PaintingVariant> tag) {
            return variant(lookup.getOrThrow(tag));
        }

        public PaintingPredicate build() {
            return new PaintingPredicate(width, height, area, variant);
        }
    }
}
