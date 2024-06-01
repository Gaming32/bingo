package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.util.BingoUtil;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.TagPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class AdjacentPaintingTrigger extends SimpleCriterionTrigger<AdjacentPaintingTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Painting painting) {
        trigger(player, instance -> instance.matches(player, painting));
    }

    public static Builder builder() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<ContextAwarePredicate> placedPainting,
        Optional<TagPredicate<PaintingVariant>> paintingVariant,
        MinMaxBounds.Ints count
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("placed_painting").forGetter(TriggerInstance::placedPainting),
                TagPredicate.codec(Registries.PAINTING_VARIANT).optionalFieldOf("painting_variant").forGetter(TriggerInstance::paintingVariant),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("count", MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::count)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(ServerPlayer player, Painting painting) {
            if (placedPainting.isPresent() && !placedPainting.get().matches(EntityPredicate.createContext(player, painting))) {
                return false;
            }
            ObjectSet<Holder<PaintingVariant>> seenVariants = new ObjectOpenCustomHashSet<>(BingoUtil.holderStrategy());
            int count = countAdjacentPaintings(painting, seenVariants);
            return this.count.matches(count);
        }

        private int countAdjacentPaintings(Painting painting, ObjectSet<Holder<PaintingVariant>> seenVariants) {
            if (!seenVariants.add(painting.getVariant())) {
                return 0;
            }

            int count = 1;
            for (Painting otherPainting : painting.level().getEntitiesOfClass(Painting.class, painting.getBoundingBox().inflate(0.25))) {
                if (otherPainting != painting) {
                    if (paintingVariant.isEmpty() || paintingVariant.get().matches(painting.getVariant())) {
                        count += countAdjacentPaintings(otherPainting, seenVariants);
                    }
                }
            }

            return count;
        }

        @Override
        public void validate(CriterionValidator criterionValidator) {
            SimpleInstance.super.validate(criterionValidator);
            criterionValidator.validateEntity(placedPainting, ".placed_painting");
        }
    }

    public static final class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private Optional<ContextAwarePredicate> placedPainting = Optional.empty();
        private Optional<TagPredicate<PaintingVariant>> paintingVariant = Optional.empty();
        private MinMaxBounds.Ints count = MinMaxBounds.Ints.ANY;

        private Builder() {
        }

        public Builder player(EntityPredicate player) {
            return player(EntityPredicate.wrap(player));
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.of(player);
            return this;
        }

        public Builder placedPainting(EntityPredicate placedPainting) {
            return placedPainting(EntityPredicate.wrap(placedPainting));
        }

        public Builder placedPainting(ContextAwarePredicate placedPainting) {
            this.placedPainting = Optional.of(placedPainting);
            return this;
        }

        public Builder paintingVariant(TagKey<PaintingVariant> paintingVariant) {
            return paintingVariant(TagPredicate.is(paintingVariant));
        }

        public Builder paintingVariant(TagPredicate<PaintingVariant> paintingVariant) {
            this.paintingVariant = Optional.of(paintingVariant);
            return this;
        }

        public Builder count(MinMaxBounds.Ints count) {
            this.count = count;
            return this;
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.ADJACENT_PAINTING.get().createCriterion(
                new TriggerInstance(player, placedPainting, paintingVariant, count)
            );
        }
    }
}
