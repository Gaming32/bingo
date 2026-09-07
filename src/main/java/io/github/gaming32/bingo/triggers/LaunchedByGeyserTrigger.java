package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.predicates.ContextAwarePredicate;
import net.minecraft.advancements.predicates.LocationPredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class LaunchedByGeyserTrigger extends SimpleCriterionTrigger<LaunchedByGeyserTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, BlockPos potentSulfurPos) {
        trigger(player, instance -> instance.matches(player, potentSulfurPos));
    }

    public static Builder builder() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<LocationPredicate> potentSulfur
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                LocationPredicate.CODEC.optionalFieldOf("potent_sulfur").forGetter(TriggerInstance::potentSulfur)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(ServerPlayer player, BlockPos potentSulfurPos) {
            return potentSulfur.isEmpty() || potentSulfur.get().matches(player.level(), potentSulfurPos.getX() + 0.5, potentSulfurPos.getY() + 0.5, potentSulfurPos.getZ() + 0.5);
        }
    }

    public static final class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private Optional<LocationPredicate> potentSulfur = Optional.empty();

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.ofNullable(player);
            return this;
        }

        public Builder potentSulfur(LocationPredicate location) {
            this.potentSulfur = Optional.ofNullable(location);
            return this;
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.LAUNCHED_BY_GEYSER.get().createCriterion(
                new TriggerInstance(player, potentSulfur)
            );
        }
    }
}
