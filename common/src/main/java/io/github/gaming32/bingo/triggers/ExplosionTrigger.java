package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.mixin.common.ExplosionAccessor;
import io.github.gaming32.bingo.util.CustomEnumCodec;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ExplosionTrigger extends SimpleCriterionTrigger<ExplosionTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, ServerLevel level, Explosion explosion) {
        final Optional<LootContext> source = Optional.ofNullable(explosion.source).map(s -> EntityPredicate.createContext(player, s));
        final Vec3 location = explosion.center();
        trigger(player, instance -> instance.matches(source, level, location, explosion));
    }

    public static Builder builder() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<ContextAwarePredicate> source,
        Optional<LocationPredicate> location,
        MinMaxBounds.Doubles radius,
        Optional<DamageSourcePredicate> damageSource,
        Optional<Explosion.BlockInteraction> blockInteraction
    ) implements SimpleInstance {
        public static final CustomEnumCodec<Explosion.BlockInteraction> EXPLOSION_TYPE_PREDICATE = CustomEnumCodec.of(Explosion.BlockInteraction.class);
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "source").forGetter(TriggerInstance::source),
                ExtraCodecs.strictOptionalField(LocationPredicate.CODEC, "location").forGetter(TriggerInstance::location),
                ExtraCodecs.strictOptionalField(MinMaxBounds.Doubles.CODEC, "radius", MinMaxBounds.Doubles.ANY).forGetter(TriggerInstance::radius),
                ExtraCodecs.strictOptionalField(DamageSourcePredicate.CODEC, "damage_source").forGetter(TriggerInstance::damageSource),
                ExtraCodecs.strictOptionalField(EXPLOSION_TYPE_PREDICATE.codec(), "block_interaction").forGetter(TriggerInstance::blockInteraction)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(Optional<LootContext> source, ServerLevel level, Vec3 location, Explosion explosion) {
            if (this.source.isPresent() && (source.isEmpty() || !this.source.get().matches(source.get()))) {
                return false;
            }
            if (this.location.isPresent() && !this.location.get().matches(level, location.x, location.y, location.z)) {
                return false;
            }
            if (this.damageSource.isPresent() && !this.damageSource.get().matches(level, location, ((ExplosionAccessor)explosion).getDamageSource())) {
                return false;
            }
            if (this.blockInteraction.isPresent() && this.blockInteraction.get() != explosion.getBlockInteraction()) {
                return false;
            }
            return true;
        }
    }

    public static final class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private Optional<ContextAwarePredicate> source = Optional.empty();
        private Optional<LocationPredicate> location = Optional.empty();
        private MinMaxBounds.Doubles radius = MinMaxBounds.Doubles.ANY;
        private Optional<DamageSourcePredicate> damageSource = Optional.empty();
        private Optional<Explosion.BlockInteraction> blockInteraction = Optional.empty();

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.of(player);
            return this;
        }

        public Builder player(EntityPredicate.Builder player) {
            return player(EntityPredicate.wrap(player));
        }

        public Builder source(ContextAwarePredicate source) {
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
