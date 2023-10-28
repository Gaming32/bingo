package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.gaming32.bingo.util.BingoCodecs;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public record PassengersCondition(List<ContextAwarePredicate> passengers, boolean requireFull) implements LootItemCondition {
    public static final Codec<PassengersCondition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            BingoCodecs.CONTEXT_AWARE_PREDICATE.listOf().fieldOf("passengers").forGetter(PassengersCondition::passengers),
            ExtraCodecs.strictOptionalField(Codec.BOOL, "require_full", false).forGetter(PassengersCondition::requireFull)
        ).apply(instance, PassengersCondition::new)
    );

    @NotNull
    @Override
    public LootItemConditionType getType() {
        return BingoConditions.PASSENGERS.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        final Entity thisEntity = lootContext.getParam(LootContextParams.THIS_ENTITY);
        if (thisEntity.getPassengers().size() < passengers.size()) {
            return false;
        }
        final Set<Entity> toTest = new LinkedHashSet<>(thisEntity.getPassengers());
        topLevel:
        for (final ContextAwarePredicate passenger : passengers) {
            final Iterator<Entity> it = toTest.iterator();
            while (it.hasNext()) {
                final Entity testPassenger = it.next();
                final LootParams subParams = new LootParams.Builder(lootContext.getLevel())
                    .withParameter(LootContextParams.ORIGIN, lootContext.getParam(LootContextParams.ORIGIN))
                    .withParameter(LootContextParams.THIS_ENTITY, testPassenger)
                    .create(LootContextParamSets.ADVANCEMENT_ENTITY);
                final LootContext subContext = new LootContext.Builder(subParams).create(Optional.empty());
                if (passenger.matches(subContext)) {
                    it.remove();
                    continue topLevel;
                }
            }
            return false;
        }
        return !requireFull || toTest.isEmpty();
    }

    @NotNull
    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.THIS_ENTITY, LootContextParams.ORIGIN);
    }
}
