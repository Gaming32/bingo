package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ZombifyPigTrigger extends SimpleCriterionTrigger<ZombifyPigTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Pig pig, Entity zombifiedPiglin, boolean direct) {
        final LootContext pigContext = EntityPredicate.createContext(player, pig);
        final LootContext zombifiedPiglinContext = EntityPredicate.createContext(player, zombifiedPiglin);
        trigger(player, instance -> instance.matches(pigContext, zombifiedPiglinContext, direct));
    }

    public static Builder zombifyPig() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<ContextAwarePredicate> pig,
        Optional<ContextAwarePredicate> zombifiedPiglin,
        Optional<Boolean> direct
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "pig").forGetter(TriggerInstance::pig),
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "zombified_piglin").forGetter(TriggerInstance::zombifiedPiglin),
                ExtraCodecs.strictOptionalField(Codec.BOOL, "direct").forGetter(TriggerInstance::direct)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(LootContext pig, LootContext zombifiedPiglin, boolean direct) {
            if (this.pig.isPresent() && !this.pig.get().matches(pig)) {
                return false;
            }
            if (this.zombifiedPiglin.isPresent() && !this.zombifiedPiglin.get().matches(zombifiedPiglin)) {
                return false;
            }
            if (this.direct.isPresent() && direct != this.direct.get()) {
                return false;
            }
            return true;
        }
    }

    public static class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private Optional<ContextAwarePredicate> pig = Optional.empty();
        private Optional<ContextAwarePredicate> zombifiedPiglin = Optional.empty();
        private Optional<Boolean> direct = Optional.empty();

        private Builder() {
        }

        public Builder player(ContextAwarePredicate predicate) {
            this.player = Optional.ofNullable(predicate);
            return this;
        }

        public Builder player(EntityPredicate predicate) {
            return player(EntityPredicate.wrap(predicate));
        }

        public Builder pig(ContextAwarePredicate predicate) {
            this.pig = Optional.ofNullable(predicate);
            return this;
        }

        public Builder pig(EntityPredicate predicate) {
            return pig(EntityPredicate.wrap(predicate));
        }

        public Builder zombifiedPiglin(ContextAwarePredicate predicate) {
            this.zombifiedPiglin = Optional.ofNullable(predicate);
            return this;
        }

        public Builder zombifiedPiglin(EntityPredicate predicate) {
            return zombifiedPiglin(EntityPredicate.wrap(predicate));
        }

        public Builder direct(boolean direct) {
            this.direct = Optional.of(direct);
            return this;
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.ZOMBIFY_PIG.createCriterion(
                new TriggerInstance(player, pig, zombifiedPiglin, direct)
            );
        }
    }
}
