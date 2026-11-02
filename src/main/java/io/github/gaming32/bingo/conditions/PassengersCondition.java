package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record PassengersCondition(List<Holder<LootItemCondition>> passengers, boolean requireFull) implements LootItemCondition {
    public static final MapCodec<PassengersCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            LootItemCondition.CODEC.listOf().fieldOf("passengers").forGetter(PassengersCondition::passengers),
            Codec.BOOL.optionalFieldOf("require_full", false).forGetter(PassengersCondition::requireFull)
        ).apply(instance, PassengersCondition::new)
    );

    @Override
    public MapCodec<PassengersCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(LootContext lootContext) {
        final Entity thisEntity = lootContext.getOptional(LootContextParams.THIS_ENTITY);
        if (thisEntity == null || thisEntity.getPassengers().size() < passengers.size()) {
            return false;
        }
        final Set<Entity> toTest = new LinkedHashSet<>(thisEntity.getPassengers());
        topLevel:
        for (final Holder<LootItemCondition> passenger : passengers) {
            final Iterator<Entity> it = toTest.iterator();
            while (it.hasNext()) {
                final Entity testPassenger = it.next();
                final LootParams subParams = new LootParams.Builder(lootContext.getLevel())
                    .withOptionalParameter(LootContextParams.ORIGIN, lootContext.getOptional(LootContextParams.ORIGIN))
                    .withParameter(LootContextParams.THIS_ENTITY, testPassenger)
                    .create(LootContextParamSets.ADVANCEMENT_ENTITY);
                final LootContext subContext = new LootContext.Builder(subParams).create(Optional.empty());
                if (passenger.value().test(subContext)) {
                    it.remove();
                    continue topLevel;
                }
            }
            return false;
        }
        return !requireFull || toTest.isEmpty();
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.THIS_ENTITY, LootContextParams.ORIGIN);
    }
}
