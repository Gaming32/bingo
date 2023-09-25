package io.github.gaming32.bingo.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record HasAnyEffectCondition(LootContext.EntityTarget entityTarget) implements LootItemCondition {
    public static final Codec<HasAnyEffectCondition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            LootContext.EntityTarget.CODEC.fieldOf("entity").forGetter(HasAnyEffectCondition::entityTarget)
        ).apply(instance, HasAnyEffectCondition::new)
    );

    @NotNull
    @Override
    public LootItemConditionType getType() {
        return BingoConditions.HAS_ANY_EFFECT.get();
    }

    @NotNull
    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return Set.of(entityTarget.getParam());
    }

    @Override
    public boolean test(LootContext lootContext) {
        return lootContext.getParam(entityTarget.getParam()) instanceof LivingEntity livingEntity
            && !livingEntity.getActiveEffectsMap().isEmpty();
    }
}
