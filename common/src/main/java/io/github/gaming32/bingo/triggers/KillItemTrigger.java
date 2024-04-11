package io.github.gaming32.bingo.triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.subpredicates.ItemEntityPredicate;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.CriterionValidator;
import net.minecraft.advancements.critereon.DamagePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class KillItemTrigger extends SimpleCriterionTrigger<KillItemTrigger.TriggerInstance> {
    @NotNull
    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ItemEntity item, DamageSource source, float amount) {
        if (item.getOwner() instanceof ServerPlayer player) {
            final LootContext itemContext = EntityPredicate.createContext(player, item);
            trigger(player, triggerInstance -> triggerInstance.matches(player, itemContext, source, amount));
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<ContextAwarePredicate> item,
        Optional<DamagePredicate> damage
    ) implements SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(TriggerInstance::player),
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "item").forGetter(TriggerInstance::item),
                ExtraCodecs.strictOptionalField(DamagePredicate.CODEC, "damage").forGetter(TriggerInstance::damage)
            ).apply(instance, TriggerInstance::new)
        );

        public boolean matches(ServerPlayer player, LootContext item, DamageSource source, float amount) {
            if (this.item.isPresent() && !this.item.get().matches(item)) {
                return false;
            }
            if (this.damage.isPresent() && !this.damage.get().matches(player, source, amount, amount, false)) {
                return false;
            }
            return true;
        }

        @Override
        public void validate(CriterionValidator criterionValidator) {
            SimpleInstance.super.validate(criterionValidator);
            criterionValidator.validateEntity(item, ".item");
        }
    }

    public static final class Builder {
        private Optional<ContextAwarePredicate> player = Optional.empty();
        private Optional<ContextAwarePredicate> item = Optional.empty();
        private Optional<DamagePredicate> damage = Optional.empty();

        private Builder() {
        }

        public Builder player(ContextAwarePredicate player) {
            this.player = Optional.ofNullable(player);
            return this;
        }

        public Builder item(ContextAwarePredicate item) {
            this.item = Optional.ofNullable(item);
            return this;
        }

        public Builder item(EntityPredicate item) {
            this.item = EntityPredicate.wrap(Optional.ofNullable(item));
            return this;
        }

        public Builder item(ItemPredicate item) {
            return item(EntityPredicate.Builder.entity()
                .subPredicate(ItemEntityPredicate.droppedBy(Optional.ofNullable(item), Optional.empty()))
                .build()
            );
        }

        public Builder damage(DamagePredicate damage) {
            this.damage = Optional.ofNullable(damage);
            return this;
        }

        public Criterion<TriggerInstance> build() {
            return BingoTriggers.KILL_ITEM.get().createCriterion(new TriggerInstance(player, item, damage));
        }
    }
}
