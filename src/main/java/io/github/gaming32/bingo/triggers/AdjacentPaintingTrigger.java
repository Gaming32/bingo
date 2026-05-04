package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.subpredicates.PaintingPredicate;
import io.github.gaming32.bingo.util.BingoUtil;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContextSource;

import java.util.Optional;

public class AdjacentPaintingTrigger extends SimpleCriterionTrigger<AdjacentPaintingTrigger.TriggerInstance> {
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
        Optional<ContextAwarePredicate> adjacentPaintings,
        MinMaxBounds.Ints count
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("placed_painting").forGetter(TriggerInstance::placedPainting),
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("adjacent_paintings").forGetter(TriggerInstance::adjacentPaintings),
                MinMaxBounds.Ints.CODEC.optionalFieldOf("count", MinMaxBounds.Ints.ANY).forGetter(TriggerInstance::count)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(ServerPlayer player, Painting painting) {
            if (placedPainting.isPresent() && !placedPainting.get().matches(EntityPredicate.createContext(player, painting))) {
                return false;
            }
            ObjectSet<Holder<PaintingVariant>> seenVariants = new ObjectOpenCustomHashSet<>(BingoUtil.holderStrategy());
            int count = countAdjacentPaintings(player, painting, seenVariants);
            return this.count.matches(count);
        }

        private int countAdjacentPaintings(
            ServerPlayer player,
            Painting painting,
            ObjectSet<Holder<PaintingVariant>> seenVariants
        ) {
            if (!seenVariants.add(painting.getVariant())) {
                return 0;
            }

            int count = 1;
            for (Painting otherPainting : painting.level().getEntitiesOfClass(Painting.class, painting.getBoundingBox().inflate(0.25))) {
                if (
                    otherPainting != painting && (
                        adjacentPaintings.isEmpty() ||
                        adjacentPaintings.get().matches(EntityPredicate.createContext(player, otherPainting))
                    )
                ) {
                    count += countAdjacentPaintings(player, otherPainting, seenVariants);
                }
            }

            return count;
        }

        @Override
        public void validate(ValidationContextSource validator) {
            SimpleInstance.super.validate(validator);
            Validatable.validate(validator.entityContext(), "placed_painting", placedPainting);
            Validatable.validate(validator.entityContext(), "adjacent_paintings", adjacentPaintings);
        }
    }

    public static final class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private Optional<ContextAwarePredicate> placedPainting = Optional.empty();
        private Optional<ContextAwarePredicate> adjacentPaintings = Optional.empty();
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

        public Builder placedPainting(ContextAwarePredicate placedPainting) {
            this.placedPainting = Optional.of(placedPainting);
            return this;
        }

        public Builder placedPainting(EntityPredicate placedPainting) {
            return placedPainting(EntityPredicate.wrap(placedPainting));
        }

        public Builder placedPainting(PaintingPredicate placedPainting) {
            return placedPainting(EntityPredicate.Builder.entity()
                .subPredicate(placedPainting)
                .build()
            );
        }

        public Builder adjacentPaintings(ContextAwarePredicate adjacentPaintings) {
            this.adjacentPaintings = Optional.of(adjacentPaintings);
            return this;
        }

        public Builder adjacentPaintings(EntityPredicate adjacentPaintings) {
            return adjacentPaintings(EntityPredicate.wrap(adjacentPaintings));
        }

        public Builder adjacentPaintings(PaintingPredicate adjacentPaintings) {
            return adjacentPaintings(EntityPredicate.Builder.entity()
                .subPredicate(adjacentPaintings)
                .build()
            );
        }

        public Builder count(MinMaxBounds.Ints count) {
            this.count = count;
            return this;
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.ADJACENT_PAINTING.get().createCriterion(
                new TriggerInstance(player, placedPainting, adjacentPaintings, count)
            );
        }
    }
}
