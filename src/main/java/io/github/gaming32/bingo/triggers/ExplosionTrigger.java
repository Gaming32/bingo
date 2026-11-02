package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.util.CustomEnumCodec;
import net.minecraft.advancements.predicates.DamageSourcePredicate;
import net.minecraft.advancements.predicates.LocationPredicate;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContextSource;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class ExplosionTrigger extends SimpleCriterionTrigger<ExplosionTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ServerLevel level, Explosion explosion) {
        final Optional<LootContext> source = Optional.ofNullable(explosion.getDirectSourceEntity())
            .map(s -> EntityPredicate.createContext(player, s));
        final Vec3 location = explosion.center();
        trigger(player, instance -> instance.matches(source, level, location, explosion));
    }

    public static Builder builder() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<Holder<LootItemCondition>> player,
        Optional<Holder<LootItemCondition>> source,
        Optional<LocationPredicate> location,
        MinMaxBounds.Doubles radius,
        Optional<DamageSourcePredicate> damageSource,
        Optional<Explosion.BlockInteraction> blockInteraction
    ) implements SimpleInstance {
        public static final CustomEnumCodec<Explosion.BlockInteraction> EXPLOSION_TYPE_PREDICATE = CustomEnumCodec.of(Explosion.BlockInteraction.class);
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                LootItemCondition.CODEC.optionalFieldOf("source").forGetter(TriggerInstance::source),
                LocationPredicate.CODEC.optionalFieldOf("location").forGetter(TriggerInstance::location),
                MinMaxBounds.Doubles.CODEC.optionalFieldOf("radius", MinMaxBounds.Doubles.ANY).forGetter(TriggerInstance::radius),
                DamageSourcePredicate.CODEC.optionalFieldOf("damage_source").forGetter(TriggerInstance::damageSource),
                EXPLOSION_TYPE_PREDICATE.codec().optionalFieldOf("block_interaction").forGetter(TriggerInstance::blockInteraction)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(Optional<LootContext> source, ServerLevel level, Vec3 location, Explosion explosion) {
            if (this.source.isPresent() && (source.isEmpty() || !this.source.get().value().test(source.get()))) {
                return false;
            }
            if (this.location.isPresent() && !this.location.get().matches(level, location.x, location.y, location.z)) {
                return false;
            }
            if (this.damageSource.isPresent() && !this.damageSource.get().matches(level, location, ((ServerExplosion) explosion).getDamageSource())) {
                return false;
            }
            if (this.blockInteraction.isPresent() && this.blockInteraction.get() != explosion.getBlockInteraction()) {
                return false;
            }
            return true;
        }

        @Override
        public void validate(ValidationContextSource validator) {
            SimpleInstance.super.validate(validator);
            Validatable.validateHolder(validator.entityContext(), "source", source);
        }
    }

    public static final class Builder {
        private Optional<Holder<LootItemCondition>> player = Optional.empty();
        private Optional<Holder<LootItemCondition>> source = Optional.empty();
        private Optional<LocationPredicate> location = Optional.empty();
        private MinMaxBounds.Doubles radius = MinMaxBounds.Doubles.ANY;
        private Optional<DamageSourcePredicate> damageSource = Optional.empty();
        private Optional<Explosion.BlockInteraction> blockInteraction = Optional.empty();

        private Builder() {
        }

        public Builder player(Holder<LootItemCondition> player) {
            this.player = Optional.of(player);
            return this;
        }

        public Builder player(EntityPredicate.Builder player) {
            return player(EntityPredicate.wrap(player));
        }

        public Builder source(Holder<LootItemCondition> source) {
            this.source = Optional.of(source);
            return this;
        }

        public Builder source(EntityPredicate.Builder source) {
            return source(EntityPredicate.wrap(source));
        }

        public Builder location(LocationPredicate location) {
            this.location = Optional.of(location);
            return this;
        }

        public Builder radius(MinMaxBounds.Doubles radius) {
            this.radius = radius;
            return this;
        }

        public Builder damageSource(DamageSourcePredicate damageSource) {
            this.damageSource = Optional.of(damageSource);
            return this;
        }

        public Builder blockInteraction(Explosion.BlockInteraction blockInteraction) {
            this.blockInteraction = Optional.of(blockInteraction);
            return this;
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.EXPLOSION.get().createCriterion(
                new TriggerInstance(player, source, location, radius, damageSource, blockInteraction)
            );
        }
    }
}
