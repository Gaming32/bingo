package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContextSource;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public class EntityTrigger extends SimpleCriterionTrigger<EntityTrigger.TriggerInstance> {
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Entity entity) {
        final LootContext mobContext = EntityPredicate.createContext(player, entity);
        trigger(player, instance -> instance.matches(mobContext));
    }

    public record TriggerInstance(
        Optional<Holder<LootItemCondition>> player,
        Optional<Holder<LootItemCondition>> entity
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                LootItemCondition.CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                LootItemCondition.CODEC.optionalFieldOf("entity").forGetter(TriggerInstance::entity)
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
            return this.entity.isEmpty() || this.entity.get().value().test(mob);
        }

        @Override
        public void validate(ValidationContextSource validator) {
            SimpleInstance.super.validate(validator);
            Validatable.validateHolder(validator.entityContext(), "entity", entity);
        }
    }
}
