package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EntityTrigger extends SimpleCriterionTrigger<EntityTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Entity entity) {
        final LootContext mobContext = EntityPredicate.createContext(player, entity);
        trigger(player, instance -> instance.matches(mobContext));
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<ContextAwarePredicate> entity
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(TriggerInstance::entity)
            ).apply(instance, TriggerInstance::new)
        );

        public static Criterion<TriggerInstance> brokeCrossbow(EntityPredicate mob) {
            return BingoTriggers.MOB_BROKE_CROSSBOW.get().createCriterion(new TriggerInstance(
                Optional.empty(), EntityPredicate.wrap(Optional.ofNullable(mob))
            ));
        }

        public static Criterion<TriggerInstance> stunnedRavager() {
            return BingoTriggers.STUN_RAVAGER.get().createCriterion(new TriggerInstance(Optional.empty(), Optional.empty()));
        }

        public boolean matches(LootContext mob) {
            return this.entity.isEmpty() || this.entity.get().matches(mob);
        }

        @Override
        public void validate(CriterionValidator criterionValidator) {
            SimpleInstance.super.validate(criterionValidator);
            criterionValidator.validateEntity(entity, ".entity");
        }
    }
}
